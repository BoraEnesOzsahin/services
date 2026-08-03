package com.ayrotek.coldwalletmanagerservice.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    // This pointcut matches all public methods in the controller and service packages
    @Around("execution(* com.ayrotek.coldwalletmanagerservice.controller..*(..)) || execution(* com.ayrotek.coldwalletmanagerservice.service..*(..))")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        // Push beautiful structured fields to MDC for Logstash to capture
        org.slf4j.MDC.put("audit.action", methodName);
        org.slf4j.MDC.put("audit.resource", className);

        // Strictly mask arguments to prevent sensitive data exposure
        String safeArgs = (args != null && args.length > 0) ? "[Protected Payload]" : "[]";

        log.info("→ START: {}.{}() | Args: {}", className, methodName, safeArgs);

        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long elapsedTime = System.currentTimeMillis() - start;
            
            org.slf4j.MDC.put("audit.result", "SUCCESS");
            org.slf4j.MDC.put("event.outcome", "success");
            
            String safeReturn = (result != null) ? "[Processed Successfully]" : "null";
            log.info("← SUCCESS: {}.{}() | {} ms | Return: {}", className, methodName, elapsedTime, safeReturn);
            
            return result;
        } catch (Throwable e) {
            long elapsedTime = System.currentTimeMillis() - start;
            
            org.slf4j.MDC.put("audit.result", "FAILURE");
            org.slf4j.MDC.put("event.outcome", "failure");
            org.slf4j.MDC.put("error.code", e.getClass().getSimpleName());
            
            log.error("✖ FAILURE: {}.{}() | {} ms | Exception: {}", className, methodName, elapsedTime, e.getMessage());
            throw e;
        } finally {
            org.slf4j.MDC.remove("audit.action");
            org.slf4j.MDC.remove("audit.resource");
            org.slf4j.MDC.remove("audit.result");
            org.slf4j.MDC.remove("event.outcome");
            org.slf4j.MDC.remove("error.code");
        }
    }
}
