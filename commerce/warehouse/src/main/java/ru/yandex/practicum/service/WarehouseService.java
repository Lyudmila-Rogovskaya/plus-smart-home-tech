package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.*;
import ru.yandex.practicum.entity.WarehouseProduct;
import ru.yandex.practicum.exception.NoSpecifiedProductInWarehouseException;
import ru.yandex.practicum.exception.ProductInShoppingCartLowQuantityInWarehouse;
import ru.yandex.practicum.exception.SpecifiedProductAlreadyInWarehouseException;
import ru.yandex.practicum.feign.ShoppingStoreClient;
import ru.yandex.practicum.mapper.WarehouseMapper;
import ru.yandex.practicum.repository.WarehouseRepository;

import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WarehouseService {
    private final WarehouseRepository warehouseRepository;
    private final WarehouseMapper warehouseMapper;
    private final ShoppingStoreClient shoppingStoreClient;

    private static final String[] ADDRESSES = {"ADDRESS_1", "ADDRESS_2"};
    private static final String CURRENT_ADDRESS = ADDRESSES[new Random().nextInt(ADDRESSES.length)];

    @Transactional
    public void addNewProduct(NewProductInWarehouseRequest request) {
        if (warehouseRepository.existsById(request.getProductId())) {
            throw new SpecifiedProductAlreadyInWarehouseException("Product already exists in warehouse");
        }
        WarehouseProduct product = warehouseMapper.toEntity(request);
        warehouseRepository.save(product);
        updateQuantityState(request.getProductId(), 0L);
    }

    @Transactional
    public void increaseQuantity(AddProductToWarehouseRequest request) {
        WarehouseProduct product = warehouseRepository.findById(request.getProductId())
                .orElseThrow(() -> new NoSpecifiedProductInWarehouseException("Product not found"));
        product.setQuantity(product.getQuantity() + request.getQuantity());
        warehouseRepository.save(product);
        updateQuantityState(product.getProductId(), product.getQuantity());
    }

    private void updateQuantityState(UUID productId, Long quantity) {
        QuantityState state;
        if (quantity == 0) {
            state = QuantityState.ENDED;
        } else if (quantity < 10) {
            state = QuantityState.FEW;
        } else if (quantity <= 100) {
            state = QuantityState.ENOUGH;
        } else {
            state = QuantityState.MANY;
        }
        SetProductQuantityStateRequest request = new SetProductQuantityStateRequest();
        request.setProductId(productId);
        request.setQuantityState(state);
        try {
            shoppingStoreClient.setProductQuantityState(request);
        } catch (Exception e) {
            log.error("Failed to update quantity state for product {}", productId, e);
        }
    }

    @Transactional(readOnly = true)
    public BookedProductsDto checkAvailability(ShoppingCartDto cartDto) {
        double totalWeight = 0.0;
        double totalVolume = 0.0;
        boolean fragile = false;

        for (Map.Entry<UUID, Long> entry : cartDto.getProducts().entrySet()) {
            UUID productId = entry.getKey();
            Long requestedQuantity = entry.getValue();
            WarehouseProduct product = warehouseRepository.findById(productId)
                    .orElseThrow(() -> new NoSpecifiedProductInWarehouseException("Product not found: " + productId));
            if (product.getQuantity() < requestedQuantity) {
                throw new ProductInShoppingCartLowQuantityInWarehouse(
                        "Not enough quantity for product " + productId + ". Available: " + product.getQuantity());
            }
            if (product.getFragile()) {
                fragile = true;
            }
            double volume = product.getDimension().getWidth() *
                    product.getDimension().getHeight() *
                    product.getDimension().getDepth();
            totalVolume += volume * requestedQuantity;
            totalWeight += product.getWeight() * requestedQuantity;
        }

        BookedProductsDto result = new BookedProductsDto();
        result.setDeliveryWeight(totalWeight);
        result.setDeliveryVolume(totalVolume);
        result.setFragile(fragile);
        return result;
    }

    public AddressDto getAddress() {
        AddressDto address = new AddressDto();
        address.setCountry(CURRENT_ADDRESS);
        address.setCity(CURRENT_ADDRESS);
        address.setStreet(CURRENT_ADDRESS);
        address.setHouse(CURRENT_ADDRESS);
        address.setFlat(CURRENT_ADDRESS);
        return address;
    }

}
