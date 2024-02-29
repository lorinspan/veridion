package com.veridion.assignment.service;

import com.algolia.search.DefaultSearchClient;
import com.algolia.search.SearchClient;
import com.algolia.search.SearchIndex;
import com.veridion.assignment.algolia.AlgoliaService;
import com.veridion.assignment.csv.CSVReaderService;
import com.veridion.assignment.csv.CSVWriterService;
import com.veridion.assignment.executors.ExecutorServiceManager;
import com.veridion.assignment.model.Company;
import com.veridion.assignment.model.CompanyUtil;
import com.veridion.assignment.model.MatchingScore;
import com.veridion.assignment.scraper.ScraperService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class CompanyService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CompanyService.class);
    private static final double EXACT_MATCH_WEIGHT = 1.0;
    private static final double STARTS_WITH_WEIGHT = 0.85;
    private static final double CONTAINS_SUBSTRING_WEIGHT = 0.75;
    private static final double LEVENSHTEIN_WEIGHT = 0.5;
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

    public ResponseEntity<Resource> crawlCompany(String url) {
        long startTime = System.currentTimeMillis();
        ScraperService.resetCounters();
        ExecutorService executor = ExecutorServiceManager.getExecutorService();
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> new ScraperService(url).call(), executor);

        return getResourceResponseEntity(startTime, executor, future);
    }

    private ResponseEntity<Resource> getResourceResponseEntity(long startTime, ExecutorService executor, CompletableFuture<Void> future) {
        AtomicReference<ResponseEntity<Resource>> responseEntity = new AtomicReference<>();
        future.thenRun(() -> {
            executor.shutdown();

            List<Company> companies = ScraperService.getCompanies();
            File result = CSVWriterService.writeCompanies(companies);
            ScraperService.printDataAnalysis(startTime, companies);

            Resource resource = new FileSystemResource(result);
            responseEntity.set(new ResponseEntity<>(resource, HttpStatus.OK));
        }).join();

        return responseEntity.get();
    }

    public ResponseEntity<Resource> crawlCSV(File file) {
        long startTime = System.currentTimeMillis();
        ScraperService.resetCounters();
        CSVReaderService csvReader = CSVReaderService.getInstance(file);
        ExecutorService executor = ExecutorServiceManager.getExecutorService();

        List<CompletableFuture<Void>> futures = csvReader.getUrls().stream()
                .map(url -> CompletableFuture.runAsync(() -> new ScraperService(url).call(), executor))
                .toList();

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        return getResourceResponseEntity(startTime, executor, allFutures);
    }

    public ResponseEntity<Resource> mergeCSV(File file) {
        List<Company> companies = CSVReaderService.getCompaniesFromCSV(file);

        Resource resource = new FileSystemResource(CSVWriterService.saveNewMergeCSV(companies));

        return new ResponseEntity<>(resource, HttpStatus.OK);
    }

    public File convertMultipartFileToFile(MultipartFile multipartFile) {
        LOGGER.debug("Converting multipart file to file in convertMultipartFileToFile().");
        String fileName = (StringUtils.hasLength(multipartFile.getOriginalFilename())) ? multipartFile.getOriginalFilename() : "companies";
        File file = new File(fileName);
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(file);
            fos.write(multipartFile.getBytes());
            fos.close();
        } catch (Exception exception) {
            LOGGER.error("An error has occurred while converting multipartfile to file: " + exception.getMessage() + ".");
        }

        return file;
    }

    public ResponseEntity<Map<String, Object>> findBestMatch(Company inputCompany) {
        List<Company> companies = CSVReaderService.getCompaniesFromCSV(CSVWriterService.MERGE_CSV_FILE);

        Company algoliaCompany = algoliaService.findCompany(inputCompany);

        List<MatchingScore> matchingScores = new ArrayList<>();

        for (Company company : companies) {
            double score = calculateMatchScore(inputCompany, company);
            matchingScores.add(new MatchingScore(company, score));
        }

        matchingScores.sort(Comparator.comparingDouble(MatchingScore::getScore).reversed());

        List<Map<String, Object>> top10CompaniesWithScores = matchingScores.stream()
                .limit(10)
                .map(matchingScore -> {
                    Map<String, Object> companyMap = new HashMap<>();
                    companyMap.put("company", matchingScore.getCompany());
                    companyMap.put("score", matchingScore.getScore());
                    return companyMap;
                })
                .collect(Collectors.toList());

        Company bestMatch = !top10CompaniesWithScores.isEmpty() ? (Company) top10CompaniesWithScores.get(0).get("company") : null;
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("algolia_company", algoliaCompany);
        responseMap.put("best_match_company", bestMatch);
        responseMap.put("top_10_companies", !top10CompaniesWithScores.isEmpty() ? top10CompaniesWithScores : null);
        responseMap.put("is_a_match", CompanyUtil.compareCompanies(bestMatch, algoliaCompany) ? "The matching algorithm has returned the same company as Algoria did!" : "The matching algorithm did not return the same company as Algoria did.");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(responseMap);
    }

    private double calculateMatchScore(Company inputCompany, Company targetCompany) {
        double score = 0.0;

        score += compareAttributes(inputCompany.getUrl(), targetCompany.getUrl(), 2.0);
        score += compareAttributes(inputCompany.getCommercialName(), targetCompany.getCommercialName(), 1.5);
        score += compareAttributes(inputCompany.getLegalName(), targetCompany.getLegalName(), 1.5);
        score += compareAttributes(inputCompany.getPhoneNumbers(), targetCompany.getPhoneNumbers(), 0.75);
        score += compareAttributes(inputCompany.getSocialMediaLinks(), targetCompany.getSocialMediaLinks(), 1.0);
        score += compareAttributes(inputCompany.getAddress(), targetCompany.getAddress(), 1.0);

        return score;
    }

    private double compareAttributes(String inputAttribute, String targetAttribute, double weight) {
        if (!StringUtils.hasLength(inputAttribute) || !StringUtils.hasLength(targetAttribute)) {
            return 0.0;
        }

        double score = 0.0;
        if (targetAttribute.equalsIgnoreCase(inputAttribute)) {
            score += EXACT_MATCH_WEIGHT * weight;
        } else if (targetAttribute.toLowerCase().startsWith(inputAttribute.toLowerCase())) {
            score += STARTS_WITH_WEIGHT * weight;
        } else if (targetAttribute.toLowerCase().contains(inputAttribute.toLowerCase())) {
            score += CONTAINS_SUBSTRING_WEIGHT * weight;
        } else {
            int distance = calculateLevenshteinDistance(inputAttribute.toLowerCase(), targetAttribute.toLowerCase());
            double normalizedDistance = 1.0 - (double) distance / Math.max(inputAttribute.length(), targetAttribute.length());
            score += normalizedDistance * LEVENSHTEIN_WEIGHT * weight;
        }
        return score;
    }

    private int calculateLevenshteinDistance(String word1, String word2) {
        int[][] dp = new int[word1.length() + 1][word2.length() + 1];

        for (int i = 0; i <= word1.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= word2.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= word1.length(); i++) {
            for (int j = 1; j <= word2.length(); j++) {
                int cost = (word1.charAt(i - 1) == word2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
            }
        }

        return dp[word1.length()][word2.length()];
    }
}
