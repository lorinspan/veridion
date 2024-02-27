package com.veridion.assignment.scraper;

import com.veridion.assignment.csv.CSVReader;
import com.veridion.assignment.csv.CSVWriter;
import com.veridion.assignment.executors.ExecutorServiceManager;

import java.util.concurrent.ExecutorService;

public class WebScraper {
    private static final String CSV_FILE_SMALL_SIZE = "src/main/resources/input-websites/sample-websites-small-size.csv";
    private static final String CSV_FILE = "src/main/resources/input-websites/sample-websites.csv";

    public static void main(String[] args) {
        CSVReader csvReader = CSVReader.getInstance(CSV_FILE_SMALL_SIZE);
        ExecutorService executor = ExecutorServiceManager.getExecutorService();
        try {
            for (String url : csvReader.getUrls()) {
                executor.submit(new ScraperTask(url));
            }
        } finally {
            ExecutorServiceManager.shutdownExecutorService();
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            CSVWriter.writeCompanies(ScraperTask.getCompanies());
            ScraperTask.printDataAnalysis();
        }));
    }

}

/*
    TODO: https://soleadify.notion.site/Software-Engineer-Assignment-049905cff8e24ce3bfbc29e8312127ec

 */