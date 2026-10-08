package com.example.tests;

import com.example.BaseTest;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
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

    @Test(description = "Tìm kiếm 'Selenium WebDriver' trên Google và kiểm tra kết quả")
    public void shouldReturnResultsWhenSearchingSelenium() {
        driver.get("https://www.google.com");

        // Ô tìm kiếm có thể là 'q' (desktop) hoặc 'APjFqb' (tùy region)
        WebElement searchBox = waitForVisible(By.name("q"));
        searchBox.clear();
        searchBox.sendKeys("Selenium WebDriver", Keys.ENTER);

        // Chờ kết quả xuất hiện (selector chung: tiêu đề kết quả)
        List<WebElement> results = wait.until(
                driver -> driver.findElements(By.cssSelector("h3"))
        );

        Assert.assertTrue(results.size() > 0, "Phải có ít nhất 1 kết quả tìm kiếm");

        boolean hasSeleniumResult = results.stream()
                .anyMatch(e -> e.getText().toLowerCase().contains("selenium"));
        Assert.assertTrue(hasSeleniumResult,
                "Phải có kết quả chứa từ 'selenium', tìm thấy: "
                        + results.stream().map(WebElement::getText).limit(3).toList());
    }
}
