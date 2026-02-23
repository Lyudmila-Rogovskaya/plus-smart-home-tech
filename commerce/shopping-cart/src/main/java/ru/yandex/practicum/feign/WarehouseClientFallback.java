package ru.yandex.practicum.feign;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.dto.BookedProductsDto;
import ru.yandex.practicum.dto.ShoppingCartDto;

@Component
@Slf4j
public class WarehouseClientFallback implements WarehouseClient {

    @Override
    public BookedProductsDto checkProductQuantityEnoughForShoppingCart(ShoppingCartDto shoppingCartDto) {
        log.error("Fallback: warehouse service is unavailable");
        throw new RuntimeException("Warehouse service is unavailable, please try later");
    }

}
