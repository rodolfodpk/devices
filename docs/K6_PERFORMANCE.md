# K6 Performance Testing

Comprehensive performance testing suite for the IoT Devices Management System using K6.

## Overview

K6 is used to validate system behavior under various load conditions and verify Resilience4j circuit breakers, retries, and rate limiters.

## Test Scenarios

### 1. Smoke Test
**Purpose:** Validate basic functionality with minimal load

```bash
make k6-smoke
```

- **VUs:** 5
- **Duration:** 1 minute (30s ramp-up, 30s steady)
- **Workload:** 100% GET requests (read-only)
- **Thresholds:** p95 < 200ms, p99 < 500ms, error rate < 1%
- **Goal:** Verify system responds correctly under minimal load

**Sample Terminal Output:**
```
💨 Running k6 smoke test...
          /\      |‾‾| /‾‾/   /‾‾/
     /\  /  \     |  |/  /   /  /
    /  \/    \    |     (   /   ‾‾\
   /          \   |  |\  \ |  (‾)  |
  / __________ \  |__| \__\ \_____/ .io

  execution: local
     script: k6/scripts/smoke-test.js
     output: -

  scenarios: (100.00%) 1 scenario, 5 max VUs, 1m0s max duration (incl. graceful stop):
           * default: Up to 5 VUs for 1m0s over 3 stages (gracefulRampDown: 30s, gracefulStop: 30s)

     ✗ create device status is 201
      ↳  0%    — ✓ 0   ✗ 0

     ✓ get all devices status is 200
     ✓ get all devices returns array
     ✓ get by brand status is 200
     ✓ get by brand returns array
     ✓ get by state status is 200
     ✓ get by state returns array

     checks.........................: 100.00% ✓ 1204  ✗ 0
     data_received..................: 234 kB  3.9 kB/s
     data_sent......................: 86 kB   1.4 kB/s
     http_req_blocked...............: avg=328.82µs min=3µs    med=16µs   max=14.47ms p(90)=28µs   p(95)=48µs
     http_req_connecting............: avg=188.69µs min=0s     med=0s     max=8.53ms  p(90)=0s     p(95)=0s
     http_req_duration..............: avg=8.18ms   min=2.05ms  med=7.15ms max=49.73ms p(90)=12.37ms p(95)=15.38ms
     http_req_failed................: 0.00%   ✓ 0    ✗ 602
     http_req_receiving.............: avg=41.13µs  min=12µs   med=37µs   max=328µs   p(90)=66µs   p(95)=89µs
     http_req_sending...............: avg=39.47µs  min=11µs   med=36µs   max=576µs   p(90)=60µs   p(95)=82µs
     http_req_tls_handshaking.......: avg=0s       min=0s     med=0s     max=0s     p(90)=0s     p(95)=0s
     http_req_waiting...............: avg=8.1ms    min=2.02ms med=7.11ms max=49.49ms p(90)=12.29ms p(95)=15.27ms
     http_reqs......................: 602     10.033627/s
     iteration_duration.............: avg=1.01s    min=1s     med=1s     max=1.05s  p(90)=1.01s   p(95)=1.02s
     iterations.....................: 602     10.033627/s
     vus............................: 5       min=1   max=5
     vus_max........................: 5       min=1   max=5

running (1m00.3s), 5/5 VUs (duration: 1m0s, passed: 0/5)
```

### 2. Load Test
**Purpose:** Simulate normal production load

```bash
make k6-load
```

- **VUs:** 50
- **Duration:** 5 minutes (1m ramp-up, 4m steady)
- **Workload Distribution:**
  - 40% GET all devices
  - 20% GET by ID
  - 15% GET by brand
  - 10% POST create
  - 10% PUT update
  - 5% DELETE
- **Thresholds:** p95 < 500ms, p99 < 1000ms, error rate < 1%
- **Goal:** Verify system stability and latency under steady load

**Sample Terminal Output:**
```
📊 Running k6 load test...

     ✓ get all devices status is 200
     ✓ get by brand status is 200
     ✓ create device status is 201
     ✓ create device has ID
     ✓ delete device status is valid

     checks.........................: 100.00% ✓ 8762  ✗ 0
     data_received..................: 3.4 MB  11 kB/s
     data_sent......................: 1.5 MB  4.9 kB/s
     http_req_duration..............: avg=28.45ms  min=2.12ms  med=25.47ms max=312.18ms p(90)=45.23ms p(95)=55.89ms
     http_req_failed................: 0.00%   ✓ 0     ✗ 4381
     http_reqs......................: 4381    14.593079/s
     iterations.....................: 876     2.916806/s
     vus............................: 50      min=1   max=50
     vus_max........................: 50      min=1   max=50

running (5m00.2s), 50/50 VUs (duration: 5m0s, passed: 0/50)
```

### 3. Stress Test
**Purpose:** Find system breaking point

```bash
make k6-stress
```

- **VUs:** Ramp from 10 → 100 → 200 → 300
- **Duration:** 20 minutes (gradual ramp)
- **Thresholds:** p95 < 1000ms, p99 < 2000ms, error rate < 5%
- **Goal:** Identify max throughput and failure points

### 4. Spike Test
**Purpose:** Test Resilience4j under sudden traffic surges

```bash
make k6-spike
```

- **VUs:** Spike from 10 → 200 → 10
- **Duration:** 5 minutes (rapid spikes)
- **Thresholds:** p95 < 1000ms, p99 < 2000ms, error rate < 2%
- **Goal:** Verify circuit breakers activate during spikes

## Running Tests

### Prerequisites

1. **Start the application with K6 profile:**
   ```bash
   make start-k6
   ```

2. **Install K6** (if not already installed):
   ```bash
   # macOS
   brew install k6
   
   # Linux
   sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys C5AD17C747E3415A3642D57D77C6C491D6AC1D69
   echo "deb https://dl.k6.io/deb stable main" | sudo tee /etc/apt/sources.list.d/k6.list
   sudo apt-get update
   sudo apt-get install k6
   
   # Windows
   choco install k6
   ```

### Available Commands

```bash
make k6-smoke       # Run smoke test (5 VUs, 1 min)
make k6-load        # Run load test (50 VUs, 5 min)
make k6-stress      # Run stress test (10→300 VUs, 20 min)
make k6-spike       # Run spike test (10→200 VUs, 5 min)
```

### Running All Tests Sequentially

```bash
make k6-test        # Run all K6 tests in sequence
```

## Success Criteria

### Smoke Test
- ✅ 100% success rate
- ✅ Response time p95 < 200ms
- ✅ Zero errors

### Load Test
- ✅ > 99% success rate
- ✅ Response time p95 < 500ms
- ✅ No memory leaks
- ✅ Consistent performance

### Stress Test
- ✅ Identify breaking point (VUs where success rate drops)
- ✅ Document max concurrent users supported
- ✅ Verify graceful degradation

### Spike Test
- ✅ Circuit breaker activates during spike
- ✅ System recovers after spike
- ✅ No data corruption

## Observability During Tests

To monitor system behavior during K6 tests:

1. **Start observability stack:**
   ```bash
   make start-obs
   ```

2. **Open Grafana dashboards:**
   ```bash
   make grafana
   # Login: admin/admin
   ```

3. **Monitor metrics:**
   - **HTTP Metrics:** Request rate, latency, status codes
   - **Resilience4j Metrics:** Circuit breaker state, retry attempts
   - **JVM Metrics:** Memory usage, thread count
   - **Database Metrics:** Connection pool, query duration

## Configuration

K6 tests run against the application with `application-k6.properties` profile, which has:
- Rate limits removed for performance testing
- Circuit breaker thresholds relaxed
- Extended timeouts for high load
- No rate limiter restrictions

## Interpreting Results

### Key Metrics

- **http_reqs:** Total HTTP requests made
- **http_req_duration:** Request latency (p50, p95, p99)
- **http_req_failed:** Failure rate percentage
- **iterations:** Number of test iterations completed
- **vus:** Current virtual users
- **data_received/data_sent:** Network traffic

### Threshold Validation

- **p(95) < 500ms:** 95% of requests complete in under 500ms
- **p(99) < 1000ms:** 99% of requests complete in under 1 second
- **rate < 0.01:** Less than 1% of requests fail

## Troubleshooting

**Issue:** Tests fail with connection refused
- **Solution:** Ensure application is running (`make start-k6`)

**Issue:** High error rate during tests
- **Solution:** Check application logs (`make logs`) and Grafana dashboards for circuit breaker activation

**Issue:** K6 not found
- **Solution:** Install K6 using platform-specific instructions above

## Additional Resources

- [K6 Documentation](https://k6.io/docs/)
- [K6 Best Practices](https://k6.io/docs/using-k6-browser/best-practices/)
- [Resilience4j Configuration](https://resilience4j.readme.io/docs/getting-started-3)

