package com.veridion.assignment.csv;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CSVReader {
    private static CSVReader instance;
    private static final Logger LOGGER = LoggerFactory.getLogger(CSVReader.class);
    private final String filePath;
    private final List<String> urls;

    private CSVReader(String filePath) {
        this.filePath = filePath;
        this.urls = readURLsFromCSV();
    }

    public static synchronized CSVReader getInstance(String filePath) {
        if (instance == null) {
            instance = new CSVReader(filePath);
        }
        return instance;
    }

    public static synchronized CSVReader getInstance() {
        if (instance == null) {
            throw new IllegalStateException("CSVReader has not been initialized with a file path.");
        }
        return instance;
    }

    public List<String> readURLsFromCSV() {
        List<String> urls = new ArrayList<>();
        String line;
        String cvsSplitBy = ",";

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            while ((line = br.readLine()) != null) {
                // use comma as separator
                String[] urlsArray = line.split(cvsSplitBy);
                for (String url : urlsArray) {
                    // Skip if the entry is "domain"
                    if (!url.trim().equalsIgnoreCase("domain")) {
                        // Check if the URL does not start with "https://" or "http://"
                        if (!url.trim().startsWith("https://") && !url.trim().startsWith("http://")) {
                            url = "https://" + url.trim();
                        }
                        urls.add(url.trim());
                    }
                }
            }
        } catch (IOException ioException) {
            LOGGER.error("An error has occurred while reading the CSV file: " + ioException.getMessage() + ".");
        }
        return urls;
    }

    public List<String> getUrls() {
        return urls;
    }
}
