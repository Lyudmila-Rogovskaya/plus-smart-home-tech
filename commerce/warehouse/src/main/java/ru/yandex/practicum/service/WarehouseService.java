package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.*;
import ru.yandex.practicum.entity.BookingItem;
import ru.yandex.practicum.entity.Dimension;
import ru.yandex.practicum.entity.OrderBooking;
import ru.yandex.practicum.entity.WarehouseProduct;
import ru.yandex.practicum.exception.NoOrderFoundException;
import ru.yandex.practicum.exception.NoSpecifiedProductInWarehouseException;
import ru.yandex.practicum.exception.ProductInShoppingCartLowQuantityInWarehouse;
import ru.yandex.practicum.exception.SpecifiedProductAlreadyInWarehouseException;
import ru.yandex.practicum.feign.ShoppingStoreClient;
import ru.yandex.practicum.mapper.WarehouseMapper;
import ru.yandex.practicum.repository.BookingItemRepository;
import ru.yandex.practicum.repository.OrderBookingRepository;
import ru.yandex.practicum.repository.WarehouseRepository;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final WarehouseMapper warehouseMapper;
    private final ShoppingStoreClient shoppingStoreClient;
    private final OrderBookingRepository orderBookingRepository;
    private final BookingItemRepository bookingItemRepository;

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
        Map<UUID, Long> requestedProducts = cartDto.getProducts();
        if (requestedProducts == null || requestedProducts.isEmpty()) {
            throw new IllegalArgumentException("Shopping cart is empty");
        }

        Set<UUID> productIds = requestedProducts.keySet();
        List<WarehouseProduct> products = warehouseRepository.findAllById(productIds);

        if (products.size() != productIds.size()) {
            Set<UUID> foundIds = products.stream()
                    .map(WarehouseProduct::getProductId)
                    .collect(Collectors.toSet());
            productIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .findFirst()
                    .ifPresent(missingId -> {
                        throw new NoSpecifiedProductInWarehouseException("Product not found: " + missingId);
                    });
        }

        double totalWeight = 0.0;
        double totalVolume = 0.0;
        boolean fragile = false;

        for (WarehouseProduct product : products) {
            UUID productId = product.getProductId();
            Long requestedQuantity = requestedProducts.get(productId);

            if (product.getQuantity() < requestedQuantity) {
                throw new ProductInShoppingCartLowQuantityInWarehouse(
                        "Not enough quantity for product " + productId + ". Available: " + product.getQuantity());
            }

            if (product.getFragile()) {
                fragile = true;
            }

            Dimension dim = product.getDimension();
            if (dim != null) {
                double volume = dim.getWidth() * dim.getHeight() * dim.getDepth();
                totalVolume += volume * requestedQuantity;
            }
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

    @Transactional
    public BookedProductsDto assemblyProductsForOrder(AssemblyProductsForOrderRequest request) {
        UUID orderId = request.getOrderId();
        Map<UUID, Long> products = request.getProducts();

        List<WarehouseProduct> warehouseProducts = warehouseRepository.findAllById(products.keySet());
        if (warehouseProducts.size() != products.size()) {
            Set<UUID> foundIds = warehouseProducts.stream()
                    .map(WarehouseProduct::getProductId)
                    .collect(Collectors.toSet());
            products.keySet().stream()
                    .filter(id -> !foundIds.contains(id))
                    .findFirst()
                    .ifPresent(missingId -> {
                        throw new NoSpecifiedProductInWarehouseException("Product not found: " + missingId);
                    });
        }

        double totalWeight = 0.0;
        double totalVolume = 0.0;
        boolean fragile = false;

        for (WarehouseProduct wp : warehouseProducts) {
            UUID productId = wp.getProductId();
            Long requestedQty = products.get(productId);
            if (wp.getQuantity() < requestedQty) {
                throw new ProductInShoppingCartLowQuantityInWarehouse(
                        "Not enough quantity for product " + productId + ". Available: " + wp.getQuantity());
            }
            wp.setQuantity(wp.getQuantity() - requestedQty);
            warehouseRepository.save(wp);

            totalWeight += wp.getWeight() * requestedQty;
            if (wp.getDimension() != null) {
                double volume = wp.getDimension().getWidth() * wp.getDimension().getHeight() * wp.getDimension().getDepth();
                totalVolume += volume * requestedQty;
            }
            if (wp.getFragile()) {
                fragile = true;
            }
        }

        OrderBooking booking = new OrderBooking();
        booking.setOrderId(orderId);
        booking = orderBookingRepository.save(booking);

        for (Map.Entry<UUID, Long> entry : products.entrySet()) {
            BookingItem item = new BookingItem();
            item.setBooking(booking);
            item.setProductId(entry.getKey());
            item.setQuantity(entry.getValue());
            bookingItemRepository.save(item);
        }

        BookedProductsDto result = new BookedProductsDto();
        result.setDeliveryWeight(totalWeight);
        result.setDeliveryVolume(totalVolume);
        result.setFragile(fragile);
        return result;
    }

    @Transactional
    public void shippedToDelivery(ShippedToDeliveryRequest request) {
        OrderBooking booking = orderBookingRepository.findByOrderId(request.getOrderId())
                .orElseThrow(() -> new NoOrderFoundException("No booking found for order: " + request.getOrderId()));
        booking.setDeliveryId(request.getDeliveryId());
        orderBookingRepository.save(booking);
        log.info("Order {} marked as shipped to delivery with deliveryId {}", request.getOrderId(), request.getDeliveryId());
    }

    @Transactional
    public void acceptReturn(Map<UUID, Long> products) {
        for (Map.Entry<UUID, Long> entry : products.entrySet()) {
            WarehouseProduct wp = warehouseRepository.findById(entry.getKey())
                    .orElseThrow(() -> new NoSpecifiedProductInWarehouseException("Product not found: " + entry.getKey()));
            wp.setQuantity(wp.getQuantity() + entry.getValue());
            warehouseRepository.save(wp);
            updateQuantityState(wp.getProductId(), wp.getQuantity());
        }
        log.info("Return accepted for products: {}", products);
    }

}
