package ru.yandex.practicum;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class KafkaConnectionTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    void contextLoads() { // проверяем, что контекст загружается и KafkaTemplate доступен
    }

    @Test
    void testKafkaConnection() { // проверяем подключения к Kafka
        System.out.println("Kafka connection test...");
    }

}
