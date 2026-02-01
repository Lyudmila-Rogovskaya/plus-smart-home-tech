package ru.yandex.practicum.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;

import java.util.Map;

@Configuration
public class KafkaConfig {

    @Autowired
    private org.springframework.boot.autoconfigure.kafka.KafkaProperties kafkaProperties;

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = kafkaProperties.buildProducerProperties(null);
        configProps.put("schema.registry.url", "http://localhost:8081");
        configProps.put("specific.avro.reader", true);

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

}
