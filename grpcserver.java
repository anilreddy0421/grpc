package com.grpc.employee;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class EmployeeServer {

    private static final int PORT = 50051;
    private Server server;

    // ─── Start the server ────────────────────────────────────────────────
    public void start() throws IOException {
        server = ServerBuilder
                .forPort(PORT)
                .addService(new EmployeeServiceImpl())
                .build()
                .start();

        System.out.println("======================================");
        System.out.println(" Employee gRPC Server started");
        System.out.println(" Listening on port: " + PORT);
        System.out.println("======================================");

        // Hook to gracefully shut down when JVM exits (Ctrl+C)
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down server...");
            try {
                EmployeeServer.this.stop();
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }
            System.out.println("Server shut down.");
        }));
    }

    // ─── Stop the server ─────────────────────────────────────────────────
    public void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    // ─── Keep server alive until terminated ──────────────────────────────
    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    // ─── Main entry point ────────────────────────────────────────────────
    public static void main(String[] args) throws IOException, InterruptedException {
        EmployeeServer employeeServer = new EmployeeServer();
        employeeServer.start();
        employeeServer.blockUntilShutdown();
    }
}