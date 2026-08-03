# Cold Wallet Manager Service

## Logging Contract

This service uses a structured JSON logging format via Logback, specifically using `logstash-logback-encoder`. 

### Field Definitions
Every log line will contain the following fields where applicable:
- `@timestamp`: The time the log event occurred.
- `level`: The log level (e.g., INFO, ERROR).
- `service.name`: The name of the service (`cold-wallet-manager`).
- `environment`: The environment the service is running in (defaults to `unknown`, overridden by `ENVIRONMENT` variable).
- `trace.id` / `span.id`: Tracing identifiers (if distributed tracing is configured).
- `correlation.id`: Request correlation ID to track a flow across multiple services.
- `request.id`: Unique identifier for an individual HTTP request.
- `logger`: The logger name.
- `thread`: The thread name logging the event.
- `message`: The actual log message.
- `error.type` / `error.message` / `error.stack_trace`: Captured on ERROR level logs with exceptions.
- `http.method` / `url.path` / `http.status_code` / `event.duration_ms`: Available on the final HTTP request completion log.

### Header Behavior
The service reads `X-Correlation-ID` and `X-Request-ID` headers from incoming requests.
- Valid UUIDs are propagated.
- If missing or invalid, safe UUIDs are generated automatically.
- These headers are echoed back in the HTTP response.

### Sensitive Data Note
Do not log sensitive data (PII, passwords, private keys, tokens) in any fields. The logging interceptor only logs metadata like HTTP methods and paths, avoiding request/response bodies. Ensure any business logic logging follows the same guidelines.

### Sample JSON Log Line
```json
{
  "@timestamp": "2026-07-16T10:00:00.000+00:00",
  "level": "INFO",
  "service.name": "cold-wallet-manager",
  "environment": "production",
  "correlation.id": "123e4567-e89b-12d3-a456-426614174000",
  "request.id": "123e4567-e89b-12d3-a456-426614174001",
  "logger": "com.ayrotek.coldwalletmanagerservice.logging.HttpRequestLoggingInterceptor",
  "thread": "http-nio-8080-exec-1",
  "message": "HTTP request completed",
  "http.method": "GET",
  "url.path": "/api/v1/wallets",
  "http.status_code": "200",
  "event.duration_ms": "45"
}
```
