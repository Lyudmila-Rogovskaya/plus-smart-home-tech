package ru.yandex.practicum.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.errors.ApiErrors;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrors> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        ApiErrors error = ApiErrors.builder()
                .httpStatus(HttpStatus.BAD_REQUEST)
                .userMessage("Validation error")
                .message(message)
                .build();

        log.warn("Validation error: {}", message);
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrors> handleIllegalArgument(IllegalArgumentException ex) {
        ApiErrors error = ApiErrors.builder()
                .httpStatus(HttpStatus.BAD_REQUEST)
                .userMessage(ex.getMessage())
                .message(ex.getMessage())
                .build();
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ApiErrors> handleExternalService(ExternalServiceException ex) {
        ApiErrors error = ApiErrors.builder()
                .httpStatus(HttpStatus.SERVICE_UNAVAILABLE)
                .userMessage("External service error: " + ex.getServiceName())
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    @ExceptionHandler(NoDeliveryFoundException.class)
    public ResponseEntity<ApiErrors> handleNoDelivery(NoDeliveryFoundException ex) {
        ApiErrors error = ApiErrors.builder()
                .httpStatus(ex.getStatus())
                .userMessage(ex.getMessage())
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(ex.getStatus()).body(error);
    }

}
