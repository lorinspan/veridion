package com.veridion.assignment.csv;

import com.veridion.assignment.model.Company;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CSVWriter {
    private static final Logger LOGGER = LoggerFactory.getLogger(CSVWriter.class);
    private static final String MERGE_CSV_FILE = "C:\\Users\\lorin\\IdeaProjects\\assignment\\src\\main\\resources\\merge-websites\\sample-websites-company-names.csv";
    private static final String CSV_FILE_PATH_OUTPUT = "C:\\Users\\lorin\\IdeaProjects\\assignment\\src\\main\\resources\\output-websites\\companies.csv";

    private static List<Company> mergeCompanies(List<Company> crawledCompanies) {
        LOGGER.debug("Getting input file of company names: " + MERGE_CSV_FILE + ".");
        List<Company> companiesFromCSV = CSVReader.getCompaniesFromCSV(MERGE_CSV_FILE);

        for (Company csvCompany : companiesFromCSV) {
            for (Company crawledCompany : crawledCompanies) {
                // Check if the URL of the crawled company matches the URL from CSV
                if (csvCompany.getUrl().equalsIgnoreCase(removeProtocol(crawledCompany.getUrl()))) {
                    // Update the phone numbers, social media links, and addresses
                    csvCompany.setPhoneNumbers(crawledCompany.getPhoneNumbers());
                    csvCompany.setSocialMediaLinks(crawledCompany.getSocialMediaLinks());
                    csvCompany.setAddress(crawledCompany.getAddress());
                    // No need to continue searching, break the loop
                    break;
                }
            }
        }

        return companiesFromCSV;
    }

    public static void writeCompanies(List<Company> crawledCompanies) {
        List<Company> companies = mergeCompanies(crawledCompanies);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CSV_FILE_PATH_OUTPUT))) {
            LOGGER.debug("Writing new CSV file.");
            // Writing headers
            writer.write("domain,company_commercial_name,company_legal_name,company_all_available_names,phone_numbers,social_media_links,addresses");
            writer.newLine();

            // Writing company data
            for (Company company : companies) {
                // Write each field enclosed in double quotes to preserve special characters and commas
                writer.write(quoteField(company.getUrl()) + "," +
                        quoteField(company.getCommercialName()) + "," +
                        quoteField(company.getLegalName()) + "," +
                        quoteField(company.getAllAvailableNames()) + "," +
                        quoteField(company.getPhoneNumbers()) + "," +
                        quoteField(company.getSocialMediaLinks()) + "," +
                        quoteField(company.getAddress()));
                writer.newLine();
            }

            LOGGER.info("CSV file written successfully: " + CSV_FILE_PATH_OUTPUT + ".");
        } catch (IOException ioException) {
            LOGGER.info("Error writing CSV file: " + ioException.getMessage() + ".");
        }
    }

    // Helper method to enclose a field in double quotes
    private static String quoteField(String field) {
        // If the field is null or empty, return an empty string
        if (field == null || field.isEmpty()) {
            return "";
        }
        // If the field contains double quotes, escape them by doubling them
        if (field.contains("\"")) {
            field = field.replace("\"", "\"\"");
        }
        // Enclose the field in double quotes
        return "\"" + field + "\"";
    }

    private static String removeProtocol(String url) {
        if (url.startsWith("https://")) {
            return url.substring(8); // Remove "https://"
        } else if (url.startsWith("http://")) {
            return url.substring(7); // Remove "http://"
        }
        return url; // URL doesn't start with a protocol, return as is
    }
}

