package ru.yandex.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.entity.CartItem;

import java.util.UUID;

public interface CartItemRepository extends JpaRepository<CartItem, UUID> {
}
