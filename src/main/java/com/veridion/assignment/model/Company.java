package com.veridion.assignment.model;

import jakarta.persistence.*;
import org.springframework.data.annotation.Id;
import org.springframework.util.StringUtils;

@Entity
@Table(name = "companies")
public class Company {

    @jakarta.persistence.Id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "phoneNumber")
    private String phoneNumber;

    @Column(name = "socialMediaLinks")
    private String socialMediaLinks;

    @Column(name = "address")
    private String address;

    // Constructors, getters, and setters

    // Default constructor
    public Company() {
    }

    // Constructor with parameters
    public Company(String name, String phoneNumber, String socialMediaLinks, String address) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.socialMediaLinks = socialMediaLinks;
        this.address = address;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getSocialMediaLinks() {
        return socialMediaLinks;
    }

    public void setSocialMediaLinks(String socialMediaLinks) {
        this.socialMediaLinks = socialMediaLinks;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void print() {
        StringBuilder sb = new StringBuilder();
        sb.append("Company has:");
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            sb.append(" phone number ").append(phoneNumber);
        }
        if (socialMediaLinks != null && !socialMediaLinks.isEmpty()) {
            sb.append(" social media links ").append(socialMediaLinks);
        }
        if (address != null && !address.isEmpty()) {
            sb.append(" address ").append(address);
        }
        System.out.println(sb);
    }

    public boolean isComplete(boolean optionalAddress) {
        return (StringUtils.hasLength(phoneNumber) && StringUtils.hasLength(socialMediaLinks) && (optionalAddress && StringUtils.hasLength(address)));
    }
}
