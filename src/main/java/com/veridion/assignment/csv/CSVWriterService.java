package com.veridion.assignment.csv;

import com.veridion.assignment.algolia.AlgoliaService;
import com.veridion.assignment.model.Company;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class CSVWriterService {
    private static final AlgoliaService algoliaService = new AlgoliaService();
    private static final Logger LOGGER = LoggerFactory.getLogger(CSVWriterService.class);
    public static final String MERGE_CSV_FILE = "C:\\Users\\lorin\\IdeaProjects\\assignment\\src\\main\\resources\\merge-websites\\sample-websites-company-names.csv";
    private static final String CSV_FILE_PATH_OUTPUT = "C:\\Users\\lorin\\IdeaProjects\\assignment\\src\\main\\resources\\output-websites\\companies_";
    private static final String CSV_EXTENSION = ".csv";

    public static List<Company> mergeCompanies(List<Company> crawledCompanies) {
        LOGGER.debug("Getting input file of company names: " + MERGE_CSV_FILE + ".");
        List<Company> companiesFromCSV = CSVReaderService.getCompaniesFromCSV(MERGE_CSV_FILE);
        Map<String, Company> mergedMap = new HashMap<>();

        // Merge CSV companies into a map
        for (Company csvCompany : companiesFromCSV) {
            String csvUrl = removeProtocol(csvCompany.getUrl());
            mergedMap.putIfAbsent(csvUrl, csvCompany);
        }

        // Merge crawled companies into the map
        for (Company crawledCompany : crawledCompanies) {
            String crawledUrl = removeProtocol(crawledCompany.getUrl());
            Company mergedCompany = mergedMap.get(crawledUrl);
            crawledCompany.setUrl(removeProtocol(crawledCompany.getUrl()));

            if (mergedCompany != null) {
                // Merge parameters of matching companies
                mergedCompany.setUrl(removeProtocol(mergedCompany.getUrl()));
                mergedCompany.setCommercialName(mergeStrings(mergedCompany.getCommercialName(), crawledCompany.getCommercialName()));
                mergedCompany.setLegalName(mergeStrings(mergedCompany.getLegalName(), crawledCompany.getLegalName()));
                mergedCompany.setAllAvailableNames(mergeStrings(mergedCompany.getAllAvailableNames(), crawledCompany.getAllAvailableNames()));
                mergedCompany.setPhoneNumbers(mergeStrings(mergedCompany.getPhoneNumbers(), crawledCompany.getPhoneNumbers()));
                mergedCompany.setSocialMediaLinks(mergeStrings(mergedCompany.getSocialMediaLinks(), crawledCompany.getSocialMediaLinks()));
                mergedCompany.setAddress(mergeStrings(mergedCompany.getAddress(), crawledCompany.getAddress()));
                // Add more parameters to merge as needed

                // Update the merged company in the map
                mergedMap.put(crawledUrl, mergedCompany);
            } else {
                // Add the crawled company if it's not in the map
                mergedMap.put(crawledUrl, crawledCompany);
            }
        }

        // Convert the map to a list of merged companies
        List<Company> mergedCompanies = new ArrayList<>(mergedMap.values());

        return mergedCompanies;
    }

    // Helper method to merge strings if not null and not equal
    private static String mergeStrings(String existingValue, String newValue) {
        if (StringUtils.hasLength(newValue) && !newValue.equals(existingValue)) {
            return StringUtils.hasLength(existingValue) ? removeLeadingOrLastingComma(existingValue + ", " + newValue) : removeLeadingOrLastingComma(newValue);
        }
        return removeLeadingOrLastingComma(existingValue);
    }

    public static File saveNewMergeCSV(List<Company> companies) {
        String filepath = MERGE_CSV_FILE;
        CSVReaderService csvReaderService = CSVReaderService.getInstance(filepath);

        writeCSV(filepath, mergeCompanies(companies));

        return csvReaderService.getCSVFile(filepath);
    }

    public static File writeCompanies(List<Company> crawledCompanies) {
        String filePath;
        saveNewMergeCSV(mergeCompanies(crawledCompanies));
        String formattedDateTime = getCurrentFormattedDateTime();
        filePath = CSV_FILE_PATH_OUTPUT + formattedDateTime + CSV_EXTENSION;
        writeCSV(filePath, crawledCompanies);

        return CSVReaderService.getInstance().getCSVFile(filePath); // Return the file after writing
    }

    private static void writeCSV(String filePath, List<Company> companies) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
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

        } catch (IOException ioException) {
            LOGGER.error("An error has occurred while writing the CSV: " + ioException.getMessage() + ".");
        }
    }

    // Helper method to enclose a field in double quotes
    private static String quoteField(String field) {
        // If the field is null or empty, return an empty string
        if (field == null || field.isEmpty()) {
            return "";
        }
        // If the field contains double quotes or commas, enclose it in double quotes
        if (field.contains("\"") || field.contains(",")) {
            // If the field contains double quotes, escape them by doubling them
            if (field.contains("\"")) {
                field = field.replace("\"", "\"\"");
            }
            // Enclose the field in double quotes
            return "\"" + field + "\"";
        }
        // If the field doesn't contain double quotes or commas, return it as is
        return field;
    }

    private static String removeAllQuotes(String url) {
        // Remove "https://" and "http://" from anywhere in the string
        return url.replaceAll("\"", "");
    }


    private static String removeProtocol(String url) {
        // Remove "https://" and "http://" from anywhere in the string
        url = url.replaceAll("https?://", "");

        // Remove "www." from the beginning of the URL
        url = url.replaceAll("^www\\.", "");

        // Split the URL by "/", and take the first part which represents the domain
        String[] parts = url.split("/");
        return parts[0];
    }

    private static String getCurrentFormattedDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HHmm_ddMMyyyy");
        return sdf.format(new Date());
    }

    private static String removeLeadingOrLastingComma(String var) {
        if (var == null || var.isEmpty()) {
            return var;
        }

        // Remove leading comma and whitespace
        while (var.startsWith(",") || var.startsWith(" ")) {
            var = var.substring(1).trim();
        }

        // Remove lasting comma and whitespace
        while (var.endsWith(",") || var.endsWith(" ")) {
            var = var.substring(0, var.length() - 1).trim();
        }

        return var;
    }
}

