package com.veridion.assignment.csv;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CSVReader {
    private String filePath;

    public CSVReader(String filePath) {
        this.filePath = filePath;
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
        } catch (IOException e) {
            e.printStackTrace();
        }
        return urls;
    }
}
