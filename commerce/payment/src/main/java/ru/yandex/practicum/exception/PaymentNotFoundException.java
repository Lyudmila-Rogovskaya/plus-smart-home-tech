package ru.yandex.practicum.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import ru.yandex.practicum.errors.HttpStatusProvide;

@Getter
public class PaymentNotFoundException extends RuntimeException implements HttpStatusProvide {

    private final HttpStatus status = HttpStatus.NOT_FOUND;

    public PaymentNotFoundException(String message) {
        super(message);
    }

    public PaymentNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}
