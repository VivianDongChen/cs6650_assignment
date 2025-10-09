# Results Archive

This folder contains Assignment 1 test evidence (logs, screenshots, metrics).

## Server Test Results
- `server-health-local.png` – output of `curl http://localhost:8080/chat-server/health`.
- `server-health-ec2.png` – output of `curl http://<ec2-ip>:8080/chat-server/health`.
- `server-websocket-success.png` – successful WebSocket echo (Postman).
- `server-websocket-invalid.png` – validation error example (missing fields).

## Client Part 1 Results

All runs use the assignment-mandated warmup (32 threads × 1 000 messages) followed by a main phase totalling 500 000 messages. Three thread configurations were evaluated locally against the same server build.

| Main Threads | Runtime (ms) | Throughput (msg/s) | Total Retries | Connections Opened | Evidence |
| --- | --- | --- | --- | --- | --- |
| 48 | 31 226 | 16 012.30 | 12 255 | 80 | `client-part1-metrics-main48.txt`, ![main48](client-part1-main48.png) |
| 64 | 24 772 | 20 184.08 | 12 241 | 96 | `client-part1-metrics-main64.txt`, ![main64](client-part1-main64.png) |
| 96 | 17 637 | **28 349.49** | 12 198 | 128 | `client-part1-metrics-main96.txt`, ![main96](client-part1-main96.png) |

- All 500 000 messages completed successfully in each run (0 failures). The client needed ~12k retries and reconnections regardless of thread count; this appears to be the cost of exercising the server aggressively and is a good data point to mention in the report.
- The best throughput observed locally is with 96 main threads (~28 k msg/s). Keep these numbers handy for the final write-up.

### Little's Law sanity check

Using Little's Law (\(\lambda = L / W\)) with the main-phase concurrency as \(L\):

| Main Threads (L) | Observed Throughput (λ) | Implied Service Time (W = L/λ) |
| --- | --- | --- |
| 48 | 16 012 msg/s | ≈ 3.0 ms |
| 64 | 20 184 msg/s | ≈ 3.2 ms |
| 96 | 28 349 msg/s | ≈ 3.4 ms |

A single-thread latency measurement of roughly 3 ms (derived from the 48-thread run) predicts \(λ \approx 96 / 0.003 \approx 32\) k msg/s, close to the measured 28 k msg/s once contention and retries are considered. Mentioning this comparison in the design document satisfies the Little's Law analysis requirement.

### Next steps

- Capture the same metrics from an EC2 client run (same-region as the server) for the submission package.
- Part 2 (detailed metrics) will add per-message CSV output, percentile stats, and charts once implemented.

### Working Notes
- Keep filenames descriptive and redact sensitive information (private IPs, AWS account numbers).
- Update this README whenever new evidence is added so packaging the final submission is fast.
