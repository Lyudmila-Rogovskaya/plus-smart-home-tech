package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.model.HubEventModel;
import ru.yandex.practicum.model.SensorEventModel;
import ru.yandex.practicum.service.EventProcessingService;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class CollectorController {

    private final EventProcessingService eventProcessingService;

    @PostMapping("/sensors")
    public void collectSensorEvent(@Valid @RequestBody SensorEventModel event) {
        log.info("Received sensor event type: {}, hubId: {}, id: {}",
                event.getType(), event.getHubId(), event.getId());

        eventProcessingService.processSensorEvent(event);
        log.debug("Successfully processed sensor event: {}", event);
    }

    @PostMapping("/hubs")
    public void collectHubEvent(@Valid @RequestBody HubEventModel event) {
        log.info("Received hub event type: {}, hubId: {}", event.getType(), event.getHubId());

        eventProcessingService.processHubEvent(event);
        log.debug("Successfully processed hub event: {}", event);
    }

}
