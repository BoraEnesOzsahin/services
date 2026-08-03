package com.ayrotek.coldwalletmanagerservice.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Propagates or generates a correlation ID for every HTTP request, and optionally
 * captures an actor identity for audit tracing.
 *
 * <p>Behaviour:
 * <ol>
 *   <li>Read X-Correlation-ID header; use it or generate a random UUID.</li>
 *   <li>Write trace ID to MDC as {@code trace.id} (ECS) and echo in response.</li>
 *   <li>Read X-Actor header (optional caller identity, e.g. service name or user ID).
 *       Write to MDC as {@code audit.actor} so AuditService picks it up automatically.</li>
 *   <li>Always remove MDC keys in finally to prevent thread-pool leakage.</li>
 * </ol>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestCorrelationFilter extends OncePerRequestFilter {

    public static final String CORRELATION_HEADER = "X-Correlation-ID";
    public static final String ACTOR_HEADER        = "X-Actor";
    /** ECS-compatible MDC key — picked up by logback-spring.xml includeMdcKeyName */
    public static final String MDC_TRACE_ID        = "trace.id";
    public static final String MDC_ACTOR           = "audit.actor";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String correlationId = resolveCorrelationId(request.getHeader(CORRELATION_HEADER));
        String actor          = resolveActor(request.getHeader(ACTOR_HEADER));

        MDC.put(MDC_TRACE_ID, correlationId);
        MDC.put(MDC_ACTOR,    actor);
        response.setHeader(CORRELATION_HEADER, correlationId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_TRACE_ID);
            MDC.remove(MDC_ACTOR);
        }
    }

    /**
     * Returns the provided value when it is non-blank and at most 128 characters.
     * Generates a random UUID otherwise.
     */
    private String resolveCorrelationId(String headerValue) {
        if (headerValue != null && !headerValue.isBlank() && headerValue.length() <= 128) {
            return headerValue.trim();
        }
        return UUID.randomUUID().toString();
    }

    /**
     * Returns a sanitised actor identifier from the X-Actor header.
     * Falls back to "anonymous" when the header is absent or blank.
     */
    private String resolveActor(String headerValue) {
        if (headerValue != null && !headerValue.isBlank() && headerValue.length() <= 128) {
            return headerValue.trim();
        }
        return "anonymous";
    }
}
