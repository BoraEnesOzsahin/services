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

        // Strictly mask arguments to prevent sensitive data exposure
        String argsString = (args != null && args.length > 0) ? "[Protected Payload]" : "[]";

        log.info("AOP Execution Started - Method: {}.{}() - Args: {}", className, methodName, argsString);

        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long elapsedTime = System.currentTimeMillis() - start;
            
            // Log successful execution
            log.info("AOP Execution Completed - Method: {}.{}() - Executed in: {}ms", className, methodName, elapsedTime);
            return result;
        } catch (IllegalArgumentException e) {
            long elapsedTime = System.currentTimeMillis() - start;
            log.error("AOP Execution Failed (Illegal Argument) - Method: {}.{}() - Error: {} - Executed in: {}ms", className, methodName, e.getMessage(), elapsedTime);
            throw e;
        } catch (Throwable e) {
            long elapsedTime = System.currentTimeMillis() - start;
            log.error("AOP Execution Failed - Method: {}.{}() - Error: {} - Executed in: {}ms", className, methodName, e.getMessage(), elapsedTime);
            throw e;
        }
    }
}
