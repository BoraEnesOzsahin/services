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

    /* ────────── controllers ────────── */

    @Pointcut("within(com.ayrotek.paymentservice.controller..*)")
    public void controllerMethods() {}

    @Before("controllerMethods()")
    public void logBeforeController(JoinPoint jp) {
        logger(jp).info("→ {}.{}() | args = {}",
                simpleClass(jp), jp.getSignature().getName(),
                Arrays.toString(jp.getArgs()));
    }

    @AfterReturning(pointcut = "controllerMethods()", returning = "result")
    public void logAfterController(JoinPoint jp, Object result) {
        logger(jp).info("← {}.{}() | return = {}",
                simpleClass(jp), jp.getSignature().getName(), result);
    }

    @AfterThrowing(pointcut = "controllerMethods()", throwing = "ex")
    public void logControllerException(JoinPoint jp, Throwable ex) {
        logger(jp).error("✖ {}.{}() | exception = {}",
                simpleClass(jp), jp.getSignature().getName(),
                ex.getMessage(), ex);
    }

    /* ────────── services ────────── */

    @Pointcut("within(com.ayrotek.paymentservice.service..*)")
    public void serviceMethods() {}

    @Around("serviceMethods()")
    public Object logAroundService(ProceedingJoinPoint pjp) throws Throwable {
        Logger log = logger(pjp);
        String cls = simpleClass(pjp);
        String method = pjp.getSignature().getName();

        log.debug("⇢ {}.{}() | args = {}", cls, method,
                Arrays.toString(pjp.getArgs()));
        long start = System.currentTimeMillis();
        try {
            Object result = pjp.proceed();
            long elapsed = System.currentTimeMillis() - start;
            log.debug("⇠ {}.{}() | {} ms | return = {}",
                    cls, method, elapsed, result);
            return result;
        } catch (Throwable t) {
            long elapsed = System.currentTimeMillis() - start;
            log.error("⇠ {}.{}() | {} ms | exception = {}",
                    cls, method, elapsed, t.getMessage(), t);
            throw t;
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
