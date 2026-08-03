package com.ayrotek.reckon.auth.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    @Around("execution(* com.ayrotek.reckon.auth.controller..*(..)) || execution(* com.ayrotek.reckon.auth.service..*(..))")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        // Strictly mask arguments to prevent sensitive data exposure
        String argsString = (args != null && args.length > 0) ? "[Protected Payload]" : "[]";

        log.info("AOP Execution Started - Method: {}.{}() - Args: {}", className, methodName, argsString);

        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long elapsedTime = System.currentTimeMillis() - start;
            log.info("AOP Execution Completed - Method: {}.{}() - Executed in: {}ms", className, methodName, elapsedTime);
            return result;
        } catch (Throwable e) {
            long elapsedTime = System.currentTimeMillis() - start;
            log.error("AOP Execution Failed - Method: {}.{}() - Error: {} - Executed in: {}ms", className, methodName, e.getMessage(), elapsedTime);
            throw e;
        }
    }
}
