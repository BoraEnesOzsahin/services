package com.ayrotek.reckon.hiveosintegration.filter;

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

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestCorrelationFilter extends OncePerRequestFilter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String ACTOR_HEADER = "X-Actor";
    
    private static final String TRACE_ID_MDC_KEY = "trace.id";
    private static final String AUDIT_ACTOR_MDC_KEY = "audit.actor";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        try {
            String correlationId = request.getHeader(CORRELATION_ID_HEADER);
            if (correlationId == null || correlationId.isEmpty()) {
                correlationId = UUID.randomUUID().toString();
            }
            MDC.put(TRACE_ID_MDC_KEY, correlationId);
            response.setHeader(CORRELATION_ID_HEADER, correlationId);

            String actor = request.getHeader(ACTOR_HEADER);
            if (actor == null || actor.isEmpty()) {
                actor = "anonymous";
            }
            MDC.put(AUDIT_ACTOR_MDC_KEY, actor);

            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
