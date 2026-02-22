package ru.yandex.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.*;
import ru.yandex.practicum.model.*;

import java.time.Instant;

@Component
public class ProtobufMapper {

    public SensorEventModel toSensorEventModel(SensorEventProto proto) {
        String id = proto.getId();
        String hubId = proto.getHubId();
        Instant timestamp = Instant.ofEpochSecond(
                proto.getTimestamp().getSeconds(),
                proto.getTimestamp().getNanos()
        );

        switch (proto.getPayloadCase()) {
            case MOTION_SENSOR:
                MotionSensorProto motion = proto.getMotionSensor();
                MotionSensorEvent motionEvent = new MotionSensorEvent();
                motionEvent.setId(id);
                motionEvent.setHubId(hubId);
                motionEvent.setTimestamp(timestamp);
                motionEvent.setLinkQuality(motion.getLinkQuality());
                motionEvent.setMotion(motion.getMotion());
                motionEvent.setVoltage(motion.getVoltage());
                return motionEvent;

            case TEMPERATURE_SENSOR:
                TemperatureSensorProto temp = proto.getTemperatureSensor();
                TemperatureSensorEvent tempEvent = new TemperatureSensorEvent();
                tempEvent.setId(id);
                tempEvent.setHubId(hubId);
                tempEvent.setTimestamp(timestamp);
                tempEvent.setTemperatureC(temp.getTemperatureC());
                tempEvent.setTemperatureF(temp.getTemperatureF());
                return tempEvent;

            case LIGHT_SENSOR:
                LightSensorProto light = proto.getLightSensor();
                LightSensorEvent lightEvent = new LightSensorEvent();
                lightEvent.setId(id);
                lightEvent.setHubId(hubId);
                lightEvent.setTimestamp(timestamp);
                lightEvent.setLinkQuality(light.getLinkQuality());
                lightEvent.setLuminosity(light.getLuminosity());
                return lightEvent;

            case CLIMATE_SENSOR:
                ClimateSensorProto climate = proto.getClimateSensor();
                ClimateSensorEventModel climateEvent = new ClimateSensorEventModel();
                climateEvent.setId(id);
                climateEvent.setHubId(hubId);
                climateEvent.setTimestamp(timestamp);
                climateEvent.setTemperatureC(climate.getTemperatureC());
                climateEvent.setHumidity(climate.getHumidity());
                climateEvent.setCo2Level(climate.getCo2Level());
                return climateEvent;

            case SWITCH_SENSOR:
                SwitchSensorProto switchSensor = proto.getSwitchSensor();
                SwitchSensorEvent switchEvent = new SwitchSensorEvent();
                switchEvent.setId(id);
                switchEvent.setHubId(hubId);
                switchEvent.setTimestamp(timestamp);
                switchEvent.setState(switchSensor.getState());
                return switchEvent;

            default:
                throw new IllegalArgumentException("Unknown sensor event type: " + proto.getPayloadCase());
        }
    }

    public HubEventModel toHubEventModel(HubEventProto proto) {
        String hubId = proto.getHubId();
        Instant timestamp = Instant.ofEpochSecond(
                proto.getTimestamp().getSeconds(),
                proto.getTimestamp().getNanos()
        );

        switch (proto.getPayloadCase()) {
            case DEVICE_ADDED:
                DeviceAddedEventProto deviceAdded = proto.getDeviceAdded();
                DeviceAddedEventModel addedEvent = new DeviceAddedEventModel();
                addedEvent.setHubId(hubId);
                addedEvent.setTimestamp(timestamp);
                addedEvent.setId(deviceAdded.getId());
                addedEvent.setDeviceType(convertDeviceType(deviceAdded.getType()));
                return addedEvent;

            case DEVICE_REMOVED:
                DeviceRemovedEventProto deviceRemoved = proto.getDeviceRemoved();
                DeviceRemovedEventModel removedEvent = new DeviceRemovedEventModel();
                removedEvent.setHubId(hubId);
                removedEvent.setTimestamp(timestamp);
                removedEvent.setId(deviceRemoved.getId());
                return removedEvent;

            case SCENARIO_ADDED:
                ScenarioAddedEventProto scenarioAdded = proto.getScenarioAdded();
                ScenarioAddedEventModel addedScenario = new ScenarioAddedEventModel();
                addedScenario.setHubId(hubId);
                addedScenario.setTimestamp(timestamp);
                addedScenario.setName(scenarioAdded.getName());
                addedScenario.setConditions(
                        scenarioAdded.getConditionList().stream()
                                .map(this::convertCondition)
                                .toList()
                );
                addedScenario.setActions(
                        scenarioAdded.getActionList().stream()
                                .map(this::convertAction)
                                .toList()
                );
                return addedScenario;

            case SCENARIO_REMOVED:
                ScenarioRemovedEventProto scenarioRemoved = proto.getScenarioRemoved();
                ScenarioRemovedEventModel removedScenario = new ScenarioRemovedEventModel();
                removedScenario.setHubId(hubId);
                removedScenario.setTimestamp(timestamp);
                removedScenario.setName(scenarioRemoved.getName());
                return removedScenario;

            default:
                throw new IllegalArgumentException("Unknown hub event type: " + proto.getPayloadCase());
        }
    }

    private DeviceTypeModel convertDeviceType(DeviceTypeProto type) {
        return switch (type) {
            case MOTION_SENSOR -> DeviceTypeModel.MOTION_SENSOR;
            case TEMPERATURE_SENSOR -> DeviceTypeModel.TEMPERATURE_SENSOR;
            case LIGHT_SENSOR -> DeviceTypeModel.LIGHT_SENSOR;
            case CLIMATE_SENSOR -> DeviceTypeModel.CLIMATE_SENSOR;
            case SWITCH_SENSOR -> DeviceTypeModel.SWITCH_SENSOR;
            default -> throw new IllegalArgumentException("Unknown device type: " + type);
        };
    }

    private ScenarioConditionModel convertCondition(ScenarioConditionProto proto) {
        ScenarioConditionModel condition = new ScenarioConditionModel();
        condition.setSensorId(proto.getSensorId());
        condition.setType(convertConditionType(proto.getType()));
        condition.setOperation(convertConditionOperation(proto.getOperation()));

        if (proto.hasBoolValue()) {
            condition.setValue(proto.getBoolValue() ? 1 : 0);
        } else if (proto.hasIntValue()) {
            condition.setValue(proto.getIntValue());
        }
        return condition;
    }

    private ConditionTypeModel convertConditionType(ConditionTypeProto type) {
        return switch (type) {
            case MOTION -> ConditionTypeModel.MOTION;
            case LUMINOSITY -> ConditionTypeModel.LUMINOSITY;
            case SWITCH -> ConditionTypeModel.SWITCH;
            case TEMPERATURE -> ConditionTypeModel.TEMPERATURE;
            case CO2LEVEL -> ConditionTypeModel.CO2LEVEL;
            case HUMIDITY -> ConditionTypeModel.HUMIDITY;
            default -> throw new IllegalArgumentException("Unknown condition type: " + type);
        };
    }

    private ConditionOperationModel convertConditionOperation(ConditionOperationProto op) {
        return switch (op) {
            case EQUALS -> ConditionOperationModel.EQUALS;
            case GREATER_THAN -> ConditionOperationModel.GREATER_THAN;
            case LOWER_THAN -> ConditionOperationModel.LOWER_THAN;
            default -> throw new IllegalArgumentException("Unknown operation: " + op);
        };
    }

    private DeviceActionModel convertAction(DeviceActionProto proto) {
        DeviceActionModel action = new DeviceActionModel();
        action.setSensorId(proto.getSensorId());
        action.setType(convertActionType(proto.getType()));
        if (proto.hasValue()) {
            action.setValue(proto.getValue());
        }
        return action;
    }

    private ActionTypeModel convertActionType(ActionTypeProto type) {
        return switch (type) {
            case ACTIVATE -> ActionTypeModel.ACTIVATE;
            case DEACTIVATE -> ActionTypeModel.DEACTIVATE;
            case INVERSE -> ActionTypeModel.INVERSE;
            case SET_VALUE -> ActionTypeModel.SET_VALUE;
            default -> throw new IllegalArgumentException("Unknown action type: " + type);
        };
    }

}
