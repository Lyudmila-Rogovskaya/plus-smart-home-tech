package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.dto.PaymentDto;
import ru.yandex.practicum.dto.PaymentState;
import ru.yandex.practicum.dto.ProductDto;
import ru.yandex.practicum.entity.Payment;
import ru.yandex.practicum.exception.NoOrderFoundException;
import ru.yandex.practicum.exception.NotEnoughInfoInOrderToCalculateException;
import ru.yandex.practicum.feign.OrderClient;
import ru.yandex.practicum.feign.ShoppingStoreClient;
import ru.yandex.practicum.mapper.PaymentMapper;
import ru.yandex.practicum.repository.PaymentRepository;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final ShoppingStoreClient shoppingStoreClient;
    private final OrderClient orderClient;

    @Transactional(readOnly = true)
    public Double productCost(OrderDto orderDto) {
        if (orderDto.getProducts() == null || orderDto.getProducts().isEmpty()) {
            throw new NotEnoughInfoInOrderToCalculateException("Order has no products");
        }

        double total = 0.0;
        for (var entry : orderDto.getProducts().entrySet()) {
            UUID productId = entry.getKey();
            Long quantity = entry.getValue();

            ProductDto product = shoppingStoreClient.getProduct(productId.toString());
            if (product == null || product.getPrice() == null) {
                throw new NotEnoughInfoInOrderToCalculateException("Product price not found for id: " + productId);
            }
            total += product.getPrice() * quantity;
        }
        log.debug("Calculated product cost for order {}: {}", orderDto.getOrderId(), total);
        return total;
    }

    @Transactional(readOnly = true)
    public Double getTotalCost(OrderDto orderDto) {
        if (orderDto.getDeliveryPrice() == null) {
            throw new NotEnoughInfoInOrderToCalculateException("Delivery price is not set");
        }

        Double productCost = productCost(orderDto);
        Double vat = productCost * 0.1;
        Double total = productCost + vat + orderDto.getDeliveryPrice();
        log.debug("Calculated total cost for order {}: {}", orderDto.getOrderId(), total);
        return total;
    }

    @Transactional
    public PaymentDto payment(OrderDto orderDto) {

        Double productCost = productCost(orderDto);
        Double vat = productCost * 0.1;
        Double total = productCost + vat + orderDto.getDeliveryPrice();

        Payment payment = new Payment();
        payment.setOrderId(orderDto.getOrderId());
        payment.setTotalPayment(total);
        payment.setDeliveryTotal(orderDto.getDeliveryPrice());
        payment.setFeeTotal(vat);
        payment.setStatus(PaymentState.PENDING);

        payment = paymentRepository.save(payment);
        log.info("Payment created with id: {}, orderId: {}", payment.getId(), orderDto.getOrderId());

        return paymentMapper.toDto(payment);
    }

    @Transactional
    public void paymentSuccess(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NoOrderFoundException("Payment not found with id: " + paymentId));

        payment.setStatus(PaymentState.SUCCESS);
        paymentRepository.save(payment);

        orderClient.payment(payment.getOrderId());
        log.info("Payment {} succeeded, order {} notified", paymentId, payment.getOrderId());
    }

    @Transactional
    public void paymentFailed(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NoOrderFoundException("Payment not found with id: " + paymentId));

        payment.setStatus(PaymentState.FAILED);
        paymentRepository.save(payment);

        orderClient.paymentFailed(payment.getOrderId());
        log.info("Payment {} failed, order {} notified", paymentId, payment.getOrderId());
    }

}
