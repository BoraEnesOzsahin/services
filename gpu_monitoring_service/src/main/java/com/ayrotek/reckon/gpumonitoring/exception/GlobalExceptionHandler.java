package com.ayrotek.reckon.gpumonitoring.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidNodeTokenException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidToken(InvalidNodeTokenException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "error", "unauthorized",
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(NodeNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNodeNotFound(NodeNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "error", "node_not_found",
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(NodeRevokedException.class)
    public ResponseEntity<Map<String, Object>> handleNodeRevoked(NodeRevokedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "error", "node_revoked",
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(fe -> errors.put(fe.getField(), fe.getDefaultMessage()));
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of(
                "error", "validation_error",
                "errors", errors
        ));
    }
}
