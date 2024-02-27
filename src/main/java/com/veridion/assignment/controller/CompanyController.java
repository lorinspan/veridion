package com.veridion.assignment.controller;

import com.veridion.assignment.model.Company;
import com.veridion.assignment.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    private final CompanyService companyService;

    @Autowired
    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    // Endpoint to retrieve a company by ID
    @GetMapping("/{companyId}")
    public ResponseEntity<Company> getCompanyById(@PathVariable Long companyId) {
        Company company = companyService.getCompanyById(companyId);
        if (company != null) {
            return new ResponseEntity<>(company, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Endpoint to create a new company
    @PostMapping
    public ResponseEntity<Company> createCompany(@RequestBody Company company) {
        Company createdCompany = companyService.createCompany(company);
        return new ResponseEntity<>(createdCompany, HttpStatus.CREATED);
    }

    // Endpoint to update an existing company
    @PutMapping("/{companyId}")
    public ResponseEntity<Company> updateCompany(@PathVariable Long companyId, @RequestBody Company company) {
        Company updatedCompany = companyService.updateCompany(companyId, company);
        if (updatedCompany != null) {
            return new ResponseEntity<>(updatedCompany, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Endpoint to delete an existing company
    @DeleteMapping("/{companyId}")
    public ResponseEntity<Void> deleteCompany(@PathVariable Long companyId) {
        boolean deleted = companyService.deleteCompany(companyId);
        if (deleted) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}