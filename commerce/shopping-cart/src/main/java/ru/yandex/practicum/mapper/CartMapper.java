package ru.yandex.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.dto.ShoppingCartDto;
import ru.yandex.practicum.entity.Cart;
import ru.yandex.practicum.entity.CartItem;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class CartMapper {

    public ShoppingCartDto toDto(Cart cart) {
        ShoppingCartDto dto = new ShoppingCartDto();
        dto.setShoppingCartId(cart.getId());
        Map<UUID, Long> products = new HashMap<>();
        for (CartItem item : cart.getItems()) {
            products.put(item.getProductId(), item.getQuantity());
        }
        dto.setProducts(products);
        return dto;
    }

}
