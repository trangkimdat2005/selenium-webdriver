package com.example;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * BaseTest cung cấp:
 *  - Khởi tạo / đóng WebDriver cho mỗi test class
 *  - Đổi trình duyệt bằng tham số TestNG (parameter "browser")
 *  - Chụp ảnh màn hình khi test thất bại
 */
public abstract class BaseTest {

    protected WebDriver driver;
    protected WebDriverWait wait;

    /** Khởi tạo WebDriver theo tên trình duyệt (chrome/edge/firefox). */
    protected void initDriver(String browserName) {
        DriverFactory.Browser browser = DriverFactory.Browser.valueOf(browserName.toUpperCase());
        driver = DriverFactory.createDriver(browser);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    /** Đóng WebDriver. */
    protected void quitDriver() {
        if (driver != null) {
            driver.quit();
        }
    }

    protected void captureOnFailure(String testName) {
        if (driver != null) {
            ScreenshotUtil.capture(driver, testName);
        }
    }

    /** Chờ element hiển thị rồi trả về. */
    protected WebElement waitForVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /** Chờ element có thể click được. */
    protected WebElement waitForClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }
}
