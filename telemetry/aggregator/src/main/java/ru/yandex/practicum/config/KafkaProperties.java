package ru.yandex.practicum.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "kafka")
public class KafkaProperties {
    private String bootstrapServer;
    private ConsumerConfig consumer = new ConsumerConfig();
    private ProducerConfig producer = new ProducerConfig();
    private Map<String, String> topics;

    @Data
    public static class ConsumerConfig {
        private String groupId;
        private int maxPollRecords;
        private int fetchMaxBytes;
        private int maxPartitionFetchBytes;
        private int maxPollIntervalMs;
        private int sessionTimeoutMs;
        private boolean enableAutoCommit;
        private int autoCommitIntervalMs;
        private String autoOffsetReset;
    }

    @Data
    public static class ProducerConfig {
        private String acks;
        private int retries;
        private int maxInFlightRequestsPerConnection;
    }

}
