# Results Archive

This folder contains Assignment 1 test evidence (logs, screenshots, metrics).

## Server Test Results
- `server-health-local.png` – output of `curl http://localhost:8080/chat-server/health`.
- `server-health-ec2.png` – output of `curl http://<ec2-ip>:8080/chat-server/health`.
- `server-websocket-success.png` – successful WebSocket echo (Postman).
- `server-websocket-invalid.png` – validation error example (missing fields).

## Client Part 1 (coming soon)
- Add warm-up run logs, throughput summary, and “Part 1 output (basic metrics)” screenshot once the client is implemented.

## Client Part 2 (coming soon)
- Add metrics CSV files, percentile charts, and analysis notes here.

### Working Notes
- Keep filenames descriptive and redact sensitive information (private IPs, AWS account numbers).
- Update this README whenever new evidence is added so packaging the final submission is fast.
