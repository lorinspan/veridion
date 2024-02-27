package com.veridion.assignment.executors;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorServiceManager {
    private static final int MAX_THREADS = 8;
    private static ExecutorService executor;

    public static ExecutorService getExecutorService() {
        if (executor == null || executor.isShutdown()) {
            executor = Executors.newFixedThreadPool(MAX_THREADS);
        }
        return executor;
    }

    public static void shutdownExecutorService() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}
