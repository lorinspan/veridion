package com.veridion.assignment.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.Objects;

public class CompanyUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(CompanyUtil.class);

    public static boolean compareCompanies(Company company1, Company company2) {
        if (company1 == null && company2 == null) {
            return true;
        }
        if (company1 == null || company2 == null) {
            return false;
        }
        boolean commercialNameEquals = Objects.equals(company1.getCommercialName(), company2.getCommercialName());
        boolean legalNameEquals = Objects.equals(company1.getLegalName(), company2.getLegalName());
        boolean allAvailableNamesEquals = Objects.equals(company1.getAllAvailableNames(), company2.getAllAvailableNames());
        boolean phoneNumbersEquals = Objects.equals(company1.getPhoneNumbers(), company2.getPhoneNumbers());
        boolean socialMediaLinksEquals = Objects.equals(company1.getSocialMediaLinks(), company2.getSocialMediaLinks());
        boolean addressEquals = Objects.equals(company1.getAddress(), company2.getAddress());
        boolean urlEquals = Objects.equals(company1.getUrl(), company2.getUrl());

        return commercialNameEquals && legalNameEquals && allAvailableNamesEquals &&
                phoneNumbersEquals && socialMediaLinksEquals && addressEquals && urlEquals;
    }

    public static String concatenateCompanyFields(Company company) {
        StringBuilder query = new StringBuilder();

        Field[] fields = Company.class.getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(company);
                if (value != null) {
                    query.append(value);
                    query.append(" ");
                }
            } catch (IllegalAccessException illegalAccessException) {
                LOGGER.error("An error has occurred while processing the company's fields: " + illegalAccessException.getMessage() + "."); // Handle the exception appropriately
            }
        }

        return query.toString().trim();
    }

    public static void printCompany(Company company) {
        String phoneNumbers = company.getPhoneNumbers();
        String socialMediaLinks = company.getSocialMediaLinks();
        String address = company.getAddress();

        if (StringUtils.hasLength(phoneNumbers) || StringUtils.hasLength(socialMediaLinks) || StringUtils.hasLength(address)) {
            LOGGER.info("Company extracted from URL: " + company.getUrl() +
                    (StringUtils.hasLength(phoneNumbers) ? " has phone number(s): " + phoneNumbers : "") +
                    (StringUtils.hasLength(socialMediaLinks) ? ", social media link(s): " + socialMediaLinks : "") +
                    (StringUtils.hasLength(address) ? ", address: " + address : "") +
                    ".");
        } else {
            LOGGER.info("Company extracted from URL: " + company.getUrl() + " - no datapoints could be extracted.");
        }
    }
}
