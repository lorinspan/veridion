package com.veridion.assignment.scraper;

import com.veridion.assignment.csv.CSVReader;
import com.veridion.assignment.executors.ExecutorServiceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class WebScraper {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebScraper.class);
    private static final String CSV_FILE_SMALL_SIZE = "src/main/resources/websites/sample-websites-small-size.csv";
    private static final String CSV_FILE = "src/main/resources/websites/sample-websites.csv";

    public static void main(String[] args) {
        final long startTime = System.currentTimeMillis();

        CSVReader csvReader = new CSVReader(CSV_FILE_SMALL_SIZE);
        ExecutorService executor = ExecutorServiceManager.getExecutorService();

        try {
            List<String> urls = csvReader.readURLsFromCSV();
            for (String url : urls) {
                executor.submit(new ScraperTask(url));
            }
        } finally {
            ExecutorServiceManager.shutdownExecutorService();
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            final long endTime = System.currentTimeMillis();
            LOGGER.info("Calculated in: " + (endTime - startTime) / 1000 + " seconds." );
        }));
    }
}

/*
    TODO: https://soleadify.notion.site/Software-Engineer-Assignment-049905cff8e24ce3bfbc29e8312127ec

    ### The data analysis part

    Run a quick analysis on the data you were able to extract:

    - how many websites were you able to crawl? (coverage)
    - how many datapoints were you able to extract from the websites you crawled? (fill rates)

    ### The scaling part

    Find a scalable way to crawl the entire list in no more than **10 minutes.**

 */