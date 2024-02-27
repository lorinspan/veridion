package com.veridion.assignment.service;

import com.veridion.assignment.model.Company;
import com.veridion.assignment.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;

    @Autowired
    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    // Method to retrieve a company by ID
    public Company getCompanyById(Long id) {
        Optional<Company> companyOptional = companyRepository.findById(id);
        return companyOptional.orElse(null);
    }

    // Method to retrieve all companies
    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    // Method to create a new company
    public Company createCompany(Company company) {
        return companyRepository.save(company);
    }

    // Method to update an existing company
    public Company updateCompany(Long id, Company updatedCompany) {
        Optional<Company> companyOptional = companyRepository.findById(id);
        if (companyOptional.isPresent()) {
            updatedCompany.setId(id); // Ensure the ID matches the path variable
            return companyRepository.save(updatedCompany);
        } else {
            return null; // Indicates the company was not found
        }
    }

    // Method to delete an existing company
    public boolean deleteCompany(Long id) {
        if (companyRepository.existsById(id)) {
            companyRepository.deleteById(id);
            return true; // Indicates successful deletion
        } else {
            return false; // Indicates the company was not found
        }
    }
}
