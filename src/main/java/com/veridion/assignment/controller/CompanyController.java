package com.veridion.assignment.controller;

import com.veridion.assignment.model.Company;
import com.veridion.assignment.service.CompanyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/company")
public class CompanyController {
    private static final Logger LOGGER = LoggerFactory.getLogger(CompanyController.class);
    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @PostMapping("/match")
    public ResponseEntity<?> matchCompany(@RequestBody Company company) {
        LOGGER.debug("Enter matchCompany with company URL: " + company.getUrl() + ".");
        return companyService.findBestMatch(company);
    }

    @PostMapping("/merge")
    public ResponseEntity<Resource> mergeCSV(@RequestParam("file") MultipartFile multipartFile) {
        LOGGER.debug("Enter mergeCSV, converting file: " + multipartFile + ".");
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
            return ResponseEntity.badRequest().build();
        }
    }
}