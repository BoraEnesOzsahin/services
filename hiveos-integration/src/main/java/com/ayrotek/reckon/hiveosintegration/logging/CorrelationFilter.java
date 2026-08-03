package com.ayrotek.reckon.hiveosintegration.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Servlet filter that extracts or generates a correlation ID and a request ID
 * for every inbound HTTP request.
 *
 * <p>Both IDs are stored in MDC under the ECS-aligned keys used by
 * {@code logback-spring.xml}:
 * <ul>
 *   <li>{@code traceId} → emitted as {@code trace.id} in JSON logs</li>
 *   <li>{@code requestId} → emitted as {@code transaction.id} in JSON logs</li>
 * </ul>
 *
 * <p>The IDs are also echoed back on the response so callers can correlate
 * log lines with their requests.
 */
@Component
public class CorrelationFilter extends OncePerRequestFilter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String REQUEST_ID_HEADER     = "X-Request-ID";

    /**
     * MDC key mapped to ECS {@code trace.id} in logback-spring.xml.
     * Changed from "correlationId" to "traceId" to fix the alignment: the
     * logback pattern reads {@code %mdc{traceId}} for the {@code trace.id} field,
     * but the old filter was writing under "correlationId", leaving trace.id always empty.
     */
    private static final String TRACE_ID_MDC   = "traceId";
    private static final String REQUEST_ID_MDC = "requestId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String traceId = request.getHeader(CORRELATION_ID_HEADER);
        if (!isValid(traceId)) {
            traceId = UUID.randomUUID().toString();
        }

        String requestId = request.getHeader(REQUEST_ID_HEADER);
        if (!isValid(requestId)) {
            requestId = UUID.randomUUID().toString();
        }

        MDC.put(TRACE_ID_MDC,   traceId);
        MDC.put(REQUEST_ID_MDC, requestId);

        response.setHeader(CORRELATION_ID_HEADER, traceId);
        response.setHeader(REQUEST_ID_HEADER,     requestId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(TRACE_ID_MDC);
            MDC.remove(REQUEST_ID_MDC);
        }
    }

    private boolean isValid(String id) {
        if (!StringUtils.hasText(id)) {
            return false;
        }
        // Basic sanitization: allow alphanumeric and hyphens only (prevents log injection)
        return id.matches("^[a-zA-Z0-9-]+$") && id.length() <= 64;
    }
}
