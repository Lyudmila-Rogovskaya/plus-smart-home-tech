package ru.yandex.practicum.errors;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApiErrors {

    Throwable cause;
    StackTraceElement[] stackTrace;
    HttpStatus httpStatus;
    String userMessage;
    String message;
    Throwable[] suppressed;
    String localizedMessage;

}
