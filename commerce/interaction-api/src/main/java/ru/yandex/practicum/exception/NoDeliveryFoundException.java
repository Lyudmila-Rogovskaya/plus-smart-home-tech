package ru.yandex.practicum.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import ru.yandex.practicum.errors.HttpStatusProvide;

@Getter
public class NoDeliveryFoundException extends RuntimeException implements HttpStatusProvide {

    private final HttpStatus status = HttpStatus.NOT_FOUND;

    public NoDeliveryFoundException(String message) {
        super(message);
    }

}
