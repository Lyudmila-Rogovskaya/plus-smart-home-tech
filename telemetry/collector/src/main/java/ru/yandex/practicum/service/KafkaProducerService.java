package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
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

    private final Producer<String, SpecificRecordBase> kafkaProducer;
    private final AvroMapper avroMapper;

    @Value("${kafka.topic.sensor}")
    private String sensorsTopic;

    @Value("${kafka.topic.hub}")
    private String hubsTopic;

    public void sendSensorEvent(SensorEventModel sensorEvent) {
        try {
            SensorEventAvro avroEvent = avroMapper.toAvro(sensorEvent);
            ProducerRecord<String, SpecificRecordBase> record = new ProducerRecord<>(
                    sensorsTopic,
                    null,
                    sensorEvent.getTimestamp().toEpochMilli(),
                    avroEvent.getHubId(),
                    avroEvent);

            kafkaProducer.send(record);
            log.info("Отправили сенсорное событие в Kafka: {}", record);
        } catch (Exception e) {
            log.error("Ошибка при отправке сенсорного события в Kafka", e);
            throw new RuntimeException("Ошибка при отправке сенсорного события в Kafka", e);
        }
    }

    public void sendHubEvent(HubEventModel hubEvent) {
        try {
            HubEventAvro avroEvent = avroMapper.toAvro(hubEvent);
            ProducerRecord<String, SpecificRecordBase> record = new ProducerRecord<>(
                    hubsTopic,
                    null,
                    hubEvent.getTimestamp().toEpochMilli(),
                    avroEvent.getHubId(),
                    avroEvent);

            kafkaProducer.send(record);
            log.info("Отправили событие хаба в Kafka: {}", record);
        } catch (Exception e) {
            log.error("Ошибка при отправке события хаба в Kafka", e);
            throw new RuntimeException("Ошибка при отправке события хаба в Kafka", e);
        }
    }

}
