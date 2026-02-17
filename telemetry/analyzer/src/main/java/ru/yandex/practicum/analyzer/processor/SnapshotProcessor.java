package ru.yandex.practicum.analyzer.processor;

import com.google.protobuf.Timestamp;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.entity.Condition;
import ru.yandex.practicum.analyzer.entity.Scenario;
import ru.yandex.practicum.analyzer.entity.ScenarioAction;
import ru.yandex.practicum.analyzer.entity.ScenarioCondition;
import ru.yandex.practicum.analyzer.entity.enums.ActionType;
import ru.yandex.practicum.analyzer.repository.ScenarioActionRepository;
import ru.yandex.practicum.analyzer.repository.ScenarioConditionRepository;
import ru.yandex.practicum.analyzer.repository.ScenarioRepository;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SnapshotProcessor {
    private final KafkaConsumer<String, SensorsSnapshotAvro> consumer;
    private final ScenarioRepository scenarioRepository;
    private final ScenarioConditionRepository scenarioConditionRepository;
    private final ScenarioActionRepository scenarioActionRepository;

    @GrpcClient("hub-router")
    private HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterClient;

    @Value("${kafka.topic.snapshots}")
    private String topic;

    public void start() {
        consumer.subscribe(List.of(topic));
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
        log.info("SnapshotProcessor started, subscribed to {}", topic);

        try {
            while (true) {
                ConsumerRecords<String, SensorsSnapshotAvro> records = consumer.poll(Duration.ofSeconds(1));
                for (ConsumerRecord<String, SensorsSnapshotAvro> record : records) {
                    try {
                        processSnapshot(record.value());
                    } catch (Exception e) {
                        log.error("Error processing snapshot", e);
                    }
                }
                consumer.commitSync();
            }
        } catch (WakeupException e) {
            log.info("SnapshotProcessor received wakeup signal");
        } catch (Exception e) {
            log.error("Error in SnapshotProcessor loop", e);
        } finally {
            consumer.close();
            log.info("SnapshotProcessor closed");
        }
    }

    private void processSnapshot(SensorsSnapshotAvro snapshot) {
        String hubId = snapshot.getHubId();
        Map<String, SensorStateAvro> states = snapshot.getSensorsState();

        List<Scenario> scenarios = scenarioRepository.findByHubId(hubId);
        if (scenarios.isEmpty()) return;

        for (Scenario scenario : scenarios) {
            boolean allMet = true;
            List<ScenarioCondition> conditions = scenarioConditionRepository.findByScenarioId(scenario.getId());
            for (ScenarioCondition sc : conditions) {
                SensorStateAvro state = states.get(sc.getSensor().getId());
                if (state == null || !checkCondition(sc.getCondition(), state.getData())) {
                    allMet = false;
                    break;
                }
            }
            if (allMet) {
                executeActions(scenario, hubId);
            }
        }
    }

    private boolean checkCondition(Condition condition, Object sensorData) {
        int actual;
        if (sensorData instanceof ClimateSensorAvro c) {
            actual = switch (condition.getType()) {
                case TEMPERATURE -> c.getTemperatureC();
                case CO2LEVEL -> c.getCo2Level();
                case HUMIDITY -> c.getHumidity();
                default ->
                        throw new IllegalArgumentException("Unsupported condition type " + condition.getType() + " for ClimateSensor");
            };
        } else if (sensorData instanceof LightSensorAvro l) {
            actual = switch (condition.getType()) {
                case LUMINOSITY -> l.getLuminosity();
                default ->
                        throw new IllegalArgumentException("Unsupported condition type " + condition.getType() + " for LightSensor");
            };
        } else if (sensorData instanceof MotionSensorAvro m) {
            actual = switch (condition.getType()) {
                case MOTION -> m.getMotion() ? 1 : 0;
                default ->
                        throw new IllegalArgumentException("Unsupported condition type " + condition.getType() + " for MotionSensor");
            };
        } else if (sensorData instanceof SwitchSensorAvro s) {
            actual = switch (condition.getType()) {
                case SWITCH -> s.getState() ? 1 : 0;
                default ->
                        throw new IllegalArgumentException("Unsupported condition type " + condition.getType() + " for SwitchSensor");
            };
        } else if (sensorData instanceof TemperatureSensorAvro t) {
            actual = switch (condition.getType()) {
                case TEMPERATURE -> t.getTemperatureC();
                default ->
                        throw new IllegalArgumentException("Unsupported condition type " + condition.getType() + " for TemperatureSensor");
            };
        } else {
            throw new IllegalArgumentException("Unknown sensor data type: " + sensorData.getClass());
        }

        return switch (condition.getOperation()) {
            case EQUALS -> actual == condition.getValue();
            case GREATER_THAN -> actual > condition.getValue();
            case LOWER_THAN -> actual < condition.getValue();
        };
    }

    private void executeActions(Scenario scenario, String hubId) {
        List<ScenarioAction> actions = scenarioActionRepository.findByScenarioId(scenario.getId());
        Instant now = Instant.now();

        for (ScenarioAction sa : actions) {
            DeviceActionRequest request = DeviceActionRequest.newBuilder()
                    .setHubId(hubId)
                    .setScenarioName(scenario.getName())
                    .setAction(DeviceActionProto.newBuilder()
                            .setSensorId(sa.getSensor().getId())
                            .setTypeValue(getActionTypeValue(sa.getAction().getType()))
                            .setValue(sa.getAction().getValue() != null ? sa.getAction().getValue() : 0)
                            .build())
                    .setTimestamp(Timestamp.newBuilder()
                            .setSeconds(now.getEpochSecond())
                            .setNanos(now.getNano())
                            .build())
                    .build();
            try {
                hubRouterClient.handleDeviceAction(request);
                log.info("Sent action for scenario {} to hub {}, sensor {}", scenario.getName(), hubId, sa.getSensor().getId());
            } catch (StatusRuntimeException e) {
                log.error("Failed to send action to hub router", e);
            }
        }
    }

    private int getActionTypeValue(ActionType actionType) {
        return switch (actionType) {
            case ACTIVATE -> 0;
            case DEACTIVATE -> 1;
            case INVERSE -> 2;
            case SET_VALUE -> 3;
        };
    }

}
