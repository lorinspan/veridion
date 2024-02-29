package com.veridion.assignment.scraper;

import com.veridion.assignment.model.Company;
import com.veridion.assignment.model.CompanyUtil;
import com.veridion.assignment.utils.Utils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ScraperService implements Callable<Void> {
    private static final String CHROME_DRIVER = "webdriver.chrome.driver";
    private static final String CHROME_DRIVER_PATH = "C:\\Users\\lorin\\IdeaProjects\\chrome-win64\\chromedriver.exe";
    private static final String CHROME_PATH = "C:\\Users\\lorin\\IdeaProjects\\chrome-win64\\chrome.exe";
    private static final String HREF = "href";
    private static final String PHONE_NUMBER_REGEX = "\\(?(\\d{3})\\)?[\\s.-](\\d{3})[\\s.-](\\d{4})";
    private static final String SOCIAL_MEDIA_LINKS_SELECTOR = "a[href*='facebook.com']";
    private static final String ADDRESS_XPATH = "//a[contains(@href, 'maps.google.com')]";
    private static final String CONTACT_SELECTOR = "a[href*='contact']";

    private static final Logger LOGGER = LoggerFactory.getLogger(ScraperService.class);
    // Can probably reduce this time in order to get faster times at the cost of accuracy. Less time given to site to load -> less accuracy.
    private static final Duration PAGE_LOAD_TIMEOUT_SECONDS = Duration.ofSeconds(20);
    private String url;

    protected static final AtomicInteger successfulCrawls = new AtomicInteger(0);
    protected static final AtomicInteger phoneNumbersFound = new AtomicInteger(0);
    protected static final AtomicInteger socialMediaLinksFound = new AtomicInteger(0);
    protected static final AtomicInteger addressesFound = new AtomicInteger(0);
    private static final List<Company> companies = Collections.synchronizedList(new ArrayList<>());

    private ScraperService() {
        this.url = null;
    }

    public ScraperService(String url) {
        this.url = url;
    }

    @Override
    public Void call() {
        WebDriver driver = null;
        try {
            System.setProperty(CHROME_DRIVER, CHROME_DRIVER_PATH);

            ChromeOptions chromeOptions = new ChromeOptions();
            chromeOptions.addArguments("--headless");
            chromeOptions.setBinary(CHROME_PATH);
            driver = new ChromeDriver(chromeOptions);

            driver.manage().timeouts().pageLoadTimeout(PAGE_LOAD_TIMEOUT_SECONDS);

            if (StringUtils.hasLength(url) && !url.trim().startsWith("https://") && !url.trim().startsWith("http://")) {
                url = "https://" + url.trim();
            }

            driver.get(url);

            Company company = scrapeCompanyInfo(driver);
            company.setUrl(url);

            Company contactCompany = null;

            if(!company.isComplete(false)) {
                try {
                    navigateToContactPage(driver);
                    contactCompany = scrapeCompanyInfo(driver);
                } catch (Exception ignored) {}
            }

            if(contactCompany != null) {
                mergeCompanyInfo(company, contactCompany);
            }

            successfulCrawls.incrementAndGet();

            addCompany(company);

            CompanyUtil.printCompany(company);

        } catch (Exception exception) {
            LOGGER.error("Could not open URL: " + url + ".");
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
        return null;
    }

    private void navigateToContactPage(WebDriver driver) {
        List<WebElement> contactLinks = driver.findElements(By.cssSelector(CONTACT_SELECTOR));

        for (WebElement link : contactLinks) {
            String href = link.getAttribute(HREF);
            if (href != null && href.contains("contact")) {
                driver.get(href);
                break;
            }
        }
    }

    private void mergeCompanyInfo(Company mainCompany, Company contactCompany) {
        Utils.mergeField(mainCompany::setPhoneNumbers, mainCompany.getPhoneNumbers(), contactCompany.getPhoneNumbers());
        Utils.mergeField(mainCompany::setAddress, mainCompany.getAddress(), contactCompany.getAddress());
        Utils.mergeField(mainCompany::setSocialMediaLinks, mainCompany.getSocialMediaLinks(), contactCompany.getSocialMediaLinks());
    }

    private Company scrapeCompanyInfo(WebDriver driver) {
        Company company = new Company();

        company.setPhoneNumbers(findPhoneNumbers(driver));
        company.setSocialMediaLinks(findSocialMediaLinks(driver));
        company.setAddress(findAddresses(driver));

        return company;
    }

    private String findPhoneNumbers(WebDriver driver) {
        StringBuilder phoneNumberString = new StringBuilder();
        Set<String> uniqueNumbers = new HashSet<>();

        Pattern pattern = Pattern.compile(PHONE_NUMBER_REGEX);

        String pageSource = driver.getPageSource();

        findUniqueItems(pageSource, pattern, uniqueNumbers, phoneNumberString);

        incrementDatapointFound(phoneNumbersFound, phoneNumberString);

        return phoneNumberString.toString();
    }

    private String findSocialMediaLinks(WebDriver driver) {
        StringBuilder socialMediaLinksString = new StringBuilder();
        Set<String> uniqueLinks = new HashSet<>();

        List<WebElement> socialMediaLinks = driver.findElements(By.cssSelector(SOCIAL_MEDIA_LINKS_SELECTOR));

        for (WebElement link : socialMediaLinks) {
            String href = link.getAttribute(HREF);
            Utils.addUniqueElement(href, uniqueLinks, socialMediaLinksString);
        }

        incrementDatapointFound(socialMediaLinksFound, socialMediaLinksString);

        return socialMediaLinksString.toString();
    }

    private String findAddresses(WebDriver driver) {
        StringBuilder addressesString = new StringBuilder();
        Set<String> uniqueAddresses = new HashSet<>();

        List<WebElement> addressElements = driver.findElements(By.xpath(ADDRESS_XPATH));

        for (WebElement addressElement : addressElements) {
            String address = addressElement.getText().trim();
            Utils.addUniqueElement(address, uniqueAddresses, addressesString);
        }

        incrementDatapointFound(addressesFound, addressesString);

        return addressesString.toString();
    }

    private void incrementDatapointFound(AtomicInteger atomicInteger, StringBuilder stringBuilder) {
        if(StringUtils.hasLength(stringBuilder.toString())) {
            atomicInteger.incrementAndGet();
        }
    }

    private void findUniqueItems(String pageSource, Pattern pattern, Set<String> uniqueItems, StringBuilder stringBuilder) {
        Matcher matcher = pattern.matcher(pageSource);
        while (matcher.find()) {
            String item = matcher.group();
            Utils.addUniqueElement(item, uniqueItems, stringBuilder);
        }
    }

    public static void printDataAnalysis(long startTime, List<Company> companies) {
        long endTime = System.currentTimeMillis();
        LOGGER.info("Calculated in: " + String.format("%.3f", (endTime - startTime) / 1000.0) + " seconds.");
        try {
            LOGGER.info("Website crawl coverage: " + ScraperService.successfulCrawls + " out of " + companies.size() + ". Percentage: " + getCoverage(companies, Company::getUrl) + ".");
            LOGGER.info("Datapoints extracted: " + ScraperService.phoneNumbersFound + " (" + getCoverage(companies, Company::getPhoneNumbers) + ") phone numbers, " + ScraperService.socialMediaLinksFound + " (" + getCoverage(companies, Company::getSocialMediaLinks) + ") social media links and " + ScraperService.addressesFound + " (" + getCoverage(companies, Company::getAddress) + ") addresses found.");
        } catch (IllegalStateException illegalStateException) {
            LOGGER.error("An error has occurred while accessing CSVReader singleton: " + illegalStateException.getMessage());
        }
    }

    public static void resetCounters() {
        companies.clear();
        ScraperService.successfulCrawls.set(0);
        ScraperService.phoneNumbersFound.set(0);
        ScraperService.socialMediaLinksFound.set(0);
        ScraperService.addressesFound.set(0);
    }

    private static String getCoverage(List<Company> companies, Function<Company, String> extractorFunction) {
        AtomicInteger numberOfMatches = new AtomicInteger();

        companies.forEach(company -> {
            String data = extractorFunction.apply(company);
            if (StringUtils.hasLength(data)) {
                numberOfMatches.getAndIncrement();
            }
        });

        return String.format("%.2f", numberOfMatches.get() / (double) companies.size() * 100) + "%";
    }

    private void addCompany(Company company) {
        companies.add(company);
    }

    public static List<Company> getCompanies() {
        return companies;
    }
}