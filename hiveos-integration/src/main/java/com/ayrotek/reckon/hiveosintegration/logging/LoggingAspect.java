package com.ayrotek.reckon.hiveosintegration.logging;

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

    @Around("execution(* com.ayrotek.reckon.hiveosintegration.controller..*(..)) || execution(* com.ayrotek.reckon.hiveosintegration.service..*(..))")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        String argsString = "[]";
        if (joinPoint.getSignature() instanceof MethodSignature) {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            Annotation[][] parameterAnnotations = method.getParameterAnnotations();
            
            List<Object> maskedArgs = new ArrayList<>();
            for (int i = 0; i < args.length; i++) {
                boolean hasMask = false;
                if (parameterAnnotations.length > i) {
                    for (Annotation annotation : parameterAnnotations[i]) {
                        if (annotation instanceof Mask) {
                            hasMask = true;
                            break;
                        }
                    }
                }
                
                if (hasMask || methodName.toLowerCase().contains("password") || methodName.toLowerCase().contains("secret") || methodName.toLowerCase().contains("key")) {
                    maskedArgs.add("********");
                } else {
                    maskedArgs.add(args[i]);
                }
            }
            argsString = maskedArgs.toString();
        } else {
            if (!methodName.toLowerCase().contains("password") && !methodName.toLowerCase().contains("secret") && !methodName.toLowerCase().contains("key")) {
                argsString = Arrays.toString(args);
            } else {
                argsString = "[********]";
            }
        }

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
