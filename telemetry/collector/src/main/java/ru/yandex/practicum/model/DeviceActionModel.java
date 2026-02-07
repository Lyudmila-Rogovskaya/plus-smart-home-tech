package ru.yandex.practicum.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@ToString
public class DeviceActionModel {

    @NotBlank
    private String sensorId;

    @NotNull
    private ActionTypeModel type;

    private Integer value;
}
