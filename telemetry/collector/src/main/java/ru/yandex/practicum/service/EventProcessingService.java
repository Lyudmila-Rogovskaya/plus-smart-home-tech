package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.model.HubEventModel;
import ru.yandex.practicum.model.SensorEventModel;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventProcessingService {

    private final KafkaProducerService kafkaProducerService;

    public void processSensorEvent(SensorEventModel sensorEvent) {
        if (sensorEvent.getType() == null) {
            throw new IllegalArgumentException("Sensor event type cannot be null");
        }

        log.debug("Processing sensor event of type: {}", sensorEvent.getType());
        kafkaProducerService.sendSensorEvent(sensorEvent);
    }

    public void processHubEvent(HubEventModel hubEvent) {
        if (hubEvent.getType() == null) {
            throw new IllegalArgumentException("Hub event type cannot be null");
        }

        log.debug("Processing hub event of type: {}", hubEvent.getType());
        kafkaProducerService.sendHubEvent(hubEvent);
    }

}
