package ru.yandex.practicum.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class DeliveryDto {

    private UUID deliveryId;
    private AddressDto fromAddress;
    private AddressDto toAddress;
    private UUID orderId;
    private DeliveryState deliveryState;

}
