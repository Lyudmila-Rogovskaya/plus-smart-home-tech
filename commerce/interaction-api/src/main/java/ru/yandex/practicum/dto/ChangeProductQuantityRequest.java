package ru.yandex.practicum.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ChangeProductQuantityRequest {

    @NotNull
    private UUID productId;

    @NotNull
    @Min(1)
    private Long newQuantity;

}
