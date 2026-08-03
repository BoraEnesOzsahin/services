# hiveos-integration

## Logging Contract

This service adheres to a standardized JSON logging format to enable efficient centralized logging and observability.

### Field Definitions
Every log entry is output as a single-line JSON object containing the following standard fields:
- `@timestamp`: The UTC timestamp of the log event.
- `level`: The severity level (e.g., INFO, WARN, ERROR).
- `service.name`: The name of the service (`hiveos-integration`).
- `environment`: The deployment environment (e.g., prod, dev). Driven by the `ENVIRONMENT` env var.
- `trace.id`: Distributed tracing ID (if present in MDC).
- `span.id`: Distributed tracing Span ID (if present in MDC).
- `correlation.id`: Request correlation ID (propagated or generated).
- `request.id`: Unique request ID (propagated or generated).
- `logger`: The logger name.
- `thread`: The thread name where the log event occurred.
- `message`: The actual log message.
- `error.type`: The exception class name (if an exception occurred).
- `error.message`: The exception message (if an exception occurred).
- `error.stack_trace`: The shortened exception stack trace (if an exception occurred).
- `http.method`: The HTTP method of the request (e.g., GET, POST).
- `url.path`: The matched URL path of the request (e.g., `/api/v1/devices/{id}`).
- `http.status_code`: The HTTP status code of the response.
- `event.duration_ms`: The duration of the request in milliseconds.

### Header Behavior
The service handles correlation headers as follows:
- **X-Correlation-ID**: Read from incoming requests. If missing or invalid, a new safe UUID is generated.
- **X-Request-ID**: Read from incoming requests. If missing or invalid, a new safe UUID is generated.
- **Echo**: Both headers are always echoed back in the HTTP response.
- **MDC**: These IDs are placed in the logging MDC during the request lifecycle and cleared afterwards.

### Sensitive Data Note
Do not log sensitive information such as PII, passwords, secrets, or raw authorization tokens in any log `message` or context fields. Always mask or exclude such data.

### Sample JSON Log Line
```json
{
  "@timestamp": "2026-07-16T21:00:00.000Z",
  "level": "INFO",
  "service.name": "hiveos-integration",
  "environment": "prod",
  "trace.id": "",
  "span.id": "",
  "correlation.id": "abc-123",
  "request.id": "xyz-789",
  "logger": "com.ayrotek.reckon.hiveosintegration.logging.RequestLoggingInterceptor",
  "thread": "http-nio-8080-exec-1",
  "message": "HTTP GET /dummy responded with 200 in 15ms",
  "http.method": "GET",
  "url.path": "/dummy",
  "http.status_code": 200,
  "event.duration_ms": 15
}
```
