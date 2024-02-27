package com.veridion.assignment.model;

import jakarta.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.util.StringUtils;

@Entity
@Table(name = "companies")
public class Company {
    private static final Logger LOGGER = LoggerFactory.getLogger(Company.class);

    @jakarta.persistence.Id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "phoneNumber")
    private String phoneNumbers;

    @Column(name = "socialMediaLinks")
    private String socialMediaLinks;

    @Column(name = "address")
    private String address;

    @Column(name = "url") // New column for URL
    private String url;

    // Constructors, getters, and setters

    // Default constructor
    public Company() {
    }

    // Constructor with parameters
    public Company(String name, String phoneNumbers, String socialMediaLinks, String address, String url) {
        this.name = name;
        this.phoneNumbers = phoneNumbers;
        this.socialMediaLinks = socialMediaLinks;
        this.address = address;
        this.url = url;
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

    public String getPhoneNumbers() {
        return phoneNumbers;
    }

    public void setPhoneNumbers(String phoneNumber) {
        this.phoneNumbers = phoneNumber;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isComplete(boolean optionalAddress) {
        boolean requiredFieldsPresent = StringUtils.hasLength(phoneNumbers) && StringUtils.hasLength(socialMediaLinks);
        if (optionalAddress) {
            return requiredFieldsPresent && StringUtils.hasLength(address);
        }
        return requiredFieldsPresent;
    }

    public void print() {
        String phoneNumbers = this.getPhoneNumbers();
        String socialMediaLinks = this.getSocialMediaLinks();
        String address = this.getAddress();

        if (StringUtils.hasLength(phoneNumbers) || StringUtils.hasLength(socialMediaLinks) || StringUtils.hasLength(address)) {
            LOGGER.info("Company extracted from URL: " + this.getUrl() +
                    (StringUtils.hasLength(phoneNumbers) ? " has phone number(s): " + phoneNumbers : "") +
                    (StringUtils.hasLength(socialMediaLinks) ? ", social media link(s): " + socialMediaLinks : "") +
                    (StringUtils.hasLength(address) ? ", address: " + address : "") +
                    ".");
        } else {
            LOGGER.info("Company extracted from URL: " + this.getUrl() + " - no datapoints could be extracted.");
        }
    }
}
