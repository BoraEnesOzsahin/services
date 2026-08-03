package com.ayrotek.paymentservice.config;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Cross-cutting logging aspect.
 *
 * Adaptable: controlled entirely by the Spring profile + logback config.
 * - Local  → human-readable console lines
 * - Docker → ECS-JSON lines consumed by Filebeat → Logstash → Elasticsearch
 *
 * Uses a PER-CLASS logger so Kibana / Logstash can filter by logger name
 * (i.e. the real class that was called, not this aspect class).
 */
@Aspect
@Component
public class LoggingAspect {

    /* ────────── pointcuts ────────── */

    @Pointcut("within(com.ayrotek.paymentservice.controller..*)")
    public void controllerMethods() {}

    @Pointcut("within(com.ayrotek.paymentservice.service..*)")
    public void serviceMethods() {}

    /* ────────── targeted audit logging ────────── */

    @Around("controllerMethods() || serviceMethods()")
    public Object logAllMethods(ProceedingJoinPoint pjp) throws Throwable {
        Logger log = logger(pjp);
        String cls = simpleClass(pjp);
        String method = pjp.getSignature().getName();

        // Push beautiful structured fields to MDC for Logstash to capture
        org.slf4j.MDC.put("audit.action", method);
        org.slf4j.MDC.put("audit.resource", cls);

        // Safely mask arguments
        String safeArgs = (pjp.getArgs() != null && pjp.getArgs().length > 0) ? "[Protected Payload]" : "[]";

        log.info("→ START: {}.{}() | Args: {}", cls, method, safeArgs);
                 
        long start = System.currentTimeMillis();
        try {
            Object result = pjp.proceed();
            long elapsed = System.currentTimeMillis() - start;
            
            org.slf4j.MDC.put("audit.result", "SUCCESS");
            org.slf4j.MDC.put("event.outcome", "success");
            
            String safeReturn = (result != null) ? "[Processed Successfully]" : "null";
            log.info("← SUCCESS: {}.{}() | {} ms | Return: {}", cls, method, elapsed, safeReturn);
            
            return result;
        } catch (Throwable t) {
            long elapsed = System.currentTimeMillis() - start;
            
            org.slf4j.MDC.put("audit.result", "FAILURE");
            org.slf4j.MDC.put("event.outcome", "failure");
            org.slf4j.MDC.put("error.code", t.getClass().getSimpleName());
            
            log.error("✖ FAILURE: {}.{}() | {} ms | Exception: {}", cls, method, elapsed, t.getMessage());
            throw t;
        } finally {
            // Clean up MDC so it doesn't leak to other threads
            org.slf4j.MDC.remove("audit.action");
            org.slf4j.MDC.remove("audit.resource");
            org.slf4j.MDC.remove("audit.result");
            org.slf4j.MDC.remove("event.outcome");
            org.slf4j.MDC.remove("error.code");
        }
    }

    /* ────────── helpers ────────── */

    private Logger logger(JoinPoint jp) {
        return LoggerFactory.getLogger(jp.getTarget().getClass());
    }

    private String simpleClass(JoinPoint jp) {
        return jp.getTarget().getClass().getSimpleName();
    }
}
