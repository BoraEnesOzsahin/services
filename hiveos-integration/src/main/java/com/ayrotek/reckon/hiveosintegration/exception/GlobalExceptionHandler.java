package com.ayrotek.reckon.hiveosintegration.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

import static net.logstash.logback.argument.StructuredArguments.kv;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception ex) {
        log.error("Unhandled exception occurred",
                kv("event.outcome", "failure"),
                kv("error.code", "INTERNAL_SERVER_ERROR"),
                kv("error.category", "System"),
                ex);
                
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(ResponseStatusException ex) {
        log.warn("Response status exception",
                kv("event.outcome", "failure"),
                kv("error.code", ex.getStatusCode().toString()),
                kv("error.category", "API"),
                ex);

        return buildErrorResponse(HttpStatus.valueOf(ex.getStatusCode().value()), ex.getReason());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.warn("Validation error",
                kv("event.outcome", "failure"),
                kv("error.code", "BAD_REQUEST"),
                kv("error.category", "Validation"),
                ex);

        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Invalid request body or parameters");
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", status.value());
        response.put("error", status.getReasonPhrase());
        if (message != null) {
            response.put("message", message);
        }
        return ResponseEntity.status(status).body(response);
    }
}
