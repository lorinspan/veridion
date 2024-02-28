package com.veridion.assignment.service;

import com.algolia.search.DefaultSearchClient;
import com.algolia.search.SearchClient;
import com.algolia.search.SearchIndex;
import com.veridion.assignment.algolia.AlgoliaService;
import com.veridion.assignment.csv.CSVReaderService;
import com.veridion.assignment.csv.CSVWriterService;
import com.veridion.assignment.executors.ExecutorServiceManager;
import com.veridion.assignment.model.Company;
import com.veridion.assignment.response.MatchResponse;
import com.veridion.assignment.scraper.ScraperService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class CompanyService {
    private final SearchClient client = DefaultSearchClient.create("K7WMA52L67", "0f0a0719ba468a4e2f8ea68b43a288a5");
    private final SearchIndex<Company> index = client.initIndex("test_index", Company.class);
    private final AlgoliaService algoliaService;
    private final CSVReaderService csvReaderService;
    private final CSVWriterService csvWriterService;
    private final ScraperService scraperService;

    public CompanyService(AlgoliaService algoliaService, CSVReaderService csvReaderService, CSVWriterService csvWriterService, ScraperService scraperService) {
        this.algoliaService = algoliaService;
        this.csvReaderService = csvReaderService;
        this.csvWriterService = csvWriterService;
        this.scraperService = scraperService;
    }

    public ResponseEntity<Resource> crawlCSV(File file) {
        long startTime = System.currentTimeMillis();
        ScraperService.resetCounters();
        CSVReaderService csvReader = CSVReaderService.getInstance(file);
        ExecutorService executor = ExecutorServiceManager.getExecutorService();

        // Create a list of CompletableFuture to hold the scraping tasks
        List<CompletableFuture<Void>> futures = csvReader.getUrls().stream()
                .map(url -> CompletableFuture.runAsync(() -> new ScraperService(url).call(), executor))
                .toList();

        // Wait for all scraping tasks to complete
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        AtomicReference<ResponseEntity<Resource>> responseEntity = new AtomicReference<>();
        // Handle completion of all scraping tasks
        allFutures.thenRun(() -> {
            // Shutdown the executor after all tasks are complete
            executor.shutdown();

            // Perform post-scraping operations
            List<Company> companies = ScraperService.getCompanies();
            File result = CSVWriterService.writeCompanies(companies);
            ScraperService.printDataAnalysis(startTime);

            // Respond with the CSV file
            Resource resource = new FileSystemResource(result);
            responseEntity.set(new ResponseEntity<>(resource, HttpStatus.OK));

            // Clean up temporary files if needed
            // Note: Ensure proper error handling and resource management here

            // You can also perform additional cleanup operations if needed

        }).join(); // Block until all tasks are complete

        // Return a temporary response until all tasks are complete
        return responseEntity.get();
    }

    public ResponseEntity<Resource> mergeCSV(File file) {
        List<Company> companies = CSVReaderService.getCompaniesFromCSV(file);

        Resource resource = new FileSystemResource(CSVWriterService.saveNewMergeCSV(companies));

        return new ResponseEntity<>(resource, HttpStatus.OK);
    }


    public File convertMultipartFileToFile(MultipartFile multipartFile) {
        String fileName = (StringUtils.hasLength(multipartFile.getOriginalFilename())) ? multipartFile.getOriginalFilename() : "companies";
        File file = new File(fileName);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        try {
            fos.write(multipartFile.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            fos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return file;
    }


    public ResponseEntity findBestMatch(Company inputCompany) {
//      List<Company> companies = CSVReaderService.getCompaniesFromCSV(CSVReaderService.getInstance().getFilePath());

        if(inputCompany != null) {
            return new ResponseEntity<>(algoliaService.findCompany(inputCompany), HttpStatus.OK);
        }

        List<Company> companies = algoliaService.findCompanies(inputCompany);

        Company bestMatch = null;
        double bestMatchScore = 0.0;

        for (Company company : companies) {
            double score = calculateMatchScore(inputCompany, company);
            if (score > bestMatchScore) {
                bestMatchScore = score;
                bestMatch = company;
            }
        }


        if(bestMatch != null) {
            return new ResponseEntity<>(new MatchResponse(bestMatch, bestMatchScore), HttpStatus.OK);
        }
        return null;
    }

    private double calculateMatchScore(Company inputCompany, Company targetCompany) {
        double score = 0.0;

        // Compare attributes and assign scores based on matching criteria

        // Commercial Name
        if (StringUtils.hasLength(inputCompany.getCommercialName()) && StringUtils.hasLength(targetCompany.getCommercialName())) {
            if (targetCompany.getCommercialName().equalsIgnoreCase(inputCompany.getCommercialName())) {
                score += 1.0; // Exact match
            } else if (targetCompany.getCommercialName().toLowerCase().contains(inputCompany.getCommercialName().toLowerCase())) {
                score += 0.5; // Contains substring
            }
        }

        // Legal Name
        if (StringUtils.hasLength(inputCompany.getLegalName()) && StringUtils.hasLength(targetCompany.getLegalName())) {
            if (targetCompany.getLegalName().equalsIgnoreCase(inputCompany.getLegalName())) {
                score += 1.0; // Exact match
            } else if (targetCompany.getLegalName().toLowerCase().contains(inputCompany.getLegalName().toLowerCase())) {
                score += 0.5; // Contains substring
            }
        }

        // Phone Numbers
        if (StringUtils.hasLength(inputCompany.getPhoneNumbers()) && StringUtils.hasLength(targetCompany.getPhoneNumbers())) {
            if (targetCompany.getPhoneNumbers().equals(inputCompany.getPhoneNumbers())) {
                score += 1.0; // Exact match
            } else if (targetCompany.getPhoneNumbers().contains(inputCompany.getPhoneNumbers())) {
                score += 0.5; // Contains substring
            }
        }

        // Social Media Links
        if (StringUtils.hasLength(inputCompany.getSocialMediaLinks()) && StringUtils.hasLength(targetCompany.getSocialMediaLinks())) {
            if (targetCompany.getSocialMediaLinks().equals(inputCompany.getSocialMediaLinks())) {
                score += 1.0; // Exact match
            } else if (targetCompany.getSocialMediaLinks().contains(inputCompany.getSocialMediaLinks())) {
                score += 0.5; // Contains substring
            }
        }

        // Address
        if (StringUtils.hasLength(inputCompany.getAddress()) && StringUtils.hasLength(targetCompany.getAddress())) {
            if (targetCompany.getAddress().equals(inputCompany.getAddress())) {
                score += 1.0; // Exact match
            } else if (targetCompany.getAddress().contains(inputCompany.getAddress())) {
                score += 0.5; // Contains substring
            }
        }

        return score;
    }

}
