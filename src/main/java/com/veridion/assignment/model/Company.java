package com.veridion.assignment.model;

import org.springframework.util.StringUtils;

import java.io.Serializable;

public class Company implements Serializable {
    private Long objectID;
    private String commercialName;

    private String legalName;

    private String allAvailableNames;

    private String phoneNumbers;

    private String socialMediaLinks;

    private String address;

    private String url;

    public Company() {}

    public Company(String commercialName, String legalName, String allAvailableNames, String phoneNumbers, String socialMediaLinks, String address, String url) {
        this.commercialName = commercialName;
        this.legalName = legalName;
        this.allAvailableNames = allAvailableNames;
        this.phoneNumbers = phoneNumbers;
        this.socialMediaLinks = socialMediaLinks;
        this.address = address;
        this.url = url;
    }

    public String getCommercialName() {
        return commercialName;
    }

    public void setCommercialName(String name) {
        this.commercialName = name;
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

    public String getLegalName() {
        return legalName;
    }

    public void setLegalName(String legalName) {
        this.legalName = legalName;
    }

    public String getAllAvailableNames() {
        return allAvailableNames;
    }

    public void setAllAvailableNames(String allAvailableNames) {
        this.allAvailableNames = allAvailableNames;
    }
}
