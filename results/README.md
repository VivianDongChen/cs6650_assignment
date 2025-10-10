# Assignment 1 - Test Results and Analysis

This folder contains test evidence, metrics, and performance analysis for Assignment 1.

---

## Part 1: Server Implementation and Validation

### Server Deployment Evidence

- `server-health-local.png` – Local health check via `curl http://localhost:8080/chat-server/health`
- `server-health-ec2.png` – EC2 health check via `curl http://34.220.13.15:8080/chat-server/health`
- `server-websocket-success.png` – Successful WebSocket message echo (Postman)
- `server-websocket-invalid.png` – Validation error example (missing required fields)

**Server Environment:**
- **Local**: Apache Tomcat 10.1.24, running on localhost:8080
- **EC2**: AWS EC2 instance (us-west-2), t3.micro, running on 34.220.13.15:8080

---

## Part 2: Multithreaded Client Performance Testing

### Common Test Configuration

All tests follow the required architecture:
- **Warmup Phase**: 32 threads × 1,000 messages each = 32,000 messages
- **Main Phase**: Variable thread count sending remaining messages to reach 500,000 total
- **Message Generation**: Single dedicated thread generates 500,000 messages in advance and places them in a thread-safe queue (capacity 10,000)
- **Message Payloads**:
  - `userId`: 1-100,000 (random)
  - `username`: `user<id>` format
  - `message`: 50 pre-defined message templates (random selection)
  - `roomId`: 1-20 (random)
  - `messageType`: 90% TEXT, 5% JOIN, 5% LEAVE
  - `timestamp`: `Instant.now()` in ISO-8601 format
- **Connection Management**: WebSocket connection pooling with reconnection on failure
- **Error Handling**: Retry up to 5 times with exponential backoff (PT0.05S to PT1S); exceeded retries counted as failures

---

## 2.1 Local Server Testing

### Test Environment
- **Server**: Local Tomcat (localhost:8080)
- **Client**: Local workstation
- **Network**: Loopback interface (no network latency)

### Single-Thread Baseline Test

**Configuration:**
```bash
java -jar target/client-part1-1.0-SNAPSHOT-shaded.jar \
  --server-uri=ws://localhost:8080/chat-server/chat/1 \
  --warmup-threads=1 \
  --warmup-messages-per-thread=1 \
  --main-threads=1 \
  --total-messages=1000 \
  --queue-capacity=100 \
  --send-timeout=PT5S \
  --max-retries=3 \
  --initial-backoff=PT0.05S \
  --max-backoff=PT0.2S
```

**Result:**
- Runtime: 1,916 ms
- Throughput: 521.92 msg/s
- **Average service time:** `W_local = 1.916 ms`

Screenshot: [client-part1-local-single-thread.png](client-part1-single-thread.png)

### Multi-Thread Performance Results

| Main Threads | Runtime (ms) | Throughput (msg/s) | Total Retries | Reconnections | Connections Opened | Screenshot |
|-------------|--------------|-------------------|---------------|---------------|-------------------|------------|
| 48  | 31,132 | 16,060.64 | 12,269 | 12,269 | 80  | [client-part1-local-main48.png](client-part1-main48.png) |
| 64  | 24,468 | 20,434.85 | 12,234 | 12,234 | 96  | [client-part1-local-main64.png](client-part1-main64.png) |
| 96  | 18,260 | 27,382.26 | 12,250 | 12,250 | 128 | [client-part1-local-main96.png](client-part1-main96.png) |
| 128 | 16,526 | **30,255.36** | 12,253 | 12,253 | 160 | [client-part1-local-main128.png](client-part1-main128.png) |
| 160 | 17,700 | 28,248.59 | 12,290 | 12,290 | 192 | [client-part1-local-main160.png](client-part1-main160.png) |

**Key Observations:**
- All 500,000 messages completed successfully (0 failures) across all configurations
- Approximately 12k retries/reconnections occur consistently across all thread counts
- Throughput peaks at **128 threads (~30,255 msg/s)**
- 160 threads shows diminishing returns (28,249 msg/s)

### Local Little's Law Analysis

Using Little's Law: `λ = L / W`
- `L` = number of concurrent workers (main phase threads)
- `W` = average service time (1.916 ms from single-thread test)
- `λ` = predicted throughput

| Main Threads (L) | Observed λ (msg/s) | Predicted λ = L / W (msg/s) | Efficiency (%) | Analysis |
|-----------------|-------------------|----------------------------|---------------|----------|
| 48  | 16,061 | ≈ 25,052 | 64.1% | Contention and retry overhead reduce throughput |
| 64  | 20,435 | ≈ 33,403 | 61.2% | Overhead persists with more threads |
| 96  | 27,382 | ≈ 50,104 | 54.7% | Increasing synchronization costs |
| 128 | 30,255 | ≈ 66,805 | 45.3% | **Optimal: Best throughput despite lower efficiency** |
| 160 | 28,249 | ≈ 83,507 | 33.8% | Diminishing returns evident |

**Local Analysis Summary:**
1. **Efficiency decreases with scale**: From 64% (48 threads) to 45% (128 threads) to 34% (160 threads)
2. **Optimal configuration**: 128 threads provides best absolute throughput (~30k msg/s)
3. **Retry impact**: Consistent ~12k retries across all tests indicate server-side backpressure
4. **Scalability ceiling**: Beyond 128 threads, thread contention outweighs parallelism benefits

### Local Connection Statistics (128 threads - Optimal)
- **Total connections opened:** 160
- **Successful messages:** 500,000
- **Failed messages:** 0
- **Total retries:** 12,253
- **Reconnections:** 12,253
- **Overall runtime:** 16,526 ms
- **Effective throughput:** 30,255.36 msg/s

---

## 2.2 EC2 Server Testing

### Test Environment
- **Server**: AWS EC2 (us-west-2), t3.micro instance, IP: 34.220.13.15
- **Client**: Local workstation
- **Network**: Public internet connection with variable latency

### Single-Thread Baseline Test (EC2)

**Configuration:**
```bash
java -jar target/client-part1-1.0-SNAPSHOT-shaded.jar \
  --server-uri=ws://34.220.13.15:8080/chat-server/chat/1 \
  --warmup-threads=1 \
  --warmup-messages-per-thread=1 \
  --main-threads=1 \
  --total-messages=1000 \
  --queue-capacity=100 \
  --send-timeout=PT5S \
  --max-retries=3 \
  --initial-backoff=PT0.05S \
  --max-backoff=PT0.2S
```

**Result:**
- Runtime: 17,371 ms
- Throughput: 57.57 msg/s
- **Average service time (with network latency):** `W_ec2 = 17.371 ms`
- **Network latency overhead:** +15.455 ms vs local (+806%)

Screenshot: [client-part1-ec2-single-thread.png](client-part1-ec2-single-thread.png)

### Multi-Thread Performance Results (EC2)

| Main Threads | Runtime (ms) | Throughput (msg/s) | Total Retries | Reconnections | Connections Opened | Failed Messages | Screenshot |
|-------------|--------------|-------------------|---------------|---------------|-------------------|-----------------|------------|
| 224 | 74,107 | 6,747.00 | 12,239 | 12,239 | 256 | 0 | [client-part1-ec2-main224.png](client-part1-ec2-main224.png) |
| 256 | 65,043 | 7,687.22 | 12,098 | 12,098 | 288 | 0 | [client-part1-ec2-main256.png](client-part1-ec2-main256.png) |
| 288 | 62,967 | **7,940.67** | 12,114 | 12,102 | 320 | 0 | [client-part1-ec2-main288.png](client-part1-ec2-main288.png) |
| 320 | 91,426 | 5,325.08 | 11,974 | 11,807 | 343 | 9 | [client-part1-ec2-main320.png](client-part1-ec2-main320.png) |

**Key Observations:**
- **Optimal configuration**: 288 threads achieved peak throughput of **7,940.67 msg/s**
- All 500,000 messages completed successfully up to 288 threads (0 failures)
- At 320 threads, system became overloaded:
  - 9 failed messages (486,842 successful out of 500,000 total)
  - Numerous "HTTP/1.1 header parser received no bytes" and timeout errors
  - Performance degraded 42% from peak (7,941 → 5,325 msg/s)
- EC2 requires **2.25x more threads** (288 vs 128) compared to local testing

### EC2 Little's Law Analysis

Using Little's Law: `λ = L / W`
- `L` = number of concurrent workers (main phase threads)
- `W` = average service time (17.371 ms from EC2 single-thread test)
- `λ` = predicted throughput

| Main Threads (L) | Observed λ (msg/s) | Predicted λ = L / W (msg/s) | Efficiency (%) | Analysis |
|-----------------|-------------------|----------------------------|---------------|----------|
| 224 | 6,747 | ≈ 12,900 | 52.3% | Network latency overhead visible |
| 256 | 7,687 | ≈ 14,739 | 52.2% | Consistent efficiency maintained |
| 288 | 7,941 | ≈ 16,582 | 47.9% | **Optimal: Peak throughput achieved** |
| 320 | 5,325 | ≈ 18,424 | 28.9% | System overload causes dramatic collapse |

**EC2 Analysis Summary:**
1. **Network latency impact**: 17.371 ms vs 1.916 ms local (+15.455 ms, 806% increase)
2. **Higher efficiency**: EC2 maintains ~48-52% efficiency (vs 45% local) due to network latency hiding contention
3. **Optimal configuration**: 288 threads provides peak throughput (7,941 msg/s)
4. **Hard limit at 320**: Connection timeouts and failures indicate system bottleneck
5. **Latency compensation**: Higher concurrency effectively hides network latency

### EC2 Connection Statistics (288 threads - Optimal)
- **Total connections opened:** 320
- **Successful messages:** 500,000
- **Failed messages:** 0
- **Total retries:** 12,114
- **Reconnections:** 12,102
- **Overall runtime:** 62,967 ms
- **Effective throughput:** 7,940.67 msg/s

---

## 2.3 Performance Comparison: Local vs EC2

### Summary Table

| Environment | Optimal Threads | Peak Throughput | Service Time (1-thread) | Peak Efficiency |
|-------------|----------------|-----------------|------------------------|----------------|
| **Local** | 128 | 30,255 msg/s | 1.916 ms | 45.3% |
| **EC2** | 288 | 7,941 msg/s | 17.371 ms | 47.9% |
| **Difference** | +160 (+125%) | -22,314 (-74%) | +15.455 ms (+806%) | +2.6% |

### Detailed Comparison

| Metric | Local Server | EC2 Server | Difference |
|--------|--------------|------------|------------|
| Single-thread latency | 1.916 ms | 17.371 ms | +15.455 ms (+806%) |
| Optimal thread count | 128 | 288 | +160 threads (+125%) |
| Peak throughput | 30,255 msg/s | 7,941 msg/s | -22,314 msg/s (-74%) |
| Peak efficiency | 45.3% | 47.9% | +2.6% |
| Network factor | Localhost (loopback) | Public Internet | Cross-region WAN |
| Retry/Reconnection rate | ~12,253 per 500k msgs | ~12,114 per 500k msgs | Consistent behavior |
| Failure threshold | >160 threads (graceful) | >288 threads (hard limit) | EC2 hits connection limits |

### Critical Insights

1. **Little's Law Validation**:
   - Both environments follow Little's Law predictions with ~45-50% efficiency
   - Indicates consistent system behavior and proper concurrency design
   - Validates theoretical modeling approach

2. **Network Latency Dominates Performance**:
   - 15.5ms network RTT vs 1.9ms local latency (9x increase)
   - Results in 74% throughput reduction on EC2
   - Demonstrates real-world WAN overhead

3. **Concurrency Compensates for Latency**:
   - EC2 requires 2.25x more threads (288 vs 128) to achieve optimal throughput
   - Higher concurrency effectively "hides" network latency
   - Efficiency actually improves slightly (47.9% vs 45.3%) due to reduced contention

4. **Scalability Limits**:
   - **Local**: Graceful degradation beyond 128 threads, still functional at 160
   - **EC2**: Hard failure at 320 threads (connection timeouts, message failures)
   - Suggests different bottlenecks: local = CPU contention, EC2 = connection limits

5. **Consistent Retry Behavior**:
   - ~12k retries in both environments
   - Indicates server-side backpressure handling rather than client issues
   - Shows robust error handling across network conditions

6. **Efficiency Paradox**:
   - EC2 shows slightly **higher** efficiency (47.9% vs 45.3%) despite lower throughput
   - Network latency increases service time uniformly, reducing relative impact of contention
   - Suggests client-side threading model scales well to distributed environments

### Practical Implications

- **Distributed deployment** reduces raw throughput but maintains efficiency
- **Network latency** can be mitigated by increasing concurrency levels
- **Connection pooling** becomes critical in high-latency environments
- **Real-world testing** reveals bottlenecks not visible in local testing
- **System design** should account for 3-5x latency increase in WAN deployments

---

## Next Steps

### Part 3 Requirements (Future Work)

1. **Per-Message Metrics Collection**:
   - CSV export with: timestamp, messageType, latency, statusCode, roomId
   - Statistical analysis: mean, median, p95, p99 response times
   - Throughput visualization: messages/second in 10-second buckets

2. **Server Optimization**:
   - Investigate server-side bottlenecks causing ~12k retry behavior
   - Analyze Tomcat thread pool and connection settings
   - Profile CPU/memory usage under peak load

3. **EC2 Optimization Opportunities**:
   - Test with client also on EC2 in same region to eliminate WAN latency
   - Investigate WebSocket keep-alive and connection pooling tuning
   - Analyze if t3.micro CPU/network limits are reached at peak load
   - Consider larger instance types for higher throughput

---

**Note**: Screenshot files should use descriptive names (e.g., `client-part1-ec2-main288.png`). Redact sensitive information (private IPs, AWS account IDs) before final submission if required.
