package com.ayrotek.reckon.hiveosintegration.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class HttpRequestLoggingInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(HttpRequestLoggingInterceptor.class);

    private static final String START_TIME_ATTR = "startTime";
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        request.setAttribute(START_TIME_ATTR, System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        try {
            long startTime = (Long) request.getAttribute(START_TIME_ATTR);
            long duration = System.currentTimeMillis() - startTime;

            MDC.put("http.method", request.getMethod());
            MDC.put("url.path", request.getRequestURI());
            MDC.put("http.status_code", String.valueOf(response.getStatus()));
            MDC.put("event.duration_ms", String.valueOf(duration));
            MDC.put("event.outcome", response.getStatus() >= 400 ? "failure" : "success");

            if (response.getStatus() >= 500) {
                log.error("HTTP request completed with error status");
            } else {
                log.info("HTTP request completed");
            }
        } finally {
            MDC.remove("http.method");
            MDC.remove("url.path");
            MDC.remove("http.status_code");
            MDC.remove("event.duration_ms");
            MDC.remove("event.outcome");
        }
    }
}
