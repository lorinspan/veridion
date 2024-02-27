package com.veridion.assignment.repository;

import com.veridion.assignment.model.Company;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    // No need to define any methods here, JpaRepository provides basic CRUD operations
}