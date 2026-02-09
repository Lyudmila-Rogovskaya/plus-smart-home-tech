package ru.yandex.practicum.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "kafka")
public class KafkaProperties {
    private String bootstrapServer;
    private ProducerConfig producer = new ProducerConfig();

    @Data
    public static class ProducerConfig {
        private String acks = "all";
        private int retries = 3;
        private int maxInFlightRequestsPerConnection = 1;
    }

}
