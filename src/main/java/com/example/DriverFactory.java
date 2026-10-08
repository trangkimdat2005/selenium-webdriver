package com.example;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.time.Duration;

/**
 * Factory tạo WebDriver cho các trình duyệt khác nhau.
 * Dùng WebDriverManager để tự động tải driver phù hợp với phiên bản trình duyệt.
 */
public class DriverFactory {

    public enum Browser {
        CHROME, EDGE, FIREFOX
    }

    public static WebDriver createDriver(Browser browser) {
        boolean headless = Boolean.parseBoolean(System.getProperty("headless", "false"));

        WebDriver driver = switch (browser) {
            case CHROME -> {
                WebDriverManager.chromedriver().setup();
                ChromeOptions opts = new ChromeOptions();
                if (headless) opts.addArguments("--headless=new");
                opts.addArguments("--start-maximized", "--disable-notifications");
                yield new ChromeDriver(opts);
            }
            case EDGE -> {
                WebDriverManager.edgedriver().setup();
                EdgeOptions opts = new EdgeOptions();
                if (headless) opts.addArguments("--headless=new");
                opts.addArguments("--start-maximized", "--disable-notifications");
                yield new EdgeDriver(opts);
            }
            case FIREFOX -> {
                WebDriverManager.firefoxdriver().setup();
                FirefoxOptions opts = new FirefoxOptions();
                if (headless) opts.addArguments("--headless");
                yield new FirefoxDriver(opts);
            }
        };

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
        return driver;
    }
}
