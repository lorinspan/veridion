package com.veridion.assignment.csv;

import com.veridion.assignment.model.Company;
import com.veridion.assignment.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CSVWriterService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CSVWriterService.class);
    public static final String MERGE_CSV_FILE = "C:\\Users\\lorin\\IdeaProjects\\assignment\\src\\main\\resources\\merge-websites\\sample-websites-company-names.csv";
    private static final String CSV_FILE_PATH_OUTPUT = "C:\\Users\\lorin\\IdeaProjects\\assignment\\src\\main\\resources\\output-websites\\companies_";
    private static final String CSV_EXTENSION = ".csv";

    public static List<Company> mergeCompanies(List<Company> crawledCompanies) {
        LOGGER.debug("Merging crawled companies with the companies stored at: " + MERGE_CSV_FILE + ".");
        List<Company> companiesFromCSV = CSVReaderService.getCompaniesFromCSV(MERGE_CSV_FILE);
        Map<String, Company> mergedMap = new HashMap<>();

        for (Company csvCompany : companiesFromCSV) {
            String csvUrl = Utils.removeProtocol(csvCompany.getUrl());
            mergedMap.putIfAbsent(csvUrl, csvCompany);
        }

        for (Company crawledCompany : crawledCompanies) {
            String crawledUrl = Utils.removeProtocol(crawledCompany.getUrl());
            Company mergedCompany = mergedMap.get(crawledUrl);
            crawledCompany.setUrl(Utils.removeProtocol(crawledCompany.getUrl()));

            if (mergedCompany != null) {
                mergedCompany.setUrl(Utils.removeProtocol(mergedCompany.getUrl()));
                mergedCompany.setCommercialName(Utils.mergeStrings(mergedCompany.getCommercialName(), crawledCompany.getCommercialName()));
                mergedCompany.setLegalName(Utils.mergeStrings(mergedCompany.getLegalName(), crawledCompany.getLegalName()));
                mergedCompany.setAllAvailableNames(Utils.mergeStrings(mergedCompany.getAllAvailableNames(), crawledCompany.getAllAvailableNames()));
                mergedCompany.setPhoneNumbers(Utils.mergeStrings(mergedCompany.getPhoneNumbers(), crawledCompany.getPhoneNumbers()));
                mergedCompany.setSocialMediaLinks(Utils.mergeStrings(mergedCompany.getSocialMediaLinks(), crawledCompany.getSocialMediaLinks()));
                mergedCompany.setAddress(Utils.mergeStrings(mergedCompany.getAddress(), crawledCompany.getAddress()));

                mergedMap.put(crawledUrl, mergedCompany);
            } else {
                mergedMap.put(crawledUrl, crawledCompany);
            }
        }

        return new ArrayList<>(mergedMap.values());
    }

    public static File saveNewMergeCSV(List<Company> companies) {
        LOGGER.debug("Saving new database CSV file with a number of " + companies.size() + " companies.");
        String filepath = MERGE_CSV_FILE;
        CSVReaderService csvReaderService = CSVReaderService.getInstance(filepath);

        writeCSV(filepath, mergeCompanies(companies));

        return csvReaderService.getCSVFile(filepath);
    }

    public static File writeCompanies(List<Company> crawledCompanies) {
        String filePath;
        saveNewMergeCSV(mergeCompanies(crawledCompanies));
        String formattedDateTime = Utils.getCurrentFormattedDateTime();
        filePath = CSV_FILE_PATH_OUTPUT + formattedDateTime + CSV_EXTENSION;
        writeCSV(filePath, crawledCompanies);

        return CSVReaderService.getInstance().getCSVFile(filePath);
    }

    private static void writeCSV(String filePath, List<Company> companies) {
        LOGGER.debug("Writing a CSV file to the path: " + filePath + ", containing a number of " + companies.size() + " companies.");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("domain,company_commercial_name,company_legal_name,company_all_available_names,phone_numbers,social_media_links,addresses");
            writer.newLine();

            for (Company company : companies) {
                writer.write(Utils.quoteField(company.getUrl()) + "," +
                        Utils.quoteField(company.getCommercialName()) + "," +
                        Utils.quoteField(company.getLegalName()) + "," +
                        Utils.quoteField(company.getAllAvailableNames()) + "," +
                        Utils.quoteField(company.getPhoneNumbers()) + "," +
                        Utils.quoteField(company.getSocialMediaLinks()) + "," +
                        Utils.quoteField(company.getAddress()));
                writer.newLine();
            }

        } catch (IOException ioException) {
            LOGGER.error("An error has occurred while writing the CSV: " + ioException.getMessage() + ".");
        }
    }
}

