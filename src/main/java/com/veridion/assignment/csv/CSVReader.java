package com.veridion.assignment.csv;

import com.veridion.assignment.model.Company;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public static ArrayList<Company> getCompaniesFromCSV(String csvPath) {
        ArrayList<Company> companies = new ArrayList<>();
        String line;
        String cvsSplitBy = ",";

        // Map to store the index of each header
        Map<String, Integer> headerIndexMap = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(csvPath))) {
            // Read the header line
            String headerLine = br.readLine();
            if (headerLine != null) {
                // Split the header line to get individual headers
                String[] headers = headerLine.split(cvsSplitBy);

                // Populate the header index map
                for (int i = 0; i < headers.length; i++) {
                    headerIndexMap.put(headers[i], i);
                }
            }

            // Read data lines
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(cvsSplitBy);

                // Create a Company object
                Company company = new Company();

                // Assign values based on headers
                company.setCommercialName(getValueForHeader(fields, headerIndexMap, "company_commercial_name"));
                company.setLegalName(getValueForHeader(fields, headerIndexMap, "company_legal_name"));
                company.setAllAvailableNames(getValueForHeader(fields, headerIndexMap, "company_all_available_names"));
                company.setPhoneNumbers(getValueForHeader(fields, headerIndexMap, "phoneNumbers"));
                company.setSocialMediaLinks(getValueForHeader(fields, headerIndexMap, "socialMediaLinks"));
                company.setAddress(getValueForHeader(fields, headerIndexMap, "addresses"));
                company.setUrl(getValueForHeader(fields, headerIndexMap, "domain"));

                // Add the company to the list
                companies.add(company);
            }
        } catch (IOException ioException) {
            LOGGER.error("An error has occurred while reading the CSV file: " + ioException.getMessage() + ".");
        }
        return companies;
    }

    // Helper method to get value based on header
    private static String getValueForHeader(String[] fields, Map<String, Integer> headerIndexMap, String header) {
        Integer index = headerIndexMap.get(header);
        if (index != null && index < fields.length) {
            return fields[index];
        }
        return null;
    }
}
