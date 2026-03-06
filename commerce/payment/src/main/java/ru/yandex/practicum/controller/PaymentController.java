package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.dto.PaymentDto;
import ru.yandex.practicum.service.PaymentService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public PaymentDto payment(@RequestBody OrderDto orderDto) {
        return paymentService.payment(orderDto);
    }

    @PostMapping("/totalCost")
    public Double getTotalCost(@RequestBody OrderDto orderDto) {
        return paymentService.getTotalCost(orderDto);
    }

    @PostMapping("/productCost")
    public Double productCost(@RequestBody OrderDto orderDto) {
        return paymentService.productCost(orderDto);
    }

    @PostMapping("/refund")
    public void paymentSuccess(@RequestBody UUID paymentId) {
        paymentService.paymentSuccess(paymentId);
    }

    @PostMapping("/failed")
    public void paymentFailed(@RequestBody UUID paymentId) {
        paymentService.paymentFailed(paymentId);
    }

}
