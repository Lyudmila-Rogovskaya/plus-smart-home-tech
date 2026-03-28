package ru.yandex.practicum.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class DeliveryDto {

    private UUID deliveryId;

    @NotNull(message = "From address must not be null")
    private AddressDto fromAddress;

    @NotNull(message = "To address must not be null")
    private AddressDto toAddress;

    @NotNull(message = "Order ID must not be null")
    private UUID orderId;

    private DeliveryState deliveryState;

}
