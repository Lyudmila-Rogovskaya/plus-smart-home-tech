package ru.yandex.practicum.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.EnumMap;
import java.util.Map;
import java.util.Properties;

@Getter
@ConfigurationProperties(prefix = "collector.kafka")
public class CollectorKafkaProperties {
    private final ProducerConfig producer;

    public CollectorKafkaProperties(ProducerConfig producer) {
        this.producer = producer;
    }

    @Getter
    public static class ProducerConfig {
        private final Properties properties;
        private final EnumMap<TopicType, String> topics;

        public ProducerConfig(Properties properties, Map<String, String> topics) {
            this.properties = properties;
            this.topics = new EnumMap<>(TopicType.class);
            for (Map.Entry<String, String> entry : topics.entrySet()) {
                this.topics.put(TopicType.from(entry.getKey()), entry.getValue());
            }
        }
    }

}
