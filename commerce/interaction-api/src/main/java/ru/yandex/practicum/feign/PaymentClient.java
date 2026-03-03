package ru.yandex.practicum.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.dto.PaymentDto;

import java.util.UUID;

@FeignClient(name = "payment", path = "/api/v1/payment")
public interface PaymentClient {

    @PostMapping
    PaymentDto payment(@RequestBody OrderDto orderDto);

    @PostMapping("/totalCost")
    Double getTotalCost(@RequestBody OrderDto orderDto);

    @PostMapping("/productCost")
    Double productCost(@RequestBody OrderDto orderDto);

    @PostMapping("/refund")
    void paymentSuccess(@RequestBody UUID paymentId);

    @PostMapping("/failed")
    void paymentFailed(@RequestBody UUID paymentId);

}
