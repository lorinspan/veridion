package com.veridion.assignment.csv;

import com.veridion.assignment.model.Company;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CSVReaderService {
    private static CSVReaderService instance;
    private static final Logger LOGGER = LoggerFactory.getLogger(CSVReaderService.class);
    private static String filePath;
    private static List<String> urls;

    private CSVReaderService() {}

    private CSVReaderService(String filePath) {
        CSVReaderService.filePath = filePath;
        urls = readURLsFromCSV();
    }

    private CSVReaderService(File file) {
        filePath = file.getPath();
        urls = readURLsFromCSV(file);
    }

    public static synchronized CSVReaderService getInstance(String filePath) {
        if (instance == null) {
            instance = new CSVReaderService(filePath);
        } else {
            filePath = filePath;
            urls = readURLsFromCSV();
        }

        return instance;
    }

    public static synchronized CSVReaderService getInstance(File file) {
        if (instance == null) {
            instance = new CSVReaderService(file);
        } else {
            filePath = file.getPath();
            urls = readURLsFromCSV(file);
        }
        return instance;
    }

    public static synchronized CSVReaderService getInstance() {
        if (!StringUtils.hasLength(filePath) &&  instance == null) {
            throw new IllegalStateException("CSVReader has not been initialized with a file path.");
        }
        return instance;
    }

    public static List<String> readURLsFromCSV() {
        return readURLsFromCSV(new File(filePath));
    }

    public static List<String> readURLsFromCSV(File file) {
        List<String> urls = new ArrayList<>();
        String line;
        String cvsSplitBy = ",";

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
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

    public File getCSVFile(String filePath) {
        return new File(filePath);
    }

    public List<String> getUrls() {
        return urls;
    }

    public static ArrayList<Company> getCompaniesFromCSV(String csvPath) {
        return getCompaniesFromCSV(new File(csvPath));
    }

    public static ArrayList<Company> getCompaniesFromCSV(File file) {
        ArrayList<Company> companies = new ArrayList<>();
        String line;
        String cvsSplitBy = ",";

        // Map to store the index of each header
        Map<String, Integer> headerIndexMap = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
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
                String[] fields = parseCSVLine(line);

                // Create a Company object
                Company company = new Company();

                // Assign values based on headers
                company.setCommercialName(getValueForHeader(fields, headerIndexMap, "company_commercial_name"));
                company.setLegalName(getValueForHeader(fields, headerIndexMap, "company_legal_name"));
                company.setAllAvailableNames(getValueForHeader(fields, headerIndexMap, "company_all_available_names"));
                company.setPhoneNumbers(getValueForHeader(fields, headerIndexMap, "phone_numbers"));
                company.setSocialMediaLinks(getValueForHeader(fields, headerIndexMap, "social_media_links"));
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


    // Helper method to parse CSV line properly, handling quoted strings
    private static String[] parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(sb.toString());
                sb.setLength(0); // Clear the StringBuilder
            } else {
                sb.append(c);
            }
        }

        // Add the last field
        fields.add(sb.toString());

        return fields.toArray(new String[0]);
    }



    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        CSVReaderService.filePath = filePath;
    }
}
