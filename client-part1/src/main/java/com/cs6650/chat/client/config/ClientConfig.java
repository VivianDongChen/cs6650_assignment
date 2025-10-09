package com.cs6650.chat.client.config;

import java.net.URI;
import java.time.Duration;

public final class ClientConfig {

    private final URI serverUri;
    private final int warmupThreads;
    private final int warmupMessagesPerThread;
    private final long totalMessages;
    private final int messageQueueCapacity;
    private final Duration sendTimeout;
    private final int maxRetries;
    private final Duration initialBackoff;
    private final Duration maxBackoff;

    public ClientConfig(URI serverUri,
                        int warmupThreads,
                        int warmupMessagesPerThread,
                        long totalMessages,
                        int messageQueueCapacity,
                        Duration sendTimeout,
                        int maxRetries,
                        Duration initialBackoff,
                        Duration maxBackoff) {
        this.serverUri = serverUri;
        this.warmupThreads = warmupThreads;
        this.warmupMessagesPerThread = warmupMessagesPerThread;
        this.totalMessages = totalMessages;
        this.messageQueueCapacity = messageQueueCapacity;
        this.sendTimeout = sendTimeout;
        this.maxRetries = maxRetries;
        this.initialBackoff = initialBackoff;
        this.maxBackoff = maxBackoff;
    }

    public URI serverUri() {
        return serverUri;
    }

    public int warmupThreads() {
        return warmupThreads;
    }

    public int warmupMessagesPerThread() {
        return warmupMessagesPerThread;
    }

    public long totalMessages() {
        return totalMessages;
    }

    public int messageQueueCapacity() {
        return messageQueueCapacity;
    }

    public Duration sendTimeout() {
        return sendTimeout;
    }

    public int maxRetries() {
        return maxRetries;
    }

    public Duration initialBackoff() {
        return initialBackoff;
    }

    public Duration maxBackoff() {
        return maxBackoff;
    }

    public long warmupMessagesTotal() {
        return (long) warmupThreads * warmupMessagesPerThread;
    }

    public long mainPhaseMessagesTotal() {
        return Math.max(0, totalMessages - warmupMessagesTotal());
    }
}
