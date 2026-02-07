package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.model.HubEventModel;
import ru.yandex.practicum.model.SensorEventModel;
import ru.yandex.practicum.service.KafkaProducerService;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class CollectorController {

    private final KafkaProducerService kafkaProducerService;

    @PostMapping("/sensors")
    @ResponseStatus(HttpStatus.OK)
    public void collectSensorEvent(@Valid @RequestBody SensorEventModel event) {
        log.info("Received sensor event type: {}, hubId: {}, id: {}",
                event.getType(), event.getHubId(), event.getId());
        try {
            kafkaProducerService.sendSensorEvent(event);
            log.debug("Successfully processed sensor event: {}", event);
        } catch (Exception e) {
            log.error("Failed to process sensor event: {}", event, e);
            throw new RuntimeException("Failed to process sensor event: " + e.getMessage(), e);
        }
    }

    @PostMapping("/hubs")
    @ResponseStatus(HttpStatus.OK)
    public void collectHubEvent(@Valid @RequestBody HubEventModel event) {
        log.info("Received hub event type: {}, hubId: {}", event.getType(), event.getHubId());
        try {
            kafkaProducerService.sendHubEvent(event);
            log.debug("Successfully processed hub event: {}", event);
        } catch (Exception e) {
            log.error("Failed to process hub event: {}", event, e);
            throw new RuntimeException("Failed to process hub event: " + e.getMessage(), e);
        }
    }

}
