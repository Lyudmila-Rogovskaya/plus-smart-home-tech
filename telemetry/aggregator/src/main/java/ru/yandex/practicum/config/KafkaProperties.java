package ru.yandex.practicum.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "kafka")
public class KafkaProperties {
    private String bootstrapServer;
    private ConsumerConfig consumer = new ConsumerConfig();
    private ProducerConfig producer = new ProducerConfig();
    private String topicSensors = "telemetry.sensors.v1";
    private String topicSnapshots = "telemetry.snapshots.v1";

    @Data
    public static class ConsumerConfig {
        private String groupId = "aggregator-group";
        private int maxPollRecords = 100;
        private int fetchMaxBytes = 3072000;
        private int maxPartitionFetchBytes = 307200;
        private int maxPollIntervalMs = 300000;
        private int sessionTimeoutMs = 45000;
        private boolean enableAutoCommit = false;
        private int autoCommitIntervalMs = 5000;
        private String autoOffsetReset = "earliest";
    }

    @Data
    public static class ProducerConfig {
        private String acks = "all";
        private int retries = 3;
        private int maxInFlightRequestsPerConnection = 1;
    }

}
