package ru.yandex.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.model.*;

import java.util.stream.Collectors;

@Component
public class AvroMapper {

    public SensorEventAvro toAvro(SensorEventModel sensorEvent) {
        SensorEventAvro.Builder builder = SensorEventAvro.newBuilder()
                .setId(sensorEvent.getId())
                .setHubId(sensorEvent.getHubId())
                .setTimestamp(sensorEvent.getTimestamp().toEpochMilli());

        switch (sensorEvent.getType()) {
            case CLIMATE_SENSOR_EVENT:
                ClimateSensorEventModel climateEvent = (ClimateSensorEventModel) sensorEvent;
                ClimateSensorAvro climateSensor = ClimateSensorAvro.newBuilder()
                        .setTemperatureC(climateEvent.getTemperatureC())
                        .setHumidity(climateEvent.getHumidity())
                        .setCo2Level(climateEvent.getCo2Level())
                        .build();
                builder.setPayload(climateSensor);
                break;

            case LIGHT_SENSOR_EVENT:
                LightSensorEvent lightEvent = (LightSensorEvent) sensorEvent;
                LightSensorAvro lightSensor = LightSensorAvro.newBuilder()
                        .setLinkQuality(lightEvent.getLinkQuality())
                        .setLuminosity(lightEvent.getLuminosity())
                        .build();
                builder.setPayload(lightSensor);
                break;

            case MOTION_SENSOR_EVENT:
                MotionSensorEvent motionEvent = (MotionSensorEvent) sensorEvent;
                MotionSensorAvro motionSensor = MotionSensorAvro.newBuilder()
                        .setLinkQuality(motionEvent.getLinkQuality())
                        .setMotion(motionEvent.getMotion())
                        .setVoltage(motionEvent.getVoltage())
                        .build();
                builder.setPayload(motionSensor);
                break;

            case SWITCH_SENSOR_EVENT:
                SwitchSensorEvent switchEvent = (SwitchSensorEvent) sensorEvent;
                SwitchSensorAvro switchSensor = SwitchSensorAvro.newBuilder()
                        .setState(switchEvent.getState())
                        .build();
                builder.setPayload(switchSensor);
                break;

            case TEMPERATURE_SENSOR_EVENT:
                TemperatureSensorEvent tempEvent = (TemperatureSensorEvent) sensorEvent;
                TemperatureSensorAvro tempSensor = TemperatureSensorAvro.newBuilder()
                        .setTemperatureC(tempEvent.getTemperatureC())
                        .setTemperatureF(tempEvent.getTemperatureF())
                        .build();
                builder.setPayload(tempSensor);
                break;

            default:
                throw new IllegalArgumentException("Unknown sensor event type: " + sensorEvent.getType());
        }

        return builder.build();
    }

    public HubEventAvro toAvro(HubEventModel hubEvent) {
        HubEventAvro.Builder builder = HubEventAvro.newBuilder()
                .setHubId(hubEvent.getHubId())
                .setTimestamp(hubEvent.getTimestamp().toEpochMilli());

        switch (hubEvent.getType()) {
            case DEVICE_ADDED:
                DeviceAddedEventModel addedEvent = (DeviceAddedEventModel) hubEvent;
                DeviceAddedEventAvro deviceAdded = DeviceAddedEventAvro.newBuilder()
                        .setId(addedEvent.getId())
                        .setType(DeviceTypeAvro.valueOf(addedEvent.getDeviceType().name()))
                        .build();
                builder.setPayload(deviceAdded);
                break;

            case DEVICE_REMOVED:
                DeviceRemovedEventModel removedEvent = (DeviceRemovedEventModel) hubEvent;
                DeviceRemovedEventAvro deviceRemoved = DeviceRemovedEventAvro.newBuilder()
                        .setId(removedEvent.getId())
                        .build();
                builder.setPayload(deviceRemoved);
                break;

            case SCENARIO_ADDED:
                ScenarioAddedEventModel scenarioAddedEvent = (ScenarioAddedEventModel) hubEvent;
                ScenarioAddedEventAvro scenarioAdded = ScenarioAddedEventAvro.newBuilder()
                        .setName(scenarioAddedEvent.getName())
                        .setConditions(scenarioAddedEvent.getConditions().stream()
                                .map(this::mapCondition)
                                .collect(Collectors.toList()))
                        .setActions(scenarioAddedEvent.getActions().stream()
                                .map(this::mapAction)
                                .collect(Collectors.toList()))
                        .build();
                builder.setPayload(scenarioAdded);
                break;

            case SCENARIO_REMOVED:
                ScenarioRemovedEventModel scenarioRemovedEvent = (ScenarioRemovedEventModel) hubEvent;
                ScenarioRemovedEventAvro scenarioRemoved = ScenarioRemovedEventAvro.newBuilder()
                        .setName(scenarioRemovedEvent.getName())
                        .build();
                builder.setPayload(scenarioRemoved);
                break;

            default:
                throw new IllegalArgumentException("Unknown hub event type: " + hubEvent.getType());
        }

        return builder.build();
    }

    private ScenarioConditionAvro mapCondition(ScenarioConditionModel condition) {
        ScenarioConditionAvro.Builder builder = ScenarioConditionAvro.newBuilder()
                .setSensorId(condition.getSensorId())
                .setType(ConditionTypeAvro.valueOf(condition.getType().name()))
                .setOperation(ConditionOperationAvro.valueOf(condition.getOperation().name()));
        if (condition.getType() == ConditionTypeModel.MOTION || condition.getType() == ConditionTypeModel.SWITCH) {
            builder.setValue(condition.getValue() != 0);
        } else {
            builder.setValue(condition.getValue());
        }
        return builder.build();
    }

    private DeviceActionAvro mapAction(DeviceActionModel action) {
        DeviceActionAvro.Builder builder = DeviceActionAvro.newBuilder()
                .setSensorId(action.getSensorId())
                .setType(ActionTypeAvro.valueOf(action.getType().name()));

        if (action.getValue() != null) {
            builder.setValue(action.getValue());
        }

        return builder.build();
    }

}
