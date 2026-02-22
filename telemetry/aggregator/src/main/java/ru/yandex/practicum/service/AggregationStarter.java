package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.config.KafkaProperties;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationStarter {

    private final KafkaConsumer<String, SensorEventAvro> consumer;
    private final Producer<String, SensorsSnapshotAvro> producer;
    private final KafkaProperties properties;

    private final Map<String, SensorsSnapshotAvro> snapshots = new HashMap<>();

    private final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();

    public void start() {
        consumer.subscribe(List.of(properties.getTopicSensors()));

        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

        log.info("Aggregator started, subscribed to {}", properties.getTopicSensors());

        try {
            while (true) {
                ConsumerRecords<String, SensorEventAvro> records = consumer.poll(Duration.ofSeconds(1));

                for (ConsumerRecord<String, SensorEventAvro> record : records) {
                    SensorEventAvro event = record.value();
                    log.debug("Received event: hubId={}, sensorId={}, timestamp={}",
                            event.getHubId(), event.getId(), event.getTimestamp());

                    Optional<SensorsSnapshotAvro> updatedSnapshot = updateSnapshot(event);

                    updatedSnapshot.ifPresent(snapshot -> {
                        ProducerRecord<String, SensorsSnapshotAvro> producerRecord = new ProducerRecord<>(
                                properties.getTopicSnapshots(),
                                snapshot.getHubId(),
                                snapshot
                        );
                        producer.send(producerRecord, (metadata, exception) -> {
                            if (exception != null) {
                                log.error("Failed to send snapshot for hub {}", snapshot.getHubId(), exception);
                            } else {
                                log.info("Snapshot sent for hub {} to partition {} offset {}",
                                        snapshot.getHubId(), metadata.partition(), metadata.offset());
                            }
                        });
                    });

                    currentOffsets.put(
                            new TopicPartition(record.topic(), record.partition()),
                            new OffsetAndMetadata(record.offset() + 1)
                    );
                }

                if (!records.isEmpty()) {
                    consumer.commitAsync(currentOffsets, (offsets, exception) -> {
                        if (exception != null) {
                            log.error("Commit failed for offsets {}", offsets, exception);
                        }
                    });
                }
            }
        } catch (WakeupException e) {
            log.info("Received shutdown signal");
        } catch (Exception e) {
            log.error("Error in poll loop", e);
        } finally {
            try {
                consumer.commitSync(currentOffsets);
                log.info("Final offsets committed");
            } catch (Exception e) {
                log.error("Error during final commit", e);
            } finally {
                producer.flush();
                consumer.close();
                producer.close();
                log.info("Aggregator stopped");
            }
        }
    }

    private Optional<SensorsSnapshotAvro> updateSnapshot(SensorEventAvro event) {
        String hubId = event.getHubId();
        String sensorId = event.getId();
        long eventTimestamp = event.getTimestamp();
        Instant eventInstant = Instant.ofEpochMilli(eventTimestamp);

        SensorsSnapshotAvro snapshot = snapshots.computeIfAbsent(hubId, k ->
                SensorsSnapshotAvro.newBuilder()
                        .setHubId(hubId)
                        .setTimestamp(eventInstant)
                        .setSensorsState(new HashMap<>())
                        .build()
        );

        Map<String, SensorStateAvro> stateMap = snapshot.getSensorsState();
        SensorStateAvro oldState = stateMap.get(sensorId);

        if (oldState != null) {
            if (oldState.getTimestamp().isAfter(eventInstant)) {
                log.debug("Event is older than current state for sensor {} ({} > {}), ignoring",
                        sensorId, oldState.getTimestamp(), eventInstant);
                return Optional.empty();
            }
            if (oldState.getData().equals(event.getPayload())) {
                log.debug("Sensor {} data unchanged, ignoring", sensorId);
                return Optional.empty();
            }
        }

        SensorStateAvro newState = SensorStateAvro.newBuilder()
                .setTimestamp(eventInstant)
                .setData(event.getPayload())
                .build();

        stateMap.put(sensorId, newState);
        snapshot.setTimestamp(eventInstant);

        log.debug("Snapshot updated for hub {}", hubId);
        return Optional.of(snapshot);
    }

}
