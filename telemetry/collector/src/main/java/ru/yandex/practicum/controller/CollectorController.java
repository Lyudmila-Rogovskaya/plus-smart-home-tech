package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.model.HubEventModel;
import ru.yandex.practicum.model.SensorEventModel;
import ru.yandex.practicum.service.KafkaProducerService;

@Slf4j
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class CollectorController {

    private final KafkaProducerService kafkaProducerService;

    @PostMapping("/sensors")
    @ResponseStatus(HttpStatus.OK)
    public void collectSensorEvent(@Valid @RequestBody SensorEventModel event) {
        log.info("Received sensor event: {}", event);
        try {
            kafkaProducerService.sendSensorEvent(event);
        } catch (Exception e) {
            log.error("Failed to process sensor event: {}", event, e);
            throw e;
        }
    }

    @PostMapping("/hubs")
    @ResponseStatus(HttpStatus.OK)
    public void collectHubEvent(@Valid @RequestBody HubEventModel event) {
        log.info("Received hub event: {}", event);
        try {
            kafkaProducerService.sendHubEvent(event);
        } catch (Exception e) {
            log.error("Failed to process hub event: {}", event, e);
            throw e;
        }
    }

}
