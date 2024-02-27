package webdriver;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class WebDriverManager {
    private static final String CHROME_DRIVER = "webdriver.chrome.driver";
    private static final String CHROME_DRIVER_PATH = "C:\\Users\\lorin\\IdeaProjects\\assignment\\chrome-win64\\chromedriver.exe";
    private static final String CHROME_PATH = "C:\\Users\\lorin\\IdeaProjects\\assignment\\chrome-win64\\chrome.exe";
    private static WebDriver driver;

    // Private constructor to prevent instantiation from outside
    private WebDriverManager() {}

    // Static method to get the WebDriver instance
    public static synchronized WebDriver getDriver() {
        if (driver == null) {
            // Initialize WebDriver
            System.setProperty(CHROME_DRIVER, CHROME_DRIVER_PATH);

            ChromeOptions chromeOptions = new ChromeOptions();
            chromeOptions.addArguments("--headless");
            chromeOptions.addArguments("--disabled-gpu");
            chromeOptions.setBinary(CHROME_PATH);

            driver = new ChromeDriver(chromeOptions);
            // Additional options can be set here
        }
        return driver;
    }

    // Static method to quit the WebDriver instance
    public static synchronized void quitDriver() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }
}
