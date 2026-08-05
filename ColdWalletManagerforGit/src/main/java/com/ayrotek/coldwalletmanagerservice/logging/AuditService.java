package com.ayrotek.coldwalletmanagerservice.logging;

import net.logstash.logback.argument.StructuredArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Emits structured audit events to the dedicated audit log.
 *
 * <p>Usage example:
 * <pre>{@code
 *   auditService.log("wallet.sign_transaction")
 *               .actor("user-123")
 *               .resource("wallet-456")
 *               .result("success")
 *               .emit();
 * }</pre>
 *
 * <p>Each event is written as a JSON line to {@code logs/audit.log} via the
 * {@code audit} Logback logger (see logback-spring.xml).  The MDC keys
 * {@code audit.action}, {@code audit.actor}, {@code audit.resource} and
 * {@code audit.result} are forwarded by the AUDIT_FILE encoder.
 */
@Service
public class AuditService {

    private static final Logger auditLog = LoggerFactory.getLogger("audit");

    /**
     * Starts a fluent audit event builder for the given action name.
     *
     * @param action a dot-namespaced action identifier, e.g. {@code "wallet.withdraw"}
     */
    public AuditEventBuilder log(String action) {
        return new AuditEventBuilder(action);
    }

    // ── Inner builder ────────────────────────────────────────────────────────

    public static final class AuditEventBuilder {

        private final String action;
        private String actor    = null; // resolved from MDC in emit() when not set explicitly
        private String resource = "-";
        private String result   = "success";
        private String detail   = null;

        private AuditEventBuilder(String action) {
            this.action = action;
        }

        /** The authenticated principal or service that triggered the action.
         *  When not called, the value defaults to the {@code audit.actor} MDC key
         *  (populated from the {@code X-Actor} request header by {@link RequestCorrelationFilter}). */
        public AuditEventBuilder actor(String actor) {
            this.actor = actor != null ? actor : "-";
            return this;
        }

        /** The resource identifier this action targeted (e.g. wallet ID). */
        public AuditEventBuilder resource(String resource) {
            this.resource = resource != null ? resource : "-";
            return this;
        }

        /** Outcome: {@code "success"}, {@code "failure"}, or {@code "denied"}. */
        public AuditEventBuilder result(String result) {
            this.result = result != null ? result : "success";
            return this;
        }

        /** Optional human-readable detail message. */
        public AuditEventBuilder detail(String detail) {
            this.detail = detail;
            return this;
        }

        /**
         * Writes the audit event.  MDC keys are set and removed atomically
         * so that concurrent requests cannot bleed into each other.
         *
         * <p>The log {@code message} field is intentionally set to a compact JSON
         * string so that downstream Logstash {@code json { source => "message" }}
         * filters can parse it without error.  All fields are also emitted as
         * top-level structured arguments so that encoders which do not apply a
         * JSON filter still capture them correctly.
         */
        public void emit() {
            String traceId      = MDC.get(RequestCorrelationFilter.MDC_TRACE_ID);
            // Resolve actor: explicit override > X-Actor MDC key > fallback
            String resolvedActor = this.actor != null
                    ? this.actor
                    : (MDC.get(RequestCorrelationFilter.MDC_ACTOR) != null
                            ? MDC.get(RequestCorrelationFilter.MDC_ACTOR)
                            : "anonymous");

            // Build a JSON string for the message field so that Logstash
            // "json { source => 'message' }" filters can parse it successfully.
            // Without this, Logstash would receive the literal "audit_event"
            // string and emit: "invalid character 'a' looking for beginning of value".
            String resolvedTrace  = traceId != null ? traceId : "-";
            String resolvedDetail = detail  != null ? detail  : "";
            String jsonMessage = "{\"event.action\":\"" + escape(action)         + "\""
                    + ",\"actor\":\""      + escape(resolvedActor)  + "\""
                    + ",\"resource\":\""   + escape(resource)       + "\""
                    + ",\"result\":\""     + escape(result)         + "\""
                    + ",\"trace.id\":\""   + escape(resolvedTrace)  + "\""
                    + ",\"detail\":\""     + escape(resolvedDetail) + "\""
                    + "}";

            MDC.put("audit.action",   action);
            MDC.put("audit.actor",    resolvedActor);
            MDC.put("audit.resource", resource);
            MDC.put("audit.result",   result);
            try {
                auditLog.info(jsonMessage,
                        StructuredArguments.kv("event.action",   action),
                        StructuredArguments.kv("actor",          resolvedActor),
                        StructuredArguments.kv("resource",       resource),
                        StructuredArguments.kv("result",         result),
                        StructuredArguments.kv("trace.id",       resolvedTrace),
                        StructuredArguments.kv("@timestamp",     Instant.now().toString()),
                        StructuredArguments.kv("detail",         resolvedDetail));
            } finally {
                MDC.remove("audit.action");
                MDC.remove("audit.actor");
                MDC.remove("audit.resource");
                MDC.remove("audit.result");
            }
        }

        /** Minimal JSON string escaping for values embedded in the message field. */
        private static String escape(String value) {
            if (value == null) return "";
            return value.replace("\\", "\\\\")
                        .replace("\"", "\\\"")
                        .replace("\n", "\\n")
                        .replace("\r", "\\r")
                        .replace("\t", "\\t");
        }
    }
}
