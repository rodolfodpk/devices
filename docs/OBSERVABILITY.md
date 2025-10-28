# Observability

Comprehensive observability for the IoT Devices Management System with structured logging, metrics collection, and visualization.

## Overview

The application includes full observability stack with:
- **Structured Logging**: JSON-formatted logs with correlation IDs
- **Prometheus**: Metrics collection and time-series database
- **Grafana**: Visualization and dashboards
- **Spring Boot Actuator**: Health checks and metrics exposure

## Quick Start

### Start Observability Stack

```bash
# Start application with Prometheus and Grafana
make start-obs

# Access Grafana
make grafana
# URL: http://localhost:3000
# Login: admin/admin

# Access Prometheus
make prometheus
# URL: http://localhost:9090

# View metrics
make metrics
# URL: http://localhost:8080/actuator/metrics
```

### Available Endpoints

- **Application**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Health Check**: http://localhost:8080/actuator/health
- **Prometheus Metrics**: http://localhost:8080/actuator/prometheus
- **All Metrics**: http://localhost:8080/actuator/metrics
- **Grafana**: http://localhost:3000 (admin/admin)
- **Prometheus UI**: http://localhost:9090

## Structured Logging

### Configuration

Structured logging is configured via `logback-spring.xml`:

- **Development Profile**: Pretty-printed console output for readability
- **Production Profile**: JSON structured logs with Logstash encoder

### Log Format

**Development** (human-readable):
```
2025-01-28 18:00:00.123  INFO c.r.device.service.DeviceService : Device created successfully
```

**Production** (JSON):
```json
{
  "@timestamp": "2025-01-28T18:00:00.123Z",
  "level": "INFO",
  "logger": "com.rdpk.device.service.DeviceService",
  "message": "Device created successfully",
  "application": "devices",
  "thread": "http-nio-8080-exec-1",
  "trace": "12345678-1234-1234-1234-123456789012"
}
```

### Log Levels

Configurable per package and environment:

- **Application Code**: INFO, WARN, ERROR
- **Spring Framework**: INFO, WARN, ERROR
- **Database**: WARN, ERROR only
- **Resilience4j**: INFO, WARN, ERROR

### Logging Features

- **Correlation IDs**: Automatic request tracing
- **Structured Fields**: Consistent JSON schema
- **Context Propagation**: MDC context for correlation
- **Log Aggregation**: Ready for ELK stack or similar

## Prometheus Metrics

### Exposed Metrics

The application exposes comprehensive metrics:

#### HTTP Metrics
```
http_server_requests_seconds{method,uri,status}    # Request latency
http_server_requests_total{method,uri,status}       # Request count
```

#### JVM Metrics
```
jvm_memory_used_bytes{area}                         # Memory usage
jvm_threads_live_threads                            # Active threads
jvm_gc_pause_seconds{action}                        # GC pauses
```

#### Resilience4j Metrics
```
resilience4j_circuitbreaker_calls_total{state}      # Circuit breaker calls
resilience4j_circuitbreaker_failure_rate             # Failure rate
resilience4j_retry_calls_total{kind}                 # Retry attempts
resilience4j_timelimiter_calls_total{kind}           # Timeout occurrences
```

#### Database Metrics
```
r2dbc_pool_acquired                                  # Acquired connections
r2dbc_pool_pending                                   # Pending connections
r2dbc_pool_timeout                                   # Timeout events
```

### Configuration

Prometheus is configured to scrape the application:

```yaml
# monitoring/prometheus.yml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'devices-application'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['host.docker.internal:8080']
```

## Grafana Dashboards

### Pre-configured Dashboards

The application includes two pre-configured Grafana dashboards:

#### 1. Application Metrics Dashboard
**File:** `monitoring/dashboards/devices-application-dashboard.json`

**Panels:**
- HTTP Request Rate (requests/sec)
- HTTP Request Duration (p50, p95, p99)
- HTTP Status Codes (200, 400, 404, 500)
- JVM Memory Usage (heap, non-heap)
- JVM Threads (live, peak)
- Database Connection Pool
- Active Users

#### 2. Resilience4j Metrics Dashboard
**File:** `monitoring/dashboards/devices-resilience4j-dashboard.json`

**Panels:**
- Circuit Breaker State (CLOSED, OPEN, HALF_OPEN)
- Circuit Breaker Calls (success, failure, rejected)
- Failure Rate (percentage)
- Retry Attempts (successful, failed)
- Timeout Events (occurrences)

### Dashboard Provisioning

Dashboards are automatically loaded when Grafana starts:

```yaml
# monitoring/dashboards/dashboard.yml
apiVersion: 1
providers:
  - name: 'Devices Application'
    folder: 'Devices'
    type: file
    options:
      path: /etc/grafana/provisioning/dashboards
```

### Viewing Dashboards

1. **Start observability stack:**
   ```bash
   make start-obs
   ```

2. **Open Grafana:**
   ```bash
   make grafana
   # Or manually: http://localhost:3000
   ```

3. **Login:** admin / admin

4. **Navigate to Dashboards:**
   - Devices → Application Metrics
   - Devices → Resilience4j Metrics

## Metrics Interpretation

### Healthy System Indicators

**HTTP Metrics:**
- Request rate: Consistent with application load
- Duration p95: < 500ms
- Error rate: < 1%

**JVM Metrics:**
- Memory usage: < 80% of heap
- GC pauses: < 100ms
- Thread count: Stable

**Resilience4j Metrics:**
- Circuit breaker: CLOSED state
- Failure rate: < 1%
- Retry attempts: Minimal

### Unhealthy System Indicators

**HTTP Metrics:**
- Request duration spike (> 1s)
- Error rate increase (> 5%)
- 500 status codes

**JVM Metrics:**
- Memory usage > 90%
- Frequent GC pauses
- Thread count growing

**Resilience4j Metrics:**
- Circuit breaker: OPEN state
- High failure rate
- Frequent retries

## Actuator Endpoints

### Health Check

```bash
curl http://localhost:8080/actuator/health
```

Response:
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 500000000000,
        "free": 100000000000,
        "threshold": 10485760
      }
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

### Prometheus Metrics

```bash
curl http://localhost:8080/actuator/prometheus
```

Sample metrics:
```
# HELP http_server_requests_seconds Duration of HTTP server requests
# TYPE http_server_requests_seconds summary
http_server_requests_seconds_count{method="GET",uri="/api/v1/devices",status="200"} 1234.0
http_server_requests_seconds_sum{method="GET",uri="/api/v1/devices",status="200"} 567.8
http_server_requests_seconds_max{method="GET",uri="/api/v1/devices",status="200"} 0.123
```

## Alerting (Future)

Potential alert conditions:

- Circuit breaker OPEN for > 5 minutes
- Error rate > 5%
- Response time p95 > 1s
- Memory usage > 90%
- Database connection pool exhausted

## Troubleshooting

### Issue: No metrics in Grafana

**Solution:**
1. Verify Prometheus is scraping: http://localhost:9090/targets
2. Check application is running: http://localhost:8080/actuator/health
3. Verify Prometheus can reach application

### Issue: Dashboard not showing data

**Solution:**
1. Check time range in Grafana (top right)
2. Verify Prometheus datasource is configured
3. Check dashboard queries in Grafana

### Issue: Structured logs not appearing

**Solution:**
1. Verify `spring.profiles.active=prod` in application.properties
2. Check logback-spring.xml is on classpath
3. Verify logstash-logback-encoder dependency

## Docker Compose Services

```yaml
services:
  postgres:
    image: postgres:17.2
    environment:
      POSTGRES_DB: devices
      POSTGRES_USER: devices
      POSTGRES_PASSWORD: devices
    ports:
      - "5432:5432"

  prometheus:
    image: prom/prometheus:latest
    volumes:
      - ./monitoring/prometheus.yml:/etc/prometheus/prometheus.yml
    ports:
      - "9090:9090"

  grafana:
    image: grafana/grafana:latest
    volumes:
      - ./monitoring/datasources:/etc/grafana/provisioning/datasources
      - ./monitoring/dashboards:/var/lib/grafana/dashboards
    ports:
      - "3000:3000"
```

## Additional Resources

- [Spring Boot Actuator Docs](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Micrometer Documentation](https://micrometer.io/docs)
- [Prometheus Documentation](https://prometheus.io/docs/)
- [Grafana Documentation](https://grafana.com/docs/)

