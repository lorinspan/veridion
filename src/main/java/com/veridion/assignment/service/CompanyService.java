package com.veridion.assignment.service;

import com.algolia.search.DefaultSearchClient;
import com.algolia.search.SearchClient;
import com.algolia.search.SearchIndex;
import com.veridion.assignment.csv.CSVReader;
import com.veridion.assignment.model.Company;
import com.veridion.assignment.response.MatchResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class CompanyService {
    private final SearchClient client = DefaultSearchClient.create("K7WMA52L67", "0f0a0719ba468a4e2f8ea68b43a288a5");
    private final SearchIndex<Company> index = client.initIndex("test_index", Company.class);

    public CompanyService() {}

    public ResponseEntity<MatchResponse> findBestMatch(Company inputCompany) {
        List<Company> companies = CSVReader.getCompaniesFromCSV(CSVReader.getInstance().getFilePath());

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
