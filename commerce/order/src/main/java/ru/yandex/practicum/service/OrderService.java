package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.*;
import ru.yandex.practicum.embedded.AddressEmbeddable;
import ru.yandex.practicum.entity.Order;
import ru.yandex.practicum.entity.OrderProduct;
import ru.yandex.practicum.exception.NoOrderFoundException;
import ru.yandex.practicum.exception.NotAuthorizedUserException;
import ru.yandex.practicum.feign.DeliveryClient;
import ru.yandex.practicum.feign.PaymentClient;
import ru.yandex.practicum.feign.WarehouseClient;
import ru.yandex.practicum.mapper.OrderMapper;
import ru.yandex.practicum.repository.OrderProductRepository;
import ru.yandex.practicum.repository.OrderRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    private final OrderMapper orderMapper;
    private final WarehouseClient warehouseClient;
    private final PaymentClient paymentClient;
    private final DeliveryClient deliveryClient;

    @Transactional(readOnly = true)
    public List<OrderDto> getClientOrders(String username) {
        if (username == null || username.isBlank()) {
            throw new NotAuthorizedUserException("Username must not be empty");
        }
        return orderRepository.findByUsername(username).stream()
                .map(orderMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderDto createNewOrder(CreateNewOrderRequest request) {
        String username = request.getUsername();
        if (username == null || username.isBlank()) {
            throw new NotAuthorizedUserException("Username must not be empty");
        }

        ShoppingCartDto cart = request.getShoppingCart();
        AssemblyProductsForOrderRequest assemblyRequest = new AssemblyProductsForOrderRequest();
        assemblyRequest.setProducts(cart.getProducts());
        assemblyRequest.setOrderId(null);

        BookedProductsDto booked;
        try {
            booked = warehouseClient.assemblyProductsForOrder(assemblyRequest);
        } catch (Exception e) {
            log.error("Failed to assemble products for order", e);
            throw new IllegalArgumentException("No specified product in warehouse or insufficient quantity", e);
        }

        Order order = new Order();
        order.setUsername(username);
        order.setShoppingCartId(cart.getShoppingCartId());
        order.setState(OrderState.NEW);
        order.setDeliveryWeight(booked.getDeliveryWeight());
        order.setDeliveryVolume(booked.getDeliveryVolume());
        order.setFragile(booked.getFragile());
        order.setDeliveryAddress(mapAddress(request.getDeliveryAddress()));

        order = orderRepository.save(order);

        List<OrderProduct> orderProducts = new ArrayList<>();
        for (Map.Entry<UUID, Long> entry : cart.getProducts().entrySet()) {
            OrderProduct op = new OrderProduct();
            op.setOrder(order);
            op.setProductId(entry.getKey());
            op.setQuantity(entry.getValue());
            orderProducts.add(op);
        }
        orderProductRepository.saveAll(orderProducts);
        order.setProducts(orderProducts);

        log.info("Created new order with id: {}", order.getId());
        return orderMapper.toDto(order);
    }

    @Transactional
    public OrderDto productReturn(ProductReturnRequest request) {
        Order order = findOrderById(request.getOrderId());
        warehouseClient.acceptReturn(request.getProducts());
        order.setState(OrderState.PRODUCT_RETURNED);
        order = orderRepository.save(order);
        log.info("Order {} returned", order.getId());
        return orderMapper.toDto(order);
    }

    @Transactional
    public OrderDto payment(UUID orderId) {
        Order order = findOrderById(orderId);
        OrderDto dto = orderMapper.toDto(order);
        PaymentDto payment = paymentClient.payment(dto);
        order.setPaymentId(payment.getPaymentId());
        order.setState(OrderState.PAID);
        order = orderRepository.save(order);
        log.info("Order {} paid, paymentId: {}", orderId, payment.getPaymentId());
        return orderMapper.toDto(order);
    }

    @Transactional
    public OrderDto paymentFailed(UUID orderId) {
        Order order = findOrderById(orderId);
        order.setState(OrderState.PAYMENT_FAILED);
        order = orderRepository.save(order);
        log.info("Order {} payment failed", orderId);
        return orderMapper.toDto(order);
    }

    @Transactional
    public OrderDto delivery(UUID orderId) {
        Order order = findOrderById(orderId);
        order.setState(OrderState.DELIVERED);
        order = orderRepository.save(order);
        log.info("Order {} delivered", orderId);
        return orderMapper.toDto(order);
    }

    @Transactional
    public OrderDto deliveryFailed(UUID orderId) {
        Order order = findOrderById(orderId);
        order.setState(OrderState.DELIVERY_FAILED);
        order = orderRepository.save(order);
        log.info("Order {} delivery failed", orderId);
        return orderMapper.toDto(order);
    }

    @Transactional
    public OrderDto assembly(UUID orderId) {
        Order order = findOrderById(orderId);
        Map<UUID, Long> products = order.getProducts().stream()
                .collect(Collectors.toMap(OrderProduct::getProductId, OrderProduct::getQuantity));

        AssemblyProductsForOrderRequest assemblyRequest = new AssemblyProductsForOrderRequest();
        assemblyRequest.setProducts(products);
        assemblyRequest.setOrderId(orderId);

        warehouseClient.assemblyProductsForOrder(assemblyRequest);
        order.setState(OrderState.ASSEMBLED);
        order = orderRepository.save(order);

        AddressDto warehouseAddress = warehouseClient.getWarehouseAddress();
        AddressDto deliveryAddress = orderMapper.toAddressDto(order.getDeliveryAddress());

        DeliveryDto deliveryDto = new DeliveryDto();
        deliveryDto.setFromAddress(warehouseAddress);
        deliveryDto.setToAddress(deliveryAddress);
        deliveryDto.setOrderId(orderId);

        DeliveryDto createdDelivery = deliveryClient.planDelivery(deliveryDto);
        order.setDeliveryId(createdDelivery.getDeliveryId());
        order = orderRepository.save(order);

        log.info("Order {} assembled and delivery planned with id {}", orderId, createdDelivery.getDeliveryId());
        return orderMapper.toDto(order);
    }

    @Transactional
    public OrderDto assemblyFailed(UUID orderId) {
        Order order = findOrderById(orderId);
        order.setState(OrderState.ASSEMBLY_FAILED);
        order = orderRepository.save(order);
        log.info("Order {} assembly failed", orderId);
        return orderMapper.toDto(order);
    }

    @Transactional
    public OrderDto complete(UUID orderId) {
        Order order = findOrderById(orderId);
        order.setState(OrderState.COMPLETED);
        order = orderRepository.save(order);
        log.info("Order {} completed", orderId);
        return orderMapper.toDto(order);
    }

    @Transactional
    public OrderDto calculateTotalCost(UUID orderId) {
        Order order = findOrderById(orderId);
        OrderDto dto = orderMapper.toDto(order);
        Double total = paymentClient.getTotalCost(dto);
        order.setTotalPrice(total);
        order = orderRepository.save(order);
        log.info("Calculated total cost for order {}: {}", orderId, total);
        return orderMapper.toDto(order);
    }

    @Transactional
    public OrderDto calculateDeliveryCost(UUID orderId) {
        Order order = findOrderById(orderId);
        OrderDto dto = orderMapper.toDto(order);
        Double deliveryCost = deliveryClient.deliveryCost(dto);
        order.setDeliveryPrice(deliveryCost);
        order = orderRepository.save(order);
        log.info("Calculated delivery cost for order {}: {}", orderId, deliveryCost);
        return orderMapper.toDto(order);
    }

    private Order findOrderById(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NoOrderFoundException("Order not found with id: " + orderId));
    }

    private AddressEmbeddable mapAddress(AddressDto dto) {
        if (dto == null) return null;
        AddressEmbeddable addr = new AddressEmbeddable();
        addr.setCountry(dto.getCountry());
        addr.setCity(dto.getCity());
        addr.setStreet(dto.getStreet());
        addr.setHouse(dto.getHouse());
        addr.setFlat(dto.getFlat());
        return addr;
    }

}
