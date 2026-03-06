package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.DeliveryDto;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.service.DeliveryService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/delivery")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @PutMapping
    public DeliveryDto planDelivery(@Valid @RequestBody DeliveryDto deliveryDto) {
        return deliveryService.planDelivery(deliveryDto);
    }

    @PostMapping("/successful")
    public void deliverySuccessful(@Valid @RequestBody UUID orderId) {
        deliveryService.deliverySuccessful(orderId);
    }

    @PostMapping("/picked")
    public void deliveryPicked(@Valid @RequestBody UUID orderId) {
        deliveryService.deliveryPicked(orderId);
    }

    @PostMapping("/failed")
    public void deliveryFailed(@Valid @RequestBody UUID orderId) {
        deliveryService.deliveryFailed(orderId);
    }

    @PostMapping("/cost")
    public Double deliveryCost(@Valid @RequestBody OrderDto orderDto) {
        return deliveryService.deliveryCost(orderDto);
    }

}
