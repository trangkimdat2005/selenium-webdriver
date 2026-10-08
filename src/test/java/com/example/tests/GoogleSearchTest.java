package com.example.tests;

import com.example.BaseTest;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.util.List;

/**
 * Test tìm kiếm Google - bài mẫu cơ bản.
 * Truyền tham số "browser" qua testng.xml: chrome / edge / firefox.
 */
public class GoogleSearchTest extends BaseTest {

    @Parameters("browser")
    @BeforeMethod
    public void setUp(@Optional("chrome") String browser) {
        initDriver(browser);
    }

    @AfterMethod
    public void tearDown(ITestResult result) {
        if (!result.isSuccess()) {
            captureOnFailure(result.getName());
        }
        quitDriver();
    }

    @Test(description = "Tìm kiếm 'Selenium WebDriver' trên DuckDuckGo và kiểm tra kết quả")
    public void shouldReturnResultsWhenSearchingSelenium() {
        // DuckDuckGo thân thiện với automation hơn Google
        driver.get("https://duckduckgo.com");

        // Ô tìm kiếm của DuckDuckGo
        WebElement searchBox = waitForVisible(By.name("q"));
        searchBox.clear();
        searchBox.sendKeys("Selenium WebDriver", Keys.ENTER);

        // Chờ URL chuyển sang trang kết quả (chứa "q=Selenium")
        wait.until(ExpectedConditions.urlContains("q=Selenium"));

        // Xác minh qua URL và title
        String url = driver.getCurrentUrl();
        Assert.assertTrue(url.contains("q=Selenium") || url.contains("q=selenium"),
                "URL phải chứa query, thực tế: " + url);
        Assert.assertTrue(driver.getTitle().toLowerCase().contains("selenium"),
                "Title phải chứa từ khóa tìm kiếm, thực tế: " + driver.getTitle());

        // Chờ link kết quả xuất hiện
        List<WebElement> resultLinks = wait.until(driver ->
                driver.findElements(By.cssSelector("a[data-testid='result-title-a']"))
        );
        Assert.assertTrue(resultLinks.size() > 0,
                "Phải có ít nhất 1 link kết quả, tìm thấy 0");
    }
}
