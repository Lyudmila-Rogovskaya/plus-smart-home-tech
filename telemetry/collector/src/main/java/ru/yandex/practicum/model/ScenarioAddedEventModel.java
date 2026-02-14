package ru.yandex.practicum.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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
