package ru.yandex.practicum.feign;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.dto.*;

import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class WarehouseClientFallback implements WarehouseClient {

    @Override
    public BookedProductsDto checkProductQuantityEnoughForShoppingCart(ShoppingCartDto shoppingCartDto) {
        log.error("Fallback: warehouse service is unavailable for checkProductQuantityEnoughForShoppingCart");
        throw new RuntimeException("Warehouse service is unavailable, please try later");
    }

    @Override
    public BookedProductsDto assemblyProductsForOrder(AssemblyProductsForOrderRequest request) {
        log.error("Fallback: warehouse service is unavailable for assemblyProductsForOrder");
        throw new RuntimeException("Warehouse service is unavailable, please try later");
    }

    @Override
    public void shippedToDelivery(ShippedToDeliveryRequest request) {
        log.error("Fallback: warehouse service is unavailable for shippedToDelivery");
        throw new RuntimeException("Warehouse service is unavailable, please try later");
    }

    @Override
    public void acceptReturn(Map<UUID, Long> products) {
        log.error("Fallback: warehouse service is unavailable for acceptReturn");
        throw new RuntimeException("Warehouse service is unavailable, please try later");
    }

    @Override
    public AddressDto getWarehouseAddress() {
        log.error("Fallback: warehouse service is unavailable for getWarehouseAddress");
        throw new RuntimeException("Warehouse service is unavailable, please try later");
    }

}
