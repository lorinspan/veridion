//package com.veridion.assignment.service;
//
//import com.veridion.assignment.model.Company;
//import com.veridion.assignment.repository.CompanyRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.Optional;
//
//@Service
//public class CompanyServiceImpl extends CompanyService {
//
//    private final CompanyRepository companyRepository;
//
//    @Autowired
//    public CompanyServiceImpl(CompanyRepository companyRepository) {
//        this.companyRepository = companyRepository;
//    }
//
//    @Override
//    public Company getCompanyById(Long id) {
//        Optional<Company> companyOptional = companyRepository.findById(id);
//        return companyOptional.orElse(null);
//    }
//
//    @Override
//    public List<Company> getAllCompanies() {
//        return companyRepository.findAll();
//    }
//
//    @Override
//    public Company createCompany(Company company) {
//        // You may want to add additional validation logic here before saving
//        return companyRepository.save(company);
//    }
//
//    @Override
//    public Company updateCompany(Long id, Company updatedCompany) {
//        Optional<Company> companyOptional = companyRepository.findById(id);
//        if (companyOptional.isPresent()) {
//            updatedCompany.setId(id); // Ensure the ID matches the path variable
//            return companyRepository.save(updatedCompany);
//        } else {
//            return null; // Indicates the company was not found
//        }
//    }
//
//    @Override
//    public boolean deleteCompany(Long id) {
//        if (companyRepository.existsById(id)) {
//            companyRepository.deleteById(id);
//            return true; // Indicates successful deletion
//        } else {
//            return false; // Indicates the company was not found
//        }
//    }
//}
