package ru.yandex.practicum.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.BookedProductsDto;
import ru.yandex.practicum.dto.ChangeProductQuantityRequest;
import ru.yandex.practicum.dto.ShoppingCartDto;
import ru.yandex.practicum.entity.Cart;
import ru.yandex.practicum.entity.CartItem;
import ru.yandex.practicum.exception.NoProductsInShoppingCartException;
import ru.yandex.practicum.exception.NotAuthorizedUserException;
import ru.yandex.practicum.feign.WarehouseClient;
import ru.yandex.practicum.mapper.CartMapper;
import ru.yandex.practicum.repository.CartRepository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShoppingCartService {
    private final CartRepository cartRepository;
    private final CartMapper cartMapper;
    private final WarehouseClient warehouseClient;

    private Cart getActiveCartOrCreate(String username) {
        if (username == null || username.isBlank()) {
            throw new NotAuthorizedUserException("Username must not be empty");
        }
        return cartRepository.findByUsernameAndActiveTrue(username)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUsername(username);
                    newCart.setActive(true);
                    return cartRepository.save(newCart);
                });
    }

    public ShoppingCartDto getCart(String username) {
        Cart cart = getActiveCartOrCreate(username);
        return cartMapper.toDto(cart);
    }

    @Transactional
    public ShoppingCartDto addProducts(String username, Map<UUID, Long> products) {
        Cart cart = getActiveCartOrCreate(username);
        try {
            ShoppingCartDto dtoForCheck = cartMapper.toDto(cart);
            dtoForCheck.getProducts().putAll(products);
            BookedProductsDto booked = warehouseClient.checkProductQuantityEnoughForShoppingCart(dtoForCheck);
            log.info("Warehouse check passed: weight={}, volume={}, fragile={}",
                    booked.getDeliveryWeight(), booked.getDeliveryVolume(), booked.getFragile());
        } catch (FeignException e) {
            log.error("Warehouse check failed", e);
            throw new IllegalArgumentException("Not enough products in warehouse", e);
        }

        for (Map.Entry<UUID, Long> entry : products.entrySet()) {
            UUID productId = entry.getKey();
            Long quantity = entry.getValue();
            cart.getItems().stream()
                    .filter(item -> item.getProductId().equals(productId))
                    .findFirst()
                    .ifPresentOrElse(
                            item -> item.setQuantity(item.getQuantity() + quantity),
                            () -> {
                                CartItem newItem = new CartItem();
                                newItem.setCart(cart);
                                newItem.setProductId(productId);
                                newItem.setQuantity(quantity);
                                cart.getItems().add(newItem);
                            }
                    );
        }
        cartRepository.save(cart);
        return cartMapper.toDto(cart);
    }

    @Transactional
    public ShoppingCartDto removeProducts(String username, List<UUID> productIds) {
        Cart cart = getActiveCartOrCreate(username);
        if (productIds == null || productIds.isEmpty()) {
            throw new NoProductsInShoppingCartException("No product ids provided");
        }
        boolean removed = cart.getItems().removeIf(item -> productIds.contains(item.getProductId()));
        if (!removed) {
            throw new NoProductsInShoppingCartException("None of the specified products found in cart");
        }
        cartRepository.save(cart);
        return cartMapper.toDto(cart);
    }

    @Transactional
    public ShoppingCartDto changeQuantity(String username, ChangeProductQuantityRequest request) {
        Cart cart = getActiveCartOrCreate(username);
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(request.getProductId()))
                .findFirst()
                .orElseThrow(() -> new NoProductsInShoppingCartException("Product not found in cart"));
        item.setQuantity(request.getNewQuantity());
        cartRepository.save(cart);
        return cartMapper.toDto(cart);
    }

    @Transactional
    public void deactivateCart(String username) {
        Cart cart = getActiveCartOrCreate(username);
        cart.setActive(false);
        cartRepository.save(cart);
    }

}
