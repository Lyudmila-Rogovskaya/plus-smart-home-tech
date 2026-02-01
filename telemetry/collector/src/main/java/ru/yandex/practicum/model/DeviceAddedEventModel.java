package ru.yandex.practicum.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
@JsonTypeName("DEVICE_ADDED")
public class DeviceAddedEventModel extends HubEventModel {

    @NotBlank
    private String id;

    @NotNull
    private DeviceTypeModel deviceType;

    @Override
    public HubEventType getType() {
        return HubEventType.DEVICE_ADDED;
    }

}
