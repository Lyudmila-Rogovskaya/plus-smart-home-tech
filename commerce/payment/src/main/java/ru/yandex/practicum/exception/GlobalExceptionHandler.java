package ru.yandex.practicum.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.errors.ApiErrors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ApiErrors> handlePaymentNotFound(PaymentNotFoundException ex) {
        ApiErrors error = ApiErrors.builder()
                .cause(ex.getCause())
                .stackTrace(ex.getStackTrace())
                .httpStatus(ex.getStatus())
                .userMessage(ex.getMessage())
                .message(ex.getMessage())
                .build();
        log.warn("Payment not found: {}", ex.getMessage());
        return ResponseEntity.status(ex.getStatus()).body(error);
    }

    @ExceptionHandler(NotEnoughInfoInOrderToCalculateException.class)
    public ResponseEntity<ApiErrors> handleNotEnoughInfo(NotEnoughInfoInOrderToCalculateException ex) {
        ApiErrors error = ApiErrors.builder()
                .cause(ex.getCause())
                .stackTrace(ex.getStackTrace())
                .httpStatus(ex.getStatus())
                .userMessage(ex.getMessage())
                .message(ex.getMessage())
                .build();
        log.warn("Not enough info in order: {}", ex.getMessage());
        return ResponseEntity.status(ex.getStatus()).body(error);
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ApiErrors> handleExternalService(ExternalServiceException ex) {
        ApiErrors error = ApiErrors.builder()
                .cause(ex.getCause())
                .stackTrace(ex.getStackTrace())
                .httpStatus(HttpStatus.SERVICE_UNAVAILABLE)
                .userMessage("External service error: " + ex.getServiceName())
                .message(ex.getMessage())
                .build();
        log.error("External service error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrors> handleGeneric(Exception ex) {
        log.error("Unexpected error", ex);
        ApiErrors error = ApiErrors.builder()
                .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                .userMessage("Internal server error")
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

}
