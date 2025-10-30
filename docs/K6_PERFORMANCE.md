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

         /\      Grafana   /‾‾/  
    /\  /  \     |\  __   /  /   
   /  \/    \    | |/ /  /   ‾‾\ 
  /          \   |   (  |  (‾)  |
 / __________ \  |_|\_\  \_____/ 

     execution: local
        script: k6/scripts/smoke-test.js
        output: -

     scenarios: (100.00%) 1 scenario, 5 max VUs, 1m30s max duration
              * default: Up to 5 looping VUs for 1m0s over 2 stages

  █ THRESHOLDS 

    http_req_duration
    ✓ 'p(95)<200' p(95)=2.12ms
    ✓ 'p(99)<500' p(99)=3.77ms

    http_req_failed
    ✓ 'rate<0.01' rate=0.00%

  █ TOTAL RESULTS 

    checks_total.......................: 361854  6030.978704/s
    checks_succeeded...................: 100.00% 361854 out of 361854
    checks_failed......................: 0.00%   0 out of 361854

    ✓ get all devices status is 200
    ✓ get all devices returns array
    ✓ get by brand status is 200
    ✓ get by brand returns array
    ✓ get by state status is 200
    ✓ get by state returns array

    HTTP
    http_req_duration........................................: avg=1.19ms min=606µs    med=998µs  max=64.38ms p(90)=1.71ms p(95)=2.12ms
    http_req_failed.........................................: 0.00%  0 out of 180927
    http_reqs................................................: 180927 3015.489352/s

    EXECUTION
    iteration_duration........................................: avg=1.23ms min=631.16µs med=1.03ms max=64.45ms p(90)=1.78ms p(95)=2.19ms
    iterations................................................: 180927 3015.489352/s
    vus........................................................: 5      min=1           max=5
    vus_max....................................................: 5      min=5           max=5

    NETWORK
    data_received............................................: 13 MB  217 kB/s
    data_sent................................................: 15 MB  253 kB/s

running (1m00.0s), 0/5 VUs, 180927 complete and 0 interrupted iterations
default ✓ [======================================] 0/5 VUs  1m0s
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

         /\      Grafana   /‾‾/  
    /\  /  \     |\  __   /  /   
   /  \/    \    | |/ /  /   ‾‾\ 
  /          \   |   (  |  (‾)  |
 / __________ \  |_|\_\  \_____/ 

     execution: local
        script: k6/scripts/load-test.js
        output: -

     scenarios: (100.00%) 1 scenario, 50 max VUs, 5m30s max duration
              * default: Up to 50 looping VUs for 5m0s over 2 stages

  █ THRESHOLDS 

    http_req_duration
    ✓ 'p(95)<500' p(95)=28.89ms
    ✓ 'p(99)<1000' p(99)=71.9ms

      {name:CreateDevice}
      ✓ 'p(95)<300' p(95)=19.42ms

      {name:GetAllDevices}
      ✓ 'p(95)<200' p(95)=30.95ms

      {name:GetDeviceById}
      ✓ 'p(95)<100' p(95)=17.86ms

    http_req_failed
    ✓ 'rate<0.01' rate=0.00%

  █ TOTAL RESULTS 

    checks_total.......................: 26024   86.553491/s
    checks_succeeded...................: 100.00% 26024 out of 26024
    checks_failed......................: 0.00%   0 out of 26024

    ✓ get all devices status is 200
    ✓ create device status is 201
    ✓ create device has ID
    ✓ delete device status is valid
    ✓ get by ID status is 200
    ✓ get by ID returns device
    ✓ get by brand status is 200
    ✓ get by brand returns array
    ✓ update device status is valid
    ✓ update device returns device or error

    HTTP
    http_req_duration........................................: avg=12.63ms min=802µs  med=10.45ms max=192.4ms  p(90)=22.57ms p(95)=28.89ms
      { expected_response:true }............................: avg=12.63ms min=802µs  med=10.45ms max=192.4ms  p(90)=22.57ms p(95)=28.89ms
      { name:CreateDevice }.................................: avg=8.16ms  min=1.2ms  med=5.45ms  max=140.89ms p(90)=14.72ms p(95)=19.42ms
      { name:GetAllDevices }................................: avg=15.73ms min=2.29ms med=13.12ms max=192.4ms  p(90)=24.77ms p(95)=30.95ms
      { name:GetDeviceById }................................: avg=6.57ms  min=802µs  med=3.92ms  max=143.95ms p(90)=12.72ms p(95)=17.86ms
    http_req_failed.........................................: 0.00%  0 out of 13328
    http_reqs................................................: 13328  44.327733/s

    EXECUTION
    iteration_duration........................................: avg=1.01s   min=1s     med=1.01s   max=1.19s    p(90)=1.02s   p(95)=1.03s
    iterations................................................: 13328  44.327733/s
    vus........................................................: 50     min=1          max=50
    vus_max....................................................: 50     min=50          max=50

    NETWORK
    data_received............................................: 299 MB 995 kB/s
    data_sent................................................: 1.4 MB 4.5 kB/s

running (5m00.7s), 00/50 VUs, 13328 complete and 0 interrupted iterations
default ✓ [======================================] 00/50 VUs  5m0s
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

1. **Start observability stack with K6 profile:**
   ```bash
   make start-k6-obs
   ```
   This automatically:
   - Cleans all volumes (`docker-compose down -v`) for fresh database state
   - Starts PostgreSQL, Prometheus, and Grafana
   - Starts the application with K6 testing profile
   - Waits for application health check

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

## Understanding K6 Terminal Output

When you run a K6 test, you'll see structured output in your terminal. Here's how to interpret it:

### Output Structure

K6 output consists of several sections:

1. **Header:** K6/Grafana logo and basic execution info
2. **Scenarios:** Test configuration and stages
3. **Thresholds:** Pass/fail status of defined thresholds
4. **Total Results:** Comprehensive metrics organized by category

### Sample Output Breakdown

Here's what you'll see when running a test:

```
         /\      Grafana   /‾‾/  
    /\  /  \     |\  __   /  /   
   /  \/    \    | |/ /  /   ‾‾\ 
  /          \   |   (  |  (‾)  |
 / __________ \  |_|\_\  \_____/ 

     execution: local
        script: k6/scripts/smoke-test.js
        output: -

     scenarios: (100.00%) 1 scenario, 5 max VUs, 1m30s max duration
              * default: Up to 5 looping VUs for 1m0s over 2 stages
```

**Explanation:**
- `execution: local` - Test runs locally (not distributed)
- `script:` - Path to the test script
- `scenarios:` - Number of VUs, duration, and stages configured

### Thresholds Section

```
  █ THRESHOLDS 

    http_req_duration
    ✓ 'p(95)<200' p(95)=2.12ms
    ✓ 'p(99)<500' p(99)=3.77ms

    http_req_failed
    ✓ 'rate<0.01' rate=0.00%
```

**What it means:**
- ✓ = Threshold passed, ✗ = Threshold failed
- Shows actual measured values vs. defined thresholds
- Critical for understanding if your system meets performance goals

### Total Results Section

#### Checks Metrics

```
    checks_total.......................: 361854  6030.978704/s
    checks_succeeded...................: 100.00% 361854 out of 361854
    checks_failed......................: 0.00%   0 out of 361854
```

- **checks_total:** Total number of assertions (checks) executed
- **checks_succeeded:** Percentage and count of passed checks
- **checks_failed:** Percentage and count of failed checks
- The number after the colon shows the rate per second

#### HTTP Metrics

```
    HTTP
    http_req_duration........................................: avg=1.19ms min=606µs    med=998µs  max=64.38ms p(90)=1.71ms p(95)=2.12ms
    http_req_failed...........................................: 0.00%  0 out of 180927
    http_reqs................................................: 180927 3015.489352/s
```

**Key HTTP Metrics Explained:**

- **http_req_duration:** Total time for HTTP requests
  - `avg` - Average response time
  - `min` - Fastest request
  - `med` - Median (p50, 50th percentile)
  - `max` - Slowest request
  - `p(90)` - 90th percentile (90% of requests faster than this)
  - `p(95)` - 95th percentile (95% of requests faster than this)
  - `p(99)` - 99th percentile (99% of requests faster than this)

- **http_req_failed:** Percentage and count of failed requests
  - Shows errors, timeouts, or non-2xx/3xx status codes

- **http_reqs:** Total requests and requests per second
  - Throughput metric

#### Execution Metrics

```
    EXECUTION
    iteration_duration........................................: avg=1.23ms min=631.16µs med=1.03ms max=64.45ms p(90)=1.78ms p(95)=2.19ms
    iterations................................................: 180927 3015.489352/s
    vus........................................................: 5      min=1           max=5
    vus_max....................................................: 5      min=5           max=5
```

**Explanation:**

- **iteration_duration:** Time to complete one full test iteration
  - Includes all operations within a single VU iteration

- **iterations:** Total iterations completed and rate per second
  - One iteration = one execution of the `default` function in your test script

- **vus:** Current number of active virtual users
  - `min`/`max` show the range during the test

- **vus_max:** Maximum virtual users that were active
  - Useful for verifying the test reached the target VU count

#### Network Metrics

```
    NETWORK
    data_received...................................................: 13 MB  217 kB/s
    data_sent.......................................................: 15 MB  253 kB/s
```

- **data_received:** Total bytes received from server and average rate
- **data_sent:** Total bytes sent to server and average rate
- Helps identify bandwidth usage and potential network bottlenecks

### Status Line

At the bottom, you'll see:

```
running (1m00.0s), 0/5 VUs, 180927 complete and 0 interrupted iterations
default ✓ [======================================] 0/5 VUs  1m0s
```

**Reading the status line:**
- `running (X)` - Elapsed test time
- `0/5 VUs` - Current active VUs / Maximum VUs
- `180927 complete` - Number of iterations completed
- `0 interrupted` - Iterations that were interrupted (due to test stopping)
- `default ✓` - Scenario name and completion status
- Progress bar shows test progress
- `1m0s` - Total test duration

### Understanding Percentiles

Percentiles tell you the distribution of response times:

- **p50 (median):** Half of requests are faster, half are slower
- **p90:** 90% of requests completed in this time or faster
- **p95:** 95% of requests completed in this time or faster (commonly used SLA)
- **p99:** 99% of requests completed in this time or faster (catches outliers)

**Example:** `p(95)=28.89ms` means 95% of requests completed in 28.89ms or less.

### Interpreting Results

#### Good Results Indicators

- ✓ All thresholds passed
- `http_req_failed: 0.00%` - No failed requests
- `checks_failed: 0.00%` - All assertions passed
- Percentile values within acceptable ranges (p95 < threshold)

#### Warning Signs

- ✗ Failed thresholds - System not meeting performance goals
- High `http_req_failed` rate - Requests failing or timing out
- High `p(99)` relative to `p(95)` - Large variance (outliers)
- `checks_failed > 0%` - Some assertions are failing

#### Comparing Tests

When comparing test runs:
1. Check threshold pass/fail status
2. Compare p95 and p99 values
3. Monitor error rates (`http_req_failed`)
4. Verify throughput (`http_reqs` per second)
5. Check if VUs reached target (`vus_max`)

## Interpreting Results

### Key Metrics Summary

- **http_reqs:** Total HTTP requests made and requests per second (throughput)
- **http_req_duration:** Request latency with percentiles (avg, min, med, max, p90, p95)
- **http_req_failed:** Failure rate percentage and count
- **iterations:** Number of test iterations completed and rate
- **vus/vus_max:** Current and maximum virtual users active
- **data_received/data_sent:** Network traffic volume and bandwidth
- **checks_total/succeeded/failed:** Assertion results and pass rate

### Threshold Validation

K6 automatically validates thresholds defined in your test:

- **✓ `p(95)<500` with `p(95)=28.89ms`** - 95th percentile is 28.89ms, which is under 500ms threshold ✓
- **✓ `p(99)<1000` with `p(99)=71.9ms`** - 99th percentile is 71.9ms, which is under 1000ms threshold ✓
- **✓ `rate<0.01` with `rate=0.00%`** - Failure rate is 0.00%, which is under 1% threshold ✓

If any threshold fails, the test will exit with a non-zero status code.

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

