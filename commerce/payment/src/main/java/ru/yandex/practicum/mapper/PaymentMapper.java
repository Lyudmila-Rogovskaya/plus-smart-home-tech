package ru.yandex.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.dto.PaymentDto;
import ru.yandex.practicum.entity.Payment;

@Component
public class PaymentMapper {

    public PaymentDto toDto(Payment payment) {
        PaymentDto dto = new PaymentDto();
        dto.setPaymentId(payment.getId());
        dto.setTotalPayment(payment.getTotalPayment());
        dto.setDeliveryTotal(payment.getDeliveryTotal());
        dto.setFeeTotal(payment.getFeeTotal());
        return dto;
    }

    public Payment toEntity(PaymentDto dto) {
        Payment payment = new Payment();
        payment.setId(dto.getPaymentId());
        payment.setTotalPayment(dto.getTotalPayment());
        payment.setDeliveryTotal(dto.getDeliveryTotal());
        payment.setFeeTotal(dto.getFeeTotal());
        return payment;
    }

}
