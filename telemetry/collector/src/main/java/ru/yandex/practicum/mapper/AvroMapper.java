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
                        .setLinkQuality(lightEvent.getLinkQuality() != null ? lightEvent.getLinkQuality() : 0)
                        .setLuminosity(lightEvent.getLuminosity() != null ? lightEvent.getLuminosity() : 0)
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

//        if (sensorEvent.getType() == SensorEventType.CLIMATE_SENSOR_EVENT) {
//            ClimateSensorEventModel event = (ClimateSensorEventModel) sensorEvent;
//            ClimateSensorAvro climateSensor = ClimateSensorAvro.newBuilder()
//                    .setTemperatureC(event.getTemperatureC())
//                    .setHumidity(event.getHumidity())
//                    .setCo2Level(event.getCo2Level())
//                    .build();
//            builder.setPayload(climateSensor);
//        } else if (sensorEvent.getType() == SensorEventType.LIGHT_SENSOR_EVENT) {
//            LightSensorEvent event = (LightSensorEvent) sensorEvent;
//            LightSensorAvro lightSensor = LightSensorAvro.newBuilder()
//                    .setLinkQuality(event.getLinkQuality() != null ? event.getLinkQuality() : 0)
//                    .setLuminosity(event.getLuminosity() != null ? event.getLuminosity() : 0)
//                    .build();
//            builder.setPayload(lightSensor);
//        } else if (sensorEvent.getType() == SensorEventType.MOTION_SENSOR_EVENT) {
//            MotionSensorEvent event = (MotionSensorEvent) sensorEvent;
//            MotionSensorAvro motionSensor = MotionSensorAvro.newBuilder()
//                    .setLinkQuality(event.getLinkQuality())
//                    .setMotion(event.getMotion())
//                    .setVoltage(event.getVoltage())
//                    .build();
//            builder.setPayload(motionSensor);
//        } else if (sensorEvent.getType() == SensorEventType.SWITCH_SENSOR_EVENT) {
//            SwitchSensorEvent event = (SwitchSensorEvent) sensorEvent;
//            SwitchSensorAvro switchSensor = SwitchSensorAvro.newBuilder()
//                    .setState(event.getState())
//                    .build();
//            builder.setPayload(switchSensor);
//        } else if (sensorEvent.getType() == SensorEventType.TEMPERATURE_SENSOR_EVENT) {
//            TemperatureSensorEvent event = (TemperatureSensorEvent) sensorEvent;
//            TemperatureSensorAvro temperatureSensor = TemperatureSensorAvro.newBuilder()
//                    .setTemperatureC(event.getTemperatureC())
//                    .setTemperatureF(event.getTemperatureF())
//                    .build();
//            builder.setPayload(temperatureSensor);
//        } else {
//            throw new IllegalArgumentException("Unknown sensor event type: " + sensorEvent.getType());
//        }
//
//        return builder.build();

    public HubEventAvro toAvro(HubEventModel hubEvent) {
        HubEventAvro.Builder builder = HubEventAvro.newBuilder()
                .setHubId(hubEvent.getHubId())
                .setTimestamp(hubEvent.getTimestamp().toEpochMilli());

        if (hubEvent.getType() == HubEventType.DEVICE_ADDED) {
            DeviceAddedEventModel event = (DeviceAddedEventModel) hubEvent;
            DeviceAddedEventAvro deviceAdded = DeviceAddedEventAvro.newBuilder()
                    .setId(event.getId())
                    .setType(DeviceTypeAvro.valueOf(event.getDeviceType().name()))
                    .build();
            builder.setPayload(deviceAdded);
        } else if (hubEvent.getType() == HubEventType.DEVICE_REMOVED) {
            DeviceRemovedEventModel event = (DeviceRemovedEventModel) hubEvent;
            DeviceRemovedEventAvro deviceRemoved = DeviceRemovedEventAvro.newBuilder()
                    .setId(event.getId())
                    .build();
            builder.setPayload(deviceRemoved);
        } else if (hubEvent.getType() == HubEventType.SCENARIO_ADDED) {
            ScenarioAddedEventModel event = (ScenarioAddedEventModel) hubEvent;
            ScenarioAddedEventAvro scenarioAdded = ScenarioAddedEventAvro.newBuilder()
                    .setName(event.getName())
                    .setConditions(event.getConditions().stream()
                            .map(this::mapCondition)
                            .collect(Collectors.toList()))
                    .setActions(event.getActions().stream()
                            .map(this::mapAction)
                            .collect(Collectors.toList()))
                    .build();
            builder.setPayload(scenarioAdded);
        } else if (hubEvent.getType() == HubEventType.SCENARIO_REMOVED) {
            ScenarioRemovedEventModel event = (ScenarioRemovedEventModel) hubEvent;
            ScenarioRemovedEventAvro scenarioRemoved = ScenarioRemovedEventAvro.newBuilder()
                    .setName(event.getName())
                    .build();
            builder.setPayload(scenarioRemoved);
        } else {
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
