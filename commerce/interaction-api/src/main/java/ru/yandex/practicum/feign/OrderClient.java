package ru.yandex.practicum.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.dto.ProductReturnRequest;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "order", path = "/api/v1/order")
public interface OrderClient {

    @PostMapping("/payment")
    OrderDto payment(@RequestBody UUID orderId);

    @PostMapping("/payment/failed")
    OrderDto paymentFailed(@RequestBody UUID orderId);

    @PostMapping("/delivery")
    OrderDto delivery(@RequestBody UUID orderId);

    @PostMapping("/delivery/failed")
    OrderDto deliveryFailed(@RequestBody UUID orderId);

    @PostMapping("/assembly")
    OrderDto assembly(@RequestBody UUID orderId);

    @PostMapping("/assembly/failed")
    OrderDto assemblyFailed(@RequestBody UUID orderId);

    @PostMapping("/completed")
    OrderDto complete(@RequestBody UUID orderId);

    @PostMapping("/calculate/total")
    OrderDto calculateTotalCost(@RequestBody UUID orderId);

    @PostMapping("/calculate/delivery")
    OrderDto calculateDeliveryCost(@RequestBody UUID orderId);

    @PostMapping("/return")
    OrderDto productReturn(@RequestBody ProductReturnRequest request);

    @GetMapping
    List<OrderDto> getClientOrders(@RequestParam String username);

}
