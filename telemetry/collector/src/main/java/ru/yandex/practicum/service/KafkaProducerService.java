package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.config.CollectorKafkaProperties;
import ru.yandex.practicum.config.TopicType;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.mapper.AvroMapper;
import ru.yandex.practicum.model.HubEventModel;
import ru.yandex.practicum.model.SensorEventModel;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final Producer<String, SpecificRecordBase> kafkaProducer;
    private final CollectorKafkaProperties properties;

    public void sendSensorEvent(SensorEventModel sensorEvent) {
        try {
            SensorEventAvro avroEvent = AvroMapper.toAvro(sensorEvent);

            ProducerRecord<String, SpecificRecordBase> record = new ProducerRecord<>(
                    properties.getProducer().getTopics().get(TopicType.SENSORS_EVENTS),
                    null,
                    sensorEvent.getTimestamp().toEpochMilli(),
                    sensorEvent.getId(),
                    avroEvent
            );

            Future<RecordMetadata> future = kafkaProducer.send(record);
            RecordMetadata metadata = future.get(5, TimeUnit.SECONDS);

            log.info("Отправили сенсорное событие в Kafka. Топик: {}, партиция: {}, offset: {}, hubId: {}",
                    metadata.topic(), metadata.partition(), metadata.offset(), avroEvent.getHubId());

        } catch (Exception e) {
            log.error("Ошибка при отправке сенсорного события в Kafka", e);
        }
    }

    public void sendHubEvent(HubEventModel hubEvent) {
        try {
            HubEventAvro avroEvent = AvroMapper.toAvro(hubEvent);

            ProducerRecord<String, SpecificRecordBase> record = new ProducerRecord<>(
                    properties.getProducer().getTopics().get(TopicType.HUBS_EVENTS),
                    null,
                    hubEvent.getTimestamp().toEpochMilli(),
                    hubEvent.getHubId(),
                    avroEvent
            );

            Future<RecordMetadata> future = kafkaProducer.send(record);
            RecordMetadata metadata = future.get(5, TimeUnit.SECONDS);

            log.info("Отправили событие хаба в Kafka. Топик: {}, партиция: {}, offset: {}, hubId: {}",
                    metadata.topic(), metadata.partition(), metadata.offset(), avroEvent.getHubId());

        } catch (Exception e) {
            log.error("Ошибка при отправке события хаба в Kafka", e);
        }
    }

}
