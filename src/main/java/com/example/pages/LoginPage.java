package com.example.pages;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Page Object Model cho trang đăng nhập Văn phòng điện tử UTC.
 * URL: https://vanphongdientu.utc.edu.vn/Login
 */
public class LoginPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // --- Locators (khớp DOM thật UTC, đã verify qua LoginDomProbe 2026-10-08) ---
    private final By usernameInput = By.cssSelector("input[name='username']");
    private final By passwordInput = By.cssSelector("input[name='userpwd']");
    private final By loginButton   = By.cssSelector("input.submit_login[type='submit']");
    private final By rememberMe    = By.id("persistent");
    private final By rememberLabel = By.cssSelector("label.check[for='persistent']");
    // Site thật chỉ có 1 div.error chung (sai pass / trống / không tồn tại).
    // Không có [data-valmsg-for] hay class alert-danger / text-danger.
    private final By loginErrorMsg = By.cssSelector("div.error");
    private final By usernameError = By.cssSelector("div.error");
    private final By passwordError = By.cssSelector("div.error");
    private final By userMenu      = By.cssSelector(".user-info, .user-menu, .account, [data-testid='user-menu']");

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    /** Mở trang login (đọc URL gốc từ property để dễ thay đổi). */
    public LoginPage open(String url) {
        driver.get(url);
        waitForPageLoaded();
        return this;
    }

    public void waitForPageLoaded() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(usernameInput));
    }

    public LoginPage typeUsername(String username) {
        WebElement el = waitForVisible(usernameInput);
        el.clear();
        if (username != null) el.sendKeys(username);
        return this;
    }

    public LoginPage typePassword(String password) {
        WebElement el = waitForVisible(passwordInput);
        el.clear();
        if (password != null) el.sendKeys(password);
        return this;
    }

    /**
     * Tick Remember Me. Checkbox `#persistent` bị `display:none` (site dùng
     * <label class="check"> làm giao diện) nên click qua label hoặc set checked bằng JS.
     */
    public LoginPage tickRememberMe() {
        toggleRememberMe(true);
        return this;
    }

    public LoginPage uncheckRememberMe() {
        toggleRememberMe(false);
        return this;
    }

    private void toggleRememberMe(boolean wantChecked) {
        WebElement cb = driver.findElement(rememberMe);
        boolean current = cb.isSelected();
        if (current == wantChecked) return;
        try {
            driver.findElement(rememberLabel).click();
            return;
        } catch (Exception ignored) {}
        ((JavascriptExecutor) driver).executeScript(
            "var c=document.getElementById('persistent');"
          + "if(c){c.checked=" + wantChecked + ";"
          + "c.dispatchEvent(new Event('change',{bubbles:true}));}", cb);
    }

    /** Trả về true nếu checkbox Remember Me đang được tick. */
    public boolean isRememberMeChecked() {
        try { return driver.findElement(rememberMe).isSelected(); }
        catch (Exception e) { return false; }
    }

    public LoginPage clickLogin() {
        waitForClickable(loginButton).click();
        return this;
    }

    /** Submit bằng phím Enter trong khi ô mật khẩu đang focus. */
    public LoginPage submitWithEnterOnPassword() {
        waitForVisible(passwordInput).sendKeys(Keys.ENTER);
        return this;
    }

    /** Double-click nút đăng nhập. */
    public void doubleClickLogin() {
        WebElement btn = waitForClickable(loginButton);
        // Dùng Actions để double-click chuẩn xác
        new org.openqa.selenium.interactions.Actions(driver)
                .doubleClick(btn)
                .perform();
    }

    /** Trả về text thông báo lỗi chung (sai mật khẩu / không tồn tại / SQL injection ...). */
    public String getLoginErrorMessage() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(loginErrorMsg)).getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    /** Trả về text lỗi validate của ô Username. */
    public String getUsernameFieldError() {
        try { return driver.findElement(usernameError).getText().trim(); }
        catch (Exception e) { return ""; }
    }

    /** Trả về text lỗi validate của ô Password. */
    public String getPasswordFieldError() {
        try { return driver.findElement(passwordError).getText().trim(); }
        catch (Exception e) { return ""; }
    }

    public String getUsernameValue() {
        return driver.findElement(usernameInput).getAttribute("value");
    }

    public String getPasswordValue() {
        return driver.findElement(passwordInput).getAttribute("value");
    }

    /** Kiểm tra đã chuyển trang (URL hoặc xuất hiện user menu) sau khi đăng nhập. */
    public boolean isLoggedIn() {
        // URL không còn chứa "/Login"
        String url = driver.getCurrentUrl();
        if (url != null && !url.toLowerCase().contains("/login")) {
            return true;
        }
        // Hoặc xuất hiện user menu / account widget
        try {
            return driver.findElement(userMenu).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isOnLoginPage() {
        String url = driver.getCurrentUrl();
        return url != null && url.toLowerCase().contains("/login");
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    // ---- helpers ----
    private WebElement waitForVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    private WebElement waitForClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }
}