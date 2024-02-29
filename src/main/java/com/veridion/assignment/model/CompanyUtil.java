package com.veridion.assignment.model;

import java.util.Objects;

public class CompanyUtil {

    public static boolean compareCompanies(Company company1, Company company2) {
        // If both objects are null, they are considered equal
        if (company1 == null && company2 == null) {
            return true;
        }
        // If one of the objects is null while the other is not, they are not equal
        if (company1 == null || company2 == null) {
            return false;
        }
        // Compare each parameter of the two Company objects
        boolean commercialNameEquals = Objects.equals(company1.getCommercialName(), company2.getCommercialName());
        boolean legalNameEquals = Objects.equals(company1.getLegalName(), company2.getLegalName());
        boolean allAvailableNamesEquals = Objects.equals(company1.getAllAvailableNames(), company2.getAllAvailableNames());
        boolean phoneNumbersEquals = Objects.equals(company1.getPhoneNumbers(), company2.getPhoneNumbers());
        boolean socialMediaLinksEquals = Objects.equals(company1.getSocialMediaLinks(), company2.getSocialMediaLinks());
        boolean addressEquals = Objects.equals(company1.getAddress(), company2.getAddress());
        boolean urlEquals = Objects.equals(company1.getUrl(), company2.getUrl());

        // Return true if all parameters are equal
        return commercialNameEquals && legalNameEquals && allAvailableNamesEquals &&
                phoneNumbersEquals && socialMediaLinksEquals && addressEquals && urlEquals;
    }
}
