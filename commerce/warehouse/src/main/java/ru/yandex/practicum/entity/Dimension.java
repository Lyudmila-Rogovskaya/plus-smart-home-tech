package ru.yandex.practicum.entity;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class Dimension {

    private Double width;
    private Double height;
    private Double depth;

}
