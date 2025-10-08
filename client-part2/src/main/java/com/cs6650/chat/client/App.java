package com.cs6650.chat.client;

public class App {

    private static final String DEFAULT_SERVER_URI = "ws://localhost:8080/chat/1";

    public static void main(String[] args) {
        App app = new App();
        app.run(args.length > 0 ? args[0] : DEFAULT_SERVER_URI);
    }

    private void run(String serverUri) {
        System.out.printf("Client Part 2 bootstrap. Target server: %s%n", serverUri);
        // TODO: wire metrics collector, HDR histogram, CSV writers, and analysis pipeline.
    }
}
