package com.mazasoft.ecommerce.userservice.exceptions.handlers;

import com.mazasoft.ecommerce.userservice.exceptions.ApiException;
import jakarta.persistence.EntityNotFoundException;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> handle(ApiException ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(Map.of(
                        "error", ex.getStatus().getReasonPhrase(),
                        "message", ex.getMessage()
                ));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handle(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "error", HttpStatus.NOT_FOUND.getReasonPhrase(),
                        "message", ex.getMessage()
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handle(MethodArgumentNotValidException ex) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining("; "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "error", HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        "message", details.isBlank() ? "Validation failed" : details
                ));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handle(ConstraintViolationException ex) {
        String details = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "error", HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        "message", details.isBlank() ? "Validation failed" : details
                ));
    }

    private String formatFieldError(FieldError error) {
        if (error == null) {
            return "";
        }
        String field = error.getField();
        String message = error.getDefaultMessage();
        return field + ": " + (message == null ? "invalid value" : message);
    }
}
