package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.mapper.AvroMapper;
import ru.yandex.practicum.model.HubEventModel;
import ru.yandex.practicum.model.SensorEventModel;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final AvroMapper avroMapper;

    private static final String SENSORS_TOPIC = "telemetry.sensors.v1";
    private static final String HUBS_TOPIC = "telemetry.hubs.v1";

    public void sendSensorEvent(SensorEventModel sensorEvent) {
        try {
            SensorEventAvro avroEvent = avroMapper.toAvro(sensorEvent);
            kafkaTemplate.send(SENSORS_TOPIC, sensorEvent.getHubId(), avroEvent);
            log.debug("Sensor event sent to Kafka: {}", sensorEvent);
        } catch (Exception e) {
            log.error("Failed to send sensor event to Kafka: {}", sensorEvent, e);
            throw new RuntimeException("Failed to send sensor event to Kafka", e);
        }
    }

    public void sendHubEvent(HubEventModel hubEvent) {
        try {
            HubEventAvro avroEvent = avroMapper.toAvro(hubEvent);
            kafkaTemplate.send(HUBS_TOPIC, hubEvent.getHubId(), avroEvent);
            log.debug("Hub event sent to Kafka: {}", hubEvent);
        } catch (Exception e) {
            log.error("Failed to send hub event to Kafka: {}", hubEvent, e);
            throw new RuntimeException("Failed to send hub event to Kafka", e);
        }
    }

}
