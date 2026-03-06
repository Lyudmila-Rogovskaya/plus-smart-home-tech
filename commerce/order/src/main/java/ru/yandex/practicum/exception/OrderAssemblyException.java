package ru.yandex.practicum.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import ru.yandex.practicum.errors.HttpStatusProvide;

@Getter
public class OrderAssemblyException extends RuntimeException implements HttpStatusProvide {

    private final HttpStatus status = HttpStatus.BAD_REQUEST;

    public OrderAssemblyException(String message, Throwable cause) {
        super(message, cause);
    }

    public OrderAssemblyException(String message) {
        super(message);
    }

}
