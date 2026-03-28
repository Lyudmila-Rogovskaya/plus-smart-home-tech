package ru.yandex.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.dto.AddressDto;
import ru.yandex.practicum.dto.DeliveryDto;
import ru.yandex.practicum.embedded.AddressEmbeddable;
import ru.yandex.practicum.entity.Delivery;

@Component
public class DeliveryMapper {

    public DeliveryDto toDto(Delivery delivery) {
        if (delivery == null) return null;

        DeliveryDto dto = new DeliveryDto();
        dto.setDeliveryId(delivery.getId());
        dto.setOrderId(delivery.getOrderId());
        dto.setDeliveryState(delivery.getDeliveryState());

        if (delivery.getFromAddress() != null) {
            AddressDto from = new AddressDto();
            from.setCountry(delivery.getFromAddress().getCountry());
            from.setCity(delivery.getFromAddress().getCity());
            from.setStreet(delivery.getFromAddress().getStreet());
            from.setHouse(delivery.getFromAddress().getHouse());
            from.setFlat(delivery.getFromAddress().getFlat());
            dto.setFromAddress(from);
        }

        if (delivery.getToAddress() != null) {
            AddressDto to = new AddressDto();
            to.setCountry(delivery.getToAddress().getCountry());
            to.setCity(delivery.getToAddress().getCity());
            to.setStreet(delivery.getToAddress().getStreet());
            to.setHouse(delivery.getToAddress().getHouse());
            to.setFlat(delivery.getToAddress().getFlat());
            dto.setToAddress(to);
        }

        return dto;
    }

    public Delivery toEntity(DeliveryDto dto) {
        if (dto == null) return null;

        Delivery delivery = new Delivery();
        delivery.setId(dto.getDeliveryId());
        delivery.setOrderId(dto.getOrderId());
        delivery.setDeliveryState(dto.getDeliveryState());

        if (dto.getFromAddress() != null) {
            AddressEmbeddable from = new AddressEmbeddable();
            from.setCountry(dto.getFromAddress().getCountry());
            from.setCity(dto.getFromAddress().getCity());
            from.setStreet(dto.getFromAddress().getStreet());
            from.setHouse(dto.getFromAddress().getHouse());
            from.setFlat(dto.getFromAddress().getFlat());
            delivery.setFromAddress(from);
        }

        if (dto.getToAddress() != null) {
            AddressEmbeddable to = new AddressEmbeddable();
            to.setCountry(dto.getToAddress().getCountry());
            to.setCity(dto.getToAddress().getCity());
            to.setStreet(dto.getToAddress().getStreet());
            to.setHouse(dto.getToAddress().getHouse());
            to.setFlat(dto.getToAddress().getFlat());
            delivery.setToAddress(to);
        }

        return delivery;
    }

}
