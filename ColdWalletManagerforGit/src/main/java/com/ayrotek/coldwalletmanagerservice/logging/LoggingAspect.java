package com.ayrotek.coldwalletmanagerservice.logging;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.slf4j.MDC;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import org.aspectj.lang.reflect.MethodSignature;

@Aspect
@Component
public class LoggingAspect {

    private static final Pattern SENSITIVE_PATTERN = Pattern.compile("(?i)(password|secret|key|token|seed|pin)[\\s]*[:=][\\s]*\"?([^\",} ]+)\"?");

    @Pointcut("within(com.ayrotek..controller..*)")
    public void controllerMethods() {}

    @Pointcut("within(com.ayrotek..service..*)")
    public void serviceMethods() {}

    @Around("controllerMethods() || serviceMethods()")
    public Object logAllMethods(ProceedingJoinPoint pjp) throws Throwable {
        Logger log = logger(pjp);
        String cls = simpleClass(pjp);
        String method = pjp.getSignature().getName();

        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method methodObj = signature.getMethod();
        
        String startMsg = null;
        String successMsg = null;
        for (java.lang.annotation.Annotation ann : methodObj.getAnnotations()) {
            if (ann.annotationType().getSimpleName().equals("TrackOperation")) {
                try {
                    startMsg = (String) ann.annotationType().getMethod("start").invoke(ann);
                    successMsg = (String) ann.annotationType().getMethod("success").invoke(ann);
                } catch (Exception ignored) {}
            }
        }

        MDC.put("audit.action", method);
        MDC.put("audit.resource", cls);
        String safeArgs = maskCrucialData(pjp.getArgs());

        if (startMsg != null) {
            log.info("▶ {}", startMsg);
        } else {
            log.info("→ START: {}.{}() | Args: {}", cls, method, safeArgs);
        }
                 
        long start = System.currentTimeMillis();
        try {
            Object result = pjp.proceed();
            long elapsed = System.currentTimeMillis() - start;
            
            MDC.put("audit.result", "SUCCESS");
            MDC.put("event.outcome", "success");
            
            if (successMsg != null) {
                log.info("■ {} ({}ms)", successMsg, elapsed);
            } else {
                String safeReturn = maskCrucialData(result);
                log.info("← SUCCESS: {}.{}() | {} ms | Return: {}", cls, method, elapsed, safeReturn);
            }
            
            return result;
        } catch (Throwable t) {
            long elapsed = System.currentTimeMillis() - start;
            MDC.put("audit.result", "FAILURE");
            MDC.put("event.outcome", "failure");
            MDC.put("error.code", t.getClass().getSimpleName());
            
            log.info("✖ FAILURE: {}.{}() | {} ms | Exception: {}", cls, method, elapsed, t.getMessage());
            throw t;
        } finally {
            MDC.remove("audit.action");
            MDC.remove("audit.resource");
            MDC.remove("audit.result");
            MDC.remove("event.outcome");
            MDC.remove("error.code");
        }
    }

    private Logger logger(JoinPoint jp) {
        return LoggerFactory.getLogger(jp.getTarget().getClass());
    }

    private String simpleClass(JoinPoint jp) {
        return jp.getTarget().getClass().getSimpleName();
    }

    private String maskCrucialData(Object obj) {
        if (obj == null) return "null";
        
        String input;
        if (obj instanceof Object[]) {
            input = Arrays.toString((Object[]) obj);
        } else {
            input = obj.toString();
        }

        Matcher matcher = SENSITIVE_PATTERN.matcher(input);
        return matcher.replaceAll("$1=***");
    }
}
