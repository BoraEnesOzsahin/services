package com.ayrotek.reckon.hiveosintegration.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Service
public class AuditService {

    private static final Logger auditLogger = LoggerFactory.getLogger("audit");

    public AuditBuilder log(String action) {
        return new AuditBuilder(action);
    }

    public static class AuditBuilder {
        private final String action;
        private String resource;
        private String result;
        private final Map<String, Object> additionalFields = new HashMap<>();

        public AuditBuilder(String action) {
            this.action = action;
        }

        public AuditBuilder resource(String resource) {
            this.resource = resource;
            return this;
        }

        public AuditBuilder result(String result) {
            this.result = result;
            return this;
        }

        public AuditBuilder with(String key, Object value) {
            this.additionalFields.put(key, value);
            return this;
        }

        public void emit() {
            String actor = MDC.get("audit.actor");
            if (actor == null) {
                actor = "system";
            }
            
            Object[] args = new Object[4 + additionalFields.size()];
            args[0] = kv("audit.action", action);
            args[1] = kv("audit.actor", actor);
            args[2] = kv("audit.resource", resource);
            args[3] = kv("audit.result", result);
            
            int i = 4;
            for (Map.Entry<String, Object> entry : additionalFields.entrySet()) {
                args[i++] = kv(entry.getKey(), entry.getValue());
            }
            
            auditLogger.info("Audit event: {}", action, args);
        }
    }
}
