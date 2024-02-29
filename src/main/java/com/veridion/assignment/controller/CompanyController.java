package com.veridion.assignment.controller;

import com.veridion.assignment.model.Company;
import com.veridion.assignment.service.CompanyService;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/company")
public class CompanyController {
    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @PostMapping("/match")
    public ResponseEntity<?> matchCompany(@RequestBody Company company) {
        return companyService.findBestMatch(company);
    }

    @PostMapping("/merge")
    public ResponseEntity<Resource> mergeCSV(@RequestParam("file") MultipartFile multipartFile) {
        File file = companyService.convertMultipartFileToFile(multipartFile);

        return companyService.mergeCSV(file);
    }

    @PostMapping("/crawl")
    public ResponseEntity<Resource> crawlCompany(@RequestParam(value = "file", required = false) MultipartFile multipartFile,
                                                 @RequestParam(value = "url", required = false) String url) {
        if (multipartFile != null) {
            File file = companyService.convertMultipartFileToFile(multipartFile);
            return companyService.crawlCSV(file);
        } else if (StringUtils.hasLength(url)) {
            return companyService.crawlCompany(url);
        } else {
            return ResponseEntity.badRequest().build(); // Handle invalid request
        }
    }
}