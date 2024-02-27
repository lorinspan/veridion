package com.veridion.assignment.executors;

import com.veridion.assignment.csv.CSVReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorServiceManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutorServiceManager.class);
    private static final int MIN_THREADS = 6;
    private static final int MAX_THREADS = 16;

    private static ExecutorService executor;

    public static ExecutorService getExecutorService() {
        int maxThreads = calculateMaxThreads(CSVReader.getInstance().getUrls().size());

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
        double ratio = (double) numberOfURLs / 100;

        // Interpolate between MIN_THREADS and MAX_THREADS using the ratio
        int maxThreads = (int) (MIN_THREADS + ratio * (MAX_THREADS - MIN_THREADS));

        // Ensure that maxThreads is within the range of MIN_THREADS and MAX_THREADS
        maxThreads = Math.min(Math.max(maxThreads, MIN_THREADS), MAX_THREADS);

        return maxThreads;
    }
}
