package ru.yandex.practicum.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.*;
import ru.yandex.practicum.entity.Delivery;
import ru.yandex.practicum.exception.ExternalServiceException;
import ru.yandex.practicum.exception.NoDeliveryFoundException;
import ru.yandex.practicum.feign.OrderClient;
import ru.yandex.practicum.feign.WarehouseClient;
import ru.yandex.practicum.mapper.DeliveryMapper;
import ru.yandex.practicum.repository.DeliveryRepository;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final DeliveryMapper deliveryMapper;
    private final WarehouseClient warehouseClient;
    private final OrderClient orderClient;

    private static final BigDecimal BASE_RATE = BigDecimal.valueOf(5.0);
    private static final BigDecimal FRAGILE_MULTIPLIER = BigDecimal.valueOf(0.2);
    private static final BigDecimal WEIGHT_MULTIPLIER = BigDecimal.valueOf(0.3);
    private static final BigDecimal VOLUME_MULTIPLIER = BigDecimal.valueOf(0.2);
    private static final BigDecimal STREET_MULTIPLIER = BigDecimal.valueOf(0.2);

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

        try {
            orderClient.getOrder(orderDto.getOrderId());
        } catch (FeignException e) {
            log.error("Failed to verify order existence: orderId={}, status={}", orderDto.getOrderId(), e.status(), e);
            throw new ExternalServiceException("order", "Order service unavailable or order not found", e);
        }

        AddressDto warehouseAddress;
        try {
            warehouseAddress = warehouseClient.getWarehouseAddress();
        } catch (FeignException e) {
            log.error("Failed to get warehouse address: {}", e.getMessage(), e);
            throw new ExternalServiceException("warehouse", "Warehouse service unavailable", e);
        }

        AddressDto deliveryAddress = orderDto.getDeliveryAddress();
        if (deliveryAddress == null) {
            throw new IllegalArgumentException("Delivery address is missing in order");
        }

        BigDecimal cost = BASE_RATE;

        String warehouseCountry = warehouseAddress.getCountry();
        if (warehouseCountry != null) {
            if (warehouseCountry.contains("ADDRESS_1")) {
                cost = BASE_RATE.multiply(BigDecimal.ONE).add(BASE_RATE);
                int factor = warehouseCountry.contains("ADDRESS_2") ? 2 : 1;
                cost = BASE_RATE.multiply(BigDecimal.valueOf(factor)).add(BASE_RATE);
            } else if (warehouseCountry.contains("ADDRESS_2")) {
                cost = BASE_RATE.multiply(BigDecimal.valueOf(2)).add(BASE_RATE);
            }
        }

        if (orderDto.getFragile() != null && orderDto.getFragile()) {
            cost = cost.add(cost.multiply(FRAGILE_MULTIPLIER));
        }

        if (orderDto.getDeliveryWeight() != null) {
            cost = cost.add(BigDecimal.valueOf(orderDto.getDeliveryWeight()).multiply(WEIGHT_MULTIPLIER));
        }

        if (orderDto.getDeliveryVolume() != null) {
            cost = cost.add(BigDecimal.valueOf(orderDto.getDeliveryVolume()).multiply(VOLUME_MULTIPLIER));
        }

        if (warehouseAddress.getStreet() != null && deliveryAddress.getStreet() != null
                && !warehouseAddress.getStreet().equals(deliveryAddress.getStreet())) {
            cost = cost.add(cost.multiply(STREET_MULTIPLIER));
        }

        log.debug("Calculated delivery cost for order {}: {}", orderDto.getOrderId(), cost);
        return cost.doubleValue();
    }

    @Transactional
    public void deliverySuccessful(UUID orderId) {
        Delivery delivery = findDeliveryByOrderId(orderId);

        delivery.setDeliveryState(DeliveryState.DELIVERED);
        deliveryRepository.save(delivery);

        try {
            orderClient.delivery(orderId);
        } catch (FeignException e) {
            log.error("Failed to notify order service about successful delivery: orderId={}, status={}",
                    orderId, e.status(), e);
            throw new ExternalServiceException("order", "Order service unavailable", e);
        }

        log.info("Delivery for order {} succeeded", orderId);
    }

    @Transactional
    public void deliveryFailed(UUID orderId) {
        Delivery delivery = findDeliveryByOrderId(orderId);

        delivery.setDeliveryState(DeliveryState.FAILED);
        deliveryRepository.save(delivery);

        try {
            orderClient.deliveryFailed(orderId);
        } catch (FeignException e) {
            log.error("Failed to notify order service about failed delivery: orderId={}, status={}",
                    orderId, e.status(), e);
            throw new ExternalServiceException("order", "Order service unavailable", e);
        }

        log.info("Delivery for order {} failed", orderId);
    }

    @Transactional
    public void deliveryPicked(UUID orderId) {
        Delivery delivery = findDeliveryByOrderId(orderId);

        delivery.setDeliveryState(DeliveryState.IN_PROGRESS);
        deliveryRepository.save(delivery);

        ShippedToDeliveryRequest request = new ShippedToDeliveryRequest();
        request.setOrderId(orderId);
        request.setDeliveryId(delivery.getId());

        try {
            warehouseClient.shippedToDelivery(request);
        } catch (FeignException e) {
            log.error("Failed to notify warehouse about picked delivery: orderId={}, deliveryId={}, status={}",
                    orderId, delivery.getId(), e.status(), e);
            throw new ExternalServiceException("warehouse", "Warehouse service unavailable", e);
        }

        try {
            orderClient.assembly(orderId);
        } catch (FeignException e) {
            log.error("Failed to notify order service about assembly: orderId={}, status={}",
                    orderId, e.status(), e);
            throw new ExternalServiceException("order", "Order service unavailable", e);
        }

        log.info("Delivery for order {} picked (IN_PROGRESS)", orderId);
    }

    private Delivery findDeliveryByOrderId(UUID orderId) {
        return deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NoDeliveryFoundException("Delivery not found for order: " + orderId));
    }

}
