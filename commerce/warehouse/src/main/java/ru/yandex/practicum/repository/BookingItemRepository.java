package ru.yandex.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.entity.BookingItem;

import java.util.UUID;

public interface BookingItemRepository extends JpaRepository<BookingItem, UUID> {
}
