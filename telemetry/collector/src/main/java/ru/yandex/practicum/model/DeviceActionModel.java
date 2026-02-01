package ru.yandex.practicum.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

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
