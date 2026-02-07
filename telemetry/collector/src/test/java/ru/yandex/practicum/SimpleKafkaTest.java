//package ru.yandex.practicum;
//
//import io.confluent.kafka.serializers.KafkaAvroSerializer;
//import org.apache.kafka.clients.producer.KafkaProducer;
//import org.apache.kafka.clients.producer.ProducerRecord;
//import org.apache.kafka.common.serialization.StringSerializer;
//import org.junit.jupiter.api.Test;
//import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
//import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
//
//import java.util.Properties;
//
//public class SimpleKafkaTest {
//
//    @Test
//    void testDirectKafkaConnection() throws Exception {
//        Properties props = new Properties();
//        props.put("bootstrap.servers", "localhost:9092");
//        props.put("key.serializer", StringSerializer.class);
//        props.put("value.serializer", KafkaAvroSerializer.class);
//        props.put("schema.registry.url", "http://localhost:8081");
//
//        try (KafkaProducer<String, SensorEventAvro> producer = new KafkaProducer<>(props)) {
//            LightSensorAvro lightSensor = LightSensorAvro.newBuilder()
//                    .setLinkQuality(75)
//                    .setLuminosity(59)
//                    .build();
//
//            SensorEventAvro event = SensorEventAvro.newBuilder()
//                    .setId("test-sensor-1")
//                    .setHubId("test-hub-1")
//                    .setTimestamp(System.currentTimeMillis())
//                    .setPayload(lightSensor)
//                    .build();
//
//            ProducerRecord<String, SensorEventAvro> record =
//                    new ProducerRecord<>("telemetry.sensors.v1", "test-hub-1", event);
//
//            producer.send(record);
//            producer.flush();
//            System.out.println("Message sent successfully!");
//        }
//    }
//}
