package com.veridion.assignment.executors;

import com.veridion.assignment.csv.CSVReaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorServiceManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutorServiceManager.class);
    private static final int MIN_THREADS = 6;
    private static final int MAX_THREADS = 16;
    private static final int RATIO_VALUE = 100;

    private static ExecutorService executor;

    public static ExecutorService getExecutorService() {
        int maxThreads = 1;
        try {
            maxThreads = calculateMaxThreads(CSVReaderService.getInstance().getUrls().size());
        } catch (IllegalStateException ignored) {}

        if (executor == null || executor.isShutdown()) {
            executor = Executors.newFixedThreadPool(maxThreads);
        }

        LOGGER.info("Running on " + maxThreads + " threads.");
        return executor;
    }

    public static void shutdownExecutorService() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }

    private static int calculateMaxThreads(int numberOfURLs) {
        double ratio = (double) numberOfURLs / RATIO_VALUE;

        int maxThreads = (int) (MIN_THREADS + ratio * (MAX_THREADS - MIN_THREADS));
        maxThreads = Math.min(Math.max(maxThreads, MIN_THREADS), MAX_THREADS);

        return maxThreads;
    }
}
