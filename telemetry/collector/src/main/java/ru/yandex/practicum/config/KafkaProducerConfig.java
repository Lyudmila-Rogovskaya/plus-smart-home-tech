package ru.yandex.practicum.config;

import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

@Configuration
public class KafkaProducerConfig {

    @Value("${kafka.bootstrap.server}")
    private String bootstrapServer;

    @Bean
    public Producer<String, SpecificRecordBase> kafkaProducer() {
        Properties config = new Properties();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, GeneralAvroSerializer.class);

        return new KafkaProducer<>(config);
    }

    public static class GeneralAvroSerializer implements org.apache.kafka.common.serialization.Serializer<SpecificRecordBase> {
        private final EncoderFactory encoderFactory = EncoderFactory.get();
        private BinaryEncoder encoder;

        @Override
        public byte[] serialize(String topic, SpecificRecordBase data) {
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                byte[] result = null;
                encoder = encoderFactory.binaryEncoder(out, encoder);
                if (data != null) {
                    DatumWriter<SpecificRecordBase> writer = new SpecificDatumWriter<>(data.getSchema());
                    writer.write(data, encoder);
                    encoder.flush();
                    result = out.toByteArray();
                }
                return result;
            } catch (IOException ex) {
                throw new org.apache.kafka.common.errors.SerializationException(
                        "Ошибка сериализации данных для топика [" + topic + "]", ex);
            }
        }
    }

}
