package com.ayrotek.coldwalletmanagerservice.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

/**
 * Logs one structured line per completed HTTP request.
 *
 * <p>MDC keys set here must match the {@code includeMdcKeyName} entries in
 * logback-spring.xml.  Keys are always removed in finally.
 */
@Component
public class HttpRequestLoggingInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(HttpRequestLoggingInterceptor.class);
    private static final String START_TIME_ATTR = "HttpRequestLoggingInterceptor.startTime";

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        request.setAttribute(START_TIME_ATTR, System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {

        Long startTime = (Long) request.getAttribute(START_TIME_ATTR);
        long durationMs = startTime != null ? System.currentTimeMillis() - startTime : 0;

        String path = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        if (path == null) {
            path = request.getRequestURI();
        }

        int statusCode = response.getStatus();
        String outcome = (ex != null || statusCode >= 500) ? "failure"
                       : (statusCode >= 400)               ? "unknown"
                       :                                     "success";

        MDC.put("http.method",       request.getMethod());
        MDC.put("url.path",          path);
        MDC.put("http.status_code",  String.valueOf(statusCode));
        MDC.put("event.duration_ms", String.valueOf(durationMs));
        MDC.put("event.action",      "request-process");
        MDC.put("event.outcome",     outcome);

        try {
            if ("failure".equals(outcome)) {
                log.error("HTTP request completed. method={}, path={}, status={}, duration_ms={}",
                        request.getMethod(), path, statusCode, durationMs, ex);
            } else {
                log.info("HTTP request completed. method={}, path={}, status={}, duration_ms={}",
                        request.getMethod(), path, statusCode, durationMs);
            }
        } finally {
            MDC.remove("http.method");
            MDC.remove("url.path");
            MDC.remove("http.status_code");
            MDC.remove("event.duration_ms");
            MDC.remove("event.action");
            MDC.remove("event.outcome");
        }
    }
}
