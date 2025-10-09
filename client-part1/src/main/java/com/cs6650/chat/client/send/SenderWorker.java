package com.cs6650.chat.client.send;

import com.cs6650.chat.client.config.ClientConfig;
import com.cs6650.chat.client.metrics.MetricsRecorder;
import com.cs6650.chat.client.message.ChatMessage;
import java.util.concurrent.BlockingQueue;

/**
 * Worker responsible for sending messages over a WebSocket connection. Concrete WebSocket
 * plumbing has not been implemented yet; this class currently provides the structure and metrics
 * wiring.
 */
public final class SenderWorker implements Runnable {

    private final ClientConfig config;
    private final BlockingQueue<ChatMessage> queue;
    private final MetricsRecorder metrics;
    private final String name;

    public SenderWorker(ClientConfig config,
                        BlockingQueue<ChatMessage> queue,
                        MetricsRecorder metrics,
                        String name) {
        this.config = config;
        this.queue = queue;
        this.metrics = metrics;
        this.name = name;
    }

    @Override
    public void run() {
        // TODO: establish WebSocket connection, consume messages from queue, send with retry logic.
        System.out.printf("[TODO] SenderWorker %s awaiting WebSocket implementation.%n", name);
    }
}
