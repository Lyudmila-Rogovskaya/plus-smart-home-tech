package ru.yandex.practicum.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import ru.yandex.practicum.errors.HttpStatusProvide;

import java.util.UUID;

@Getter
public class ProductNotFoundException extends RuntimeException implements HttpStatusProvide {

    private final HttpStatus status = HttpStatus.NOT_FOUND;

    public ProductNotFoundException(String message) {
        super(message);
    }

    public ProductNotFoundException(UUID id) {
        super("Product not found with id: " + id);
    }

}
