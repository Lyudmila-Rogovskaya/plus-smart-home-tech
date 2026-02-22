package ru.yandex.practicum.config;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {

    @Bean
    public Producer<String, org.apache.avro.specific.SpecificRecordBase> kafkaProducer(
            CollectorKafkaProperties properties) {
        return new KafkaProducer<>(properties.getProducer().getProperties());
    }

}
