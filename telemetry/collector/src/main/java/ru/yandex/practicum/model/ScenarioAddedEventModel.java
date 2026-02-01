package ru.yandex.practicum.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString(callSuper = true)
@JsonTypeName("SCENARIO_ADDED")
public class ScenarioAddedEventModel extends HubEventModel {

    @NotBlank
    private String name;

    @NotNull
    private List<ScenarioConditionModel> conditions;

    @NotNull
    private List<DeviceActionModel> actions;

    @Override
    public HubEventType getType() {
        return HubEventType.SCENARIO_ADDED;
    }

}
