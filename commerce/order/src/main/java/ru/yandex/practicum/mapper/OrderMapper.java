package ru.yandex.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.dto.AddressDto;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.embedded.AddressEmbeddable;
import ru.yandex.practicum.entity.Order;
import ru.yandex.practicum.entity.OrderProduct;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    public OrderDto toDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.setOrderId(order.getId());
        dto.setShoppingCartId(order.getShoppingCartId());
        dto.setPaymentId(order.getPaymentId());
        dto.setDeliveryId(order.getDeliveryId());
        dto.setState(order.getState());
        dto.setDeliveryWeight(order.getDeliveryWeight());
        dto.setDeliveryVolume(order.getDeliveryVolume());
        dto.setFragile(order.getFragile());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setDeliveryPrice(order.getDeliveryPrice());
        dto.setProductPrice(order.getProductPrice());

        Map<UUID, Long> productsMap = order.getProducts().stream()
                .collect(Collectors.toMap(OrderProduct::getProductId, OrderProduct::getQuantity));
        dto.setProducts(productsMap);

        if (order.getDeliveryAddress() != null) {
            AddressDto addressDto = new AddressDto();
            addressDto.setCountry(order.getDeliveryAddress().getCountry());
            addressDto.setCity(order.getDeliveryAddress().getCity());
            addressDto.setStreet(order.getDeliveryAddress().getStreet());
            addressDto.setHouse(order.getDeliveryAddress().getHouse());
            addressDto.setFlat(order.getDeliveryAddress().getFlat());
            dto.setDeliveryAddress(addressDto);
        }
        return dto;
    }

    public Order toEntity(OrderDto dto, String username) {
        Order order = new Order();
        order.setId(dto.getOrderId());
        order.setShoppingCartId(dto.getShoppingCartId());
        order.setPaymentId(dto.getPaymentId());
        order.setDeliveryId(dto.getDeliveryId());
        order.setState(dto.getState());
        order.setDeliveryWeight(dto.getDeliveryWeight());
        order.setDeliveryVolume(dto.getDeliveryVolume());
        order.setFragile(dto.getFragile());
        order.setTotalPrice(dto.getTotalPrice());
        order.setDeliveryPrice(dto.getDeliveryPrice());
        order.setProductPrice(dto.getProductPrice());
        order.setUsername(username);

        if (dto.getDeliveryAddress() != null) {
            AddressEmbeddable addr = new AddressEmbeddable();
            addr.setCountry(dto.getDeliveryAddress().getCountry());
            addr.setCity(dto.getDeliveryAddress().getCity());
            addr.setStreet(dto.getDeliveryAddress().getStreet());
            addr.setHouse(dto.getDeliveryAddress().getHouse());
            addr.setFlat(dto.getDeliveryAddress().getFlat());
            order.setDeliveryAddress(addr);
        }

        return order;
    }

    public AddressDto toAddressDto(AddressEmbeddable embeddable) {
        if (embeddable == null) return null;
        AddressDto dto = new AddressDto();
        dto.setCountry(embeddable.getCountry());
        dto.setCity(embeddable.getCity());
        dto.setStreet(embeddable.getStreet());
        dto.setHouse(embeddable.getHouse());
        dto.setFlat(embeddable.getFlat());
        return dto;
    }

}
