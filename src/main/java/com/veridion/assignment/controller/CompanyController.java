package com.veridion.assignment.api;

import com.veridion.assignment.model.Company;
import com.veridion.assignment.response.MatchResponse;
import com.veridion.assignment.service.CompanyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/company")
public class CompanyController {
    private CompanyService companyService;

    @PostMapping("/match")
    public ResponseEntity<MatchResponse> matchCompany(@RequestBody Company company) {
        return companyService.findBestMatch(company);
    }

    // Inner class for custom response object

}
