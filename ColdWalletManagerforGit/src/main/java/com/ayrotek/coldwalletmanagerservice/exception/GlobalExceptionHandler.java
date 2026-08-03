package com.ayrotek.coldwalletmanagerservice.exception;

import net.logstash.logback.argument.StructuredArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Centralised exception handler.
 *
 * <p>Logs structured error events with ECS fields and MDC context.
 * Stack traces appear in the log via Logback's built-in mechanism
 * (passing the exception as the last SLF4J argument); they are NOT
 * embedded in the HTTP response to avoid leaking internal details.
 *
 * <p>MDC keys set here ({@code error.code}, {@code error.category}) are
 * forwarded by the ERROR_FILE appender in logback-spring.xml.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ── Validation errors (400) ──────────────────────────────────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException e) {
        List<String> fieldErrors = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.toList());

        withErrorMdc("VALIDATION_ERROR", "validation", () ->
                log.warn("Request validation failed.",
                        StructuredArguments.kv("event.outcome",  "failure"),
                        StructuredArguments.kv("error.code",     "VALIDATION_ERROR"),
                        StructuredArguments.kv("error.category", "validation"),
                        StructuredArguments.kv("field_errors",   fieldErrors))
        );

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status",  400);
        body.put("error",   "Bad Request");
        body.put("message", "Validation failed");
        body.put("details", fieldErrors);
        return ResponseEntity.badRequest().body(body);
    }

    // ── Explicit HTTP status exceptions ──────────────────────────────────────

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(ResponseStatusException e) {
        HttpStatus status = HttpStatus.valueOf(e.getStatusCode().value());
        String category   = status.is4xxClientError() ? "client_error" : "server_error";
        String code       = status.name();

        withErrorMdc(code, category, () -> {
            if (status.is5xxServerError()) {
                log.error("Request failed.",
                        StructuredArguments.kv("event.outcome",  "failure"),
                        StructuredArguments.kv("error.code",     code),
                        StructuredArguments.kv("error.category", category),
                        StructuredArguments.kv("http.status",    status.value()),
                        StructuredArguments.kv("reason",         e.getReason()),
                        e);
            } else {
                log.warn("Request rejected.",
                        StructuredArguments.kv("event.outcome",  "failure"),
                        StructuredArguments.kv("error.code",     code),
                        StructuredArguments.kv("error.category", category),
                        StructuredArguments.kv("http.status",    status.value()),
                        StructuredArguments.kv("reason",         e.getReason()));
            }
        });

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status",  status.value());
        body.put("error",   status.getReasonPhrase());
        body.put("message", e.getReason());
        return new ResponseEntity<>(body, status);
    }

    // ── Catch-all ────────────────────────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception e) {
        withErrorMdc("INTERNAL_ERROR", "server_error", () ->
                log.error("Unhandled exception.",
                        StructuredArguments.kv("event.outcome",  "failure"),
                        StructuredArguments.kv("error.code",     "INTERNAL_ERROR"),
                        StructuredArguments.kv("error.category", "server_error"),
                        StructuredArguments.kv("error.type",     e.getClass().getName()),
                        e)
        );

        // Do NOT expose internal stack traces in the HTTP response
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status",  500);
        body.put("error",   "Internal Server Error");
        body.put("message", "An unexpected error occurred. Check logs for details.");
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    /**
     * Sets {@code error.code} and {@code error.category} in MDC for the
     * duration of the given action, then cleans up in a finally block.
     */
    private void withErrorMdc(String errorCode, String errorCategory, Runnable action) {
        MDC.put("error.code",     errorCode);
        MDC.put("error.category", errorCategory);
        MDC.put("event.outcome",  "failure");
        try {
            action.run();
        } finally {
            MDC.remove("error.code");
            MDC.remove("error.category");
            MDC.remove("event.outcome");
        }
    }
}
