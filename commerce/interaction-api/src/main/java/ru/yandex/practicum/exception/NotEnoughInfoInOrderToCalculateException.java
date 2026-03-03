package ru.yandex.practicum.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import ru.yandex.practicum.errors.HttpStatusProvide;

@Getter
public class NotEnoughInfoInOrderToCalculateException extends RuntimeException implements HttpStatusProvide {

    private final HttpStatus status = HttpStatus.BAD_REQUEST;

    public NotEnoughInfoInOrderToCalculateException(String message) {
        super(message);
    }

}
