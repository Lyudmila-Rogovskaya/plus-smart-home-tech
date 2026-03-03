package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.*;
import ru.yandex.practicum.entity.Delivery;
import ru.yandex.practicum.exception.NoDeliveryFoundException;
import ru.yandex.practicum.feign.OrderClient;
import ru.yandex.practicum.feign.WarehouseClient;
import ru.yandex.practicum.mapper.DeliveryMapper;
import ru.yandex.practicum.repository.DeliveryRepository;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final DeliveryMapper deliveryMapper;
    private final WarehouseClient warehouseClient;
    private final OrderClient orderClient;

    private static final double BASE_RATE = 5.0;

    @Transactional
    public DeliveryDto planDelivery(DeliveryDto deliveryDto) {
        Delivery delivery = deliveryMapper.toEntity(deliveryDto);
        if (delivery.getDeliveryState() == null) {
            delivery.setDeliveryState(DeliveryState.CREATED);
        }
        delivery = deliveryRepository.save(delivery);
        log.info("Delivery planned with id: {}, orderId: {}", delivery.getId(), delivery.getOrderId());
        return deliveryMapper.toDto(delivery);
    }

    @Transactional(readOnly = true)
    public Double deliveryCost(OrderDto orderDto) {

        AddressDto warehouseAddress = warehouseClient.getWarehouseAddress();

        AddressDto deliveryAddress = orderDto.getDeliveryAddress();
        if (deliveryAddress == null) {
            throw new IllegalArgumentException("Delivery address is missing in order");
        }

        double cost = BASE_RATE;

        String warehouseCountry = warehouseAddress.getCountry();
        if (warehouseCountry != null) {
            if (warehouseCountry.contains("ADDRESS_1")) {
                cost = BASE_RATE * 1 + BASE_RATE;
                int factor = warehouseCountry.contains("ADDRESS_2") ? 2 : 1;
                cost = BASE_RATE * factor + BASE_RATE;
            } else if (warehouseCountry.contains("ADDRESS_2")) {
                cost = BASE_RATE * 2 + BASE_RATE;
            }
        }

        if (orderDto.getFragile() != null && orderDto.getFragile()) {
            cost += cost * 0.2;
        }

        if (orderDto.getDeliveryWeight() != null) {
            cost += orderDto.getDeliveryWeight() * 0.3;
        }

        if (orderDto.getDeliveryVolume() != null) {
            cost += orderDto.getDeliveryVolume() * 0.2;
        }

        if (warehouseAddress.getStreet() != null && deliveryAddress.getStreet() != null
                && !warehouseAddress.getStreet().equals(deliveryAddress.getStreet())) {
            cost += cost * 0.2;
        }

        log.debug("Calculated delivery cost for order {}: {}", orderDto.getOrderId(), cost);
        return cost;
    }

    @Transactional
    public void deliverySuccessful(UUID orderId) {
        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NoDeliveryFoundException("Delivery not found for order: " + orderId));
        delivery.setDeliveryState(DeliveryState.DELIVERED);
        deliveryRepository.save(delivery);
        orderClient.delivery(orderId);
        log.info("Delivery for order {} succeeded", orderId);
    }

    @Transactional
    public void deliveryFailed(UUID orderId) {
        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NoDeliveryFoundException("Delivery not found for order: " + orderId));
        delivery.setDeliveryState(DeliveryState.FAILED);
        deliveryRepository.save(delivery);
        orderClient.deliveryFailed(orderId);
        log.info("Delivery for order {} failed", orderId);
    }

    @Transactional
    public void deliveryPicked(UUID orderId) {
        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NoDeliveryFoundException("Delivery not found for order: " + orderId));
        delivery.setDeliveryState(DeliveryState.IN_PROGRESS);
        deliveryRepository.save(delivery);

        ShippedToDeliveryRequest request = new ShippedToDeliveryRequest();
        request.setOrderId(orderId);
        request.setDeliveryId(delivery.getId());
        warehouseClient.shippedToDelivery(request);

        orderClient.assembly(orderId);
        log.info("Delivery for order {} picked (IN_PROGRESS)", orderId);
    }

}
