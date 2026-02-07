package ru.yandex.practicum.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@ToString(callSuper = true)
@JsonTypeName("SCENARIO_REMOVED")
public class ScenarioRemovedEventModel extends HubEventModel {

    @NotBlank
    private String name;

    @Override
    public HubEventType getType() {
        return HubEventType.SCENARIO_REMOVED;
    }

}