package ru.yandex.practicum.analyzer.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.entity.*;
import ru.yandex.practicum.analyzer.entity.enums.ActionType;
import ru.yandex.practicum.analyzer.entity.enums.ConditionOperation;
import ru.yandex.practicum.analyzer.entity.enums.ConditionType;
import ru.yandex.practicum.analyzer.repository.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class HubEventProcessor implements Runnable {
    private final KafkaConsumer<String, HubEventAvro> consumer;
    private final SensorRepository sensorRepository;
    private final ScenarioRepository scenarioRepository;
    private final ConditionRepository conditionRepository;
    private final ActionRepository actionRepository;
    private final ScenarioConditionRepository scenarioConditionRepository;
    private final ScenarioActionRepository scenarioActionRepository;

    @Value("${kafka.topic.hubs}")
    private String topic;

    @Override
    public void run() {
        consumer.subscribe(List.of(topic));
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
        log.info("HubEventProcessor started, subscribed to {}", topic);

        try {
            while (true) {
                ConsumerRecords<String, HubEventAvro> records = consumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, HubEventAvro> record : records) {
                    try {
                        process(record.value());
                    } catch (Exception e) {
                        log.error("Error processing hub event", e);
                    }
                }
            }
        } catch (WakeupException e) {
            log.info("HubEventProcessor received wakeup signal");
        } catch (Exception e) {
            log.error("Error in HubEventProcessor loop", e);
        } finally {
            consumer.close();
            log.info("HubEventProcessor closed");
        }
    }

    private void process(HubEventAvro event) {
        switch (event.getPayload()) {
            case DeviceAddedEventAvro deviceAdded -> handleDeviceAdded(event.getHubId(), deviceAdded);
            case DeviceRemovedEventAvro deviceRemoved -> handleDeviceRemoved(event.getHubId(), deviceRemoved);
            case ScenarioAddedEventAvro scenarioAdded -> handleScenarioAdded(event.getHubId(), scenarioAdded);
            case ScenarioRemovedEventAvro scenarioRemoved -> handleScenarioRemoved(event.getHubId(), scenarioRemoved);
            default -> log.warn("Unknown hub event payload: {}", event.getPayload());
        }
    }

    private void handleDeviceAdded(String hubId, DeviceAddedEventAvro deviceAdded) {
        Sensor sensor = new Sensor(deviceAdded.getId(), hubId);
        sensorRepository.save(sensor);
        log.info("Added sensor {} to hub {}", sensor.getId(), hubId);
    }

    private void handleDeviceRemoved(String hubId, DeviceRemovedEventAvro deviceRemoved) {
        sensorRepository.findById(deviceRemoved.getId()).ifPresent(sensor -> {
            if (!sensor.getHubId().equals(hubId)) {
                log.warn("Sensor {} belongs to different hub {}, ignoring removal", deviceRemoved.getId(), sensor.getHubId());
                return;
            }
            sensorRepository.delete(sensor);
            log.info("Removed sensor {} from hub {}", deviceRemoved.getId(), hubId);
        });
    }

    private void handleScenarioAdded(String hubId, ScenarioAddedEventAvro scenarioAdded) {
        scenarioRepository.findByHubIdAndName(hubId, scenarioAdded.getName()).ifPresent(scenario -> {
            scenarioRepository.delete(scenario);
            scenarioRepository.flush();
        });

        Scenario scenario = new Scenario();
        scenario.setHubId(hubId);
        scenario.setName(scenarioAdded.getName());
        scenario = scenarioRepository.save(scenario);

        for (ScenarioConditionAvro condAvro : scenarioAdded.getConditions()) {
            Condition condition = new Condition();
            condition.setType(ConditionType.valueOf(condAvro.getType().name()));
            condition.setOperation(ConditionOperation.valueOf(condAvro.getOperation().name()));
            Object val = condAvro.getValue();
            if (val instanceof Integer) condition.setValue((Integer) val);
            else if (val instanceof Boolean) condition.setValue((Boolean) val ? 1 : 0);
            else condition.setValue(0);
            condition = conditionRepository.save(condition);

            Sensor sensor = sensorRepository.findById(condAvro.getSensorId())
                    .orElseThrow(() -> new IllegalArgumentException("Sensor not found: " + condAvro.getSensorId()));

            ScenarioCondition sc = new ScenarioCondition();
            sc.setScenario(scenario);
            sc.setSensor(sensor);
            sc.setCondition(condition);
            sc.setId(new ScenarioConditionId(scenario.getId(), sensor.getId(), condition.getId()));
            scenarioConditionRepository.save(sc);
        }

        for (DeviceActionAvro actAvro : scenarioAdded.getActions()) {
            Action action = new Action();
            action.setType(ActionType.valueOf(actAvro.getType().name()));
            action.setValue(actAvro.getValue());
            action = actionRepository.save(action);

            Sensor sensor = sensorRepository.findById(actAvro.getSensorId())
                    .orElseThrow(() -> new IllegalArgumentException("Sensor not found: " + actAvro.getSensorId()));

            ScenarioAction sa = new ScenarioAction();
            sa.setScenario(scenario);
            sa.setSensor(sensor);
            sa.setAction(action);
            sa.setId(new ScenarioActionId(scenario.getId(), sensor.getId(), action.getId()));
            scenarioActionRepository.save(sa);
        }

        log.info("Added scenario {} for hub {}", scenario.getName(), hubId);
    }

    private void handleScenarioRemoved(String hubId, ScenarioRemovedEventAvro scenarioRemoved) {
        scenarioRepository.findByHubIdAndName(hubId, scenarioRemoved.getName()).ifPresent(scenario -> {
            scenarioRepository.delete(scenario);
            log.info("Removed scenario {} from hub {}", scenarioRemoved.getName(), hubId);
        });
    }

}
