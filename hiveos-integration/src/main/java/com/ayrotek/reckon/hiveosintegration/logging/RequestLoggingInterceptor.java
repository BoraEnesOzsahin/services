package com.ayrotek.reckon.hiveosintegration.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

/**
 * MVC interceptor that logs every completed HTTP request as a structured ECS event.
 *
 * <p>MDC keys set here and their ECS field targets in {@code logback-spring.xml}:
 * <ul>
 *   <li>{@code httpMethod}      → {@code http.request.method}</li>
 *   <li>{@code urlPath}         → {@code url.path}</li>
 *   <li>{@code httpStatusCode}  → {@code http.response.status_code}</li>
 *   <li>{@code eventDurationMs} → {@code event.duration}</li>
 *   <li>{@code eventAction}     → {@code event.action} (e.g. "GET /api/v1/hiveos/farms")</li>
 *   <li>{@code eventOutcome}    → {@code event.outcome} ("success" or "failure")</li>
 * </ul>
 *
 * <p>All MDC keys are cleared in the {@code finally} block so they do not
 * bleed into subsequent requests on the same thread.
 */
@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingInterceptor.class);
    private static final String START_TIME_ATTR = "RequestLoggingInterceptor.startTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_TIME_ATTR, System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        Long startTime   = (Long) request.getAttribute(START_TIME_ATTR);
        long durationMs  = startTime != null ? System.currentTimeMillis() - startTime : 0;

        String method = request.getMethod();
        String path   = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        if (path == null) {
            path = request.getRequestURI();
        }

        int status = response.getStatus();

        // ECS event.outcome: "success" for 1xx–3xx, "failure" for 4xx–5xx
        String outcome = status < 400 ? "success" : "failure";

        MDC.put("httpMethod",      method);
        MDC.put("urlPath",         path);
        MDC.put("httpStatusCode",  String.valueOf(status));
        MDC.put("eventDurationMs", String.valueOf(durationMs));
        MDC.put("eventAction",     method + " " + path);  // ECS event.action
        MDC.put("eventOutcome",    outcome);               // ECS event.outcome

        try {
            if (ex != null || status >= 500) {
                log.error("HTTP {} {} responded with {} in {}ms", method, path, status, durationMs, ex);
            } else if (status >= 400) {
                log.warn("HTTP {} {} responded with {} in {}ms", method, path, status, durationMs);
            } else {
                log.info("HTTP {} {} responded with {} in {}ms", method, path, status, durationMs);
            }
        } finally {
            MDC.remove("httpMethod");
            MDC.remove("urlPath");
            MDC.remove("httpStatusCode");
            MDC.remove("eventDurationMs");
            MDC.remove("eventAction");
            MDC.remove("eventOutcome");
        }
    }
}
