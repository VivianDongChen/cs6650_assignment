package com.cs6650.chat.client.send;

import com.cs6650.chat.client.config.ClientConfig;
import com.cs6650.chat.client.metrics.MetricsRecorder;
import com.cs6650.chat.client.message.ChatMessage;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

/**
 * Coordinates warmup and main phase execution by submitting {@link SenderWorker} tasks to a
 * provided thread pool. Networking details are still TODO – the class currently exposes only the
 * high-level orchestration structure.
 */
public final class SenderOrchestrator {

    private final ClientConfig config;
    private final BlockingQueue<ChatMessage> queue;
    private final MetricsRecorder metrics;
    private final ExecutorService executor;

    public SenderOrchestrator(ClientConfig config,
                              BlockingQueue<ChatMessage> queue,
                              MetricsRecorder metrics,
                              ExecutorService executor) {
        this.config = config;
        this.queue = queue;
        this.metrics = metrics;
        this.executor = executor;
    }

    public void runWarmupAndMainPhase() {
        // TODO: submit warmup workers (32 threads × 1000 messages) and wait for completion.
        // TODO: submit main phase workers to process remaining messages until queue is drained.
    }

    public void shutdown() {
        // Placeholder for future resource cleanup.
    }
}
