# Client Part 1

This module implements the Assignment 1 **Part 2 (Client Part 1)** multithreaded WebSocket client.  
Goals:

- Simulate 500 000 chat messages against the server.
- Honor the two-phase workload (warmup: 32 threads × 1000 msgs → main phase).
- Produce “basic metrics” output (success/failed counts, runtime, throughput, connection stats).

## High-Level Architecture

| Component | Purpose |
| --- | --- |
| `MessageGenerator` (single thread) | Pre-generates messages into a bounded queue (50-message templates, randomised fields). |
| `SenderWorker` (pool of threads) | Consumes messages, maintains persistent WebSocket connection, handles retries/backoff. |
| `ConnectionManager` | Creates/recovers WebSocket sessions, records connection stats, applies pooling limits. |
| `MetricsRecorder` | Thread-safe counters and timers; exposes final report + optional interim logging. |
| `ClientApp` | CLI entry point; parses args (host, port, room count, warmup/main config) and orchestrates phases. |

## Implementation Roadmap

1. **Scaffold & config**
   - Confirm Maven exec plugin / shading strategy for runnable JAR.
   - Define configuration model (CLI args + defaults via properties/env).
2. **Message generation**
   - Implement randomisation rules; push into `LinkedBlockingQueue` (size e.g. 10 000 to avoid memory pressure).
3. **Connection pool**
   - Warmup: spawn 32 workers, each open new WebSocket (auto-close after 1000 msgs).
   - Main phase: optionally resize thread pool; reuse connections.
   - Auto-reconnect with jittered exponential backoff (max 5 attempts).
4. **Retry & error tracking**
   - Per-message retry loop; on 5 failures mark as “failed”.
   - Collect per-connection stats (opens, reconnects, failures).
5. **Metrics output**
   - Record start/end wall clock.
   - Counters: successes, failures, retries, total connections, reconnections.
   - Print summary to stdout (CSV/log friendly).
6. **Testing / validation**
   - Local run against localhost server (small message count first).
   - Full 500 k run to gather Part 1 screenshot (basic metrics).
   - Optional: stress test on EC2 client host as described in plan.

## Testing Checklist

- [ ] Unit-test random message generation (field ranges, distribution).
- [ ] Smoke test with 1 000 messages / 4 threads to validate queue/connection.
- [ ] Warmup phase metrics validated (32 × 1000).
- [ ] Full run metrics captured (success ≥ 500 000, failure == 0 ideally).
- [ ] EC2 client run (same region as server) + collect screenshot for results.
- [ ] Capture Little's Law estimate vs. actual throughput.

## Running the client (sample)

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
mvn -q clean package
$JAVA_HOME/bin/java -jar target/client-part1-1.0-SNAPSHOT-shaded.jar \
  --server-uri=ws://localhost:8080/chat/1 \
  --warmup-threads=32 \
  --warmup-messages-per-thread=1000 \
  --main-threads=64 \
  --total-messages=500000
```

Additional flags:

| Flag | Description | Default |
| --- | --- | --- |
| `--queue-capacity` | Bounded queue size between generator and senders | 10 000 |
| `--send-timeout` | ISO-8601 duration for awaiting server ack | `PT10S` |
| `--max-retries` | Max retries per message before counting as failure | 5 |
| `--initial-backoff` | First backoff duration (`PT0.1S`) | `PT0.1S` |
| `--max-backoff` | Maximum backoff duration | `PT5S` |

## TODO Backlog

- [ ] Wire CLI parser (Picocli or Apache Commons CLI).
- [ ] Implement WebSocket client (likely `java.net.http.WebSocket` or `Tyrus`).
- [ ] Backoff strategy helper.
- [ ] Structured logging (logback or slf4j-simple).
- [ ] Write instructions for build/run once implementation stabilises.
