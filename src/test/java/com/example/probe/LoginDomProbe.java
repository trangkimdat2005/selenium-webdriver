package com.example.probe;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.example.DriverFactory;

/**
 * Probe tạm thời để dump DOM trang login UTC thật nhằm debug 5 test fail.
 * KHÔNG thuộc test suite chính; file này chỉ chạy thủ công để inspect selector.
 *
 * Cách chạy:
 *   mvn -q test -Dtest=LoginDomProbe
 *
 * Kết quả ghi ra:
 *   target/login-dom-probe.txt
 */
public class LoginDomProbe {

    public static void main(String[] args) throws Exception {
        WebDriver driver = DriverFactory.createDriver(DriverFactory.Browser.CHROME);
        Path out = Paths.get("target", "login-dom-probe.txt");
        Files.createDirectories(out.getParent());

        StringBuilder log = new StringBuilder();
        append(log, "======================================================");
        append(log, "LOGIN DOM PROBE - " + java.time.LocalDateTime.now());
        append(log, "======================================================");

        try {
            // ---- 1) Trang thuần (chưa nhập gì) ----
            driver.get("https://vanphongdientu.utc.edu.vn/Login");
            sleep(2000);
            dumpSection(log, "1) PAGE_LOADED", driver);

            // ---- 2) Gõ username + password sai, submit → tìm error ----
            try {
                WebElement username = driver.findElement(By.cssSelector("input[placeholder='Tên đăng nhập'], input[name*='user' i], input[type='text']"));
                WebElement password = driver.findElement(By.cssSelector("input[placeholder='Mật khẩu'], input[type='password']"));
                username.clear();
                username.sendKeys("khongtontai_9999_probe");
                password.clear();
                password.sendKeys("anypassword123");
                // Tìm nút submit
                WebElement submit = findSubmitButton(driver);
                append(log, "\n--- 2) SUBMITTING with bad credentials ---");
                append(log, "  submit button found: " + (submit != null)
                        + (submit != null ? " tag=" + submit.getTagName() : ""));
                if (submit != null) {
                    submit.click();
                    sleep(3000);
                }
                dumpSection(log, "2) AFTER_BAD_SUBMIT", driver);
            } catch (Exception e) {
                append(log, "\n--- 2) BAD_SUBMIT FAILED: " + e.getMessage() + " ---");
            }

            // ---- 3) Refresh, submit form trống → tìm validation error ----
            try {
                driver.navigate().refresh();
                sleep(2000);
                WebElement submit = findSubmitButton(driver);
                if (submit != null) {
                    submit.click();
                    sleep(3000);
                }
                dumpSection(log, "3) AFTER_EMPTY_SUBMIT", driver);
            } catch (Exception e) {
                append(log, "\n--- 3) EMPTY_SUBMIT FAILED: " + e.getMessage() + " ---");
            }

            // ---- 4) Tìm các tab role (HSSV/CBGD/Email UTC) ----
            try {
                List<WebElement> tabs = driver.findElements(
                        By.xpath("//a[contains(text(),'HSSV') or contains(text(),'CBGD') or contains(text(),'e-mail UTC')]"));
                append(log, "\n--- 4) ROLE_TABS found=" + tabs.size() + " ---");
                for (WebElement t : tabs) {
                    append(log, "  tab: text='" + safe(t.getText()) + "' tag=" + t.getTagName()
                            + " href=" + safe(t.getAttribute("href")));
                }
            } catch (Exception e) {
                append(log, "\n--- 4) ROLE_TABS FAILED: " + e.getMessage() + " ---");
            }

            // ---- 5) Tìm tất cả checkbox trên trang ----
            try {
                List<WebElement> checkboxes = driver.findElements(By.cssSelector("input[type='checkbox']"));
                append(log, "\n--- 5) CHECKBOXES found=" + checkboxes.size() + " ---");
                for (WebElement cb : checkboxes) {
                    append(log, "  cb: id=" + safe(cb.getAttribute("id"))
                            + " name=" + safe(cb.getAttribute("name"))
                            + " class=" + safe(cb.getAttribute("class"))
                            + " label=" + extractAssociatedLabel(driver, cb));
                }
            } catch (Exception e) {
                append(log, "\n--- 5) CHECKBOXES FAILED: " + e.getMessage() + " ---");
            }

        } finally {
            Files.writeString(out, log.toString(), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("\n[PROBE] Saved: " + out.toAbsolutePath());
            driver.quit();
        }
    }

    private static void dumpSection(StringBuilder log, String label, WebDriver driver) {
        append(log, "\n======================================================");
        append(log, label + " | URL=" + driver.getCurrentUrl() + " | TITLE=" + driver.getTitle());
        append(log, "======================================================");

        // (a) Full HTML body — cắt còn 12k để log không phình quá
        String body = driver.findElement(By.tagName("body")).getAttribute("outerHTML");
        append(log, "\n--- BODY HTML (first 12000 chars) ---");
        append(log, body.length() > 12000 ? body.substring(0, 12000) + "...[TRUNC]" : body);

        // (b) Tất cả element có id/class chứa "error/alert/danger/invalid/text-danger"
        try {
            List<WebElement> errs = driver.findElements(By.cssSelector(
                    "[class*='error'], [class*='Error'], [class*='alert'], [class*='Alert'], "
                    + "[class*='danger'], [class*='Danger'], [class*='invalid'], [class*='Invalid'], "
                    + "[role='alert'], [data-valmsg-for]"));
            append(log, "\n--- ERROR-LIKE ELEMENTS count=" + errs.size() + " ---");
            for (WebElement e : errs) {
                if (!e.isDisplayed()) continue;
                append(log, "  tag=" + e.getTagName()
                        + " id=" + safe(e.getAttribute("id"))
                        + " class=" + safe(e.getAttribute("class"))
                        + " role=" + safe(e.getAttribute("role"))
                        + " text='" + safe(e.getText()) + "'");
            }
        } catch (Exception ignored) {}

        // (c) Tất cả input
        try {
            List<WebElement> inputs = driver.findElements(By.cssSelector("input"));
            append(log, "\n--- INPUTS count=" + inputs.size() + " ---");
            for (WebElement i : inputs) {
                append(log, "  type=" + safe(i.getAttribute("type"))
                        + " id=" + safe(i.getAttribute("id"))
                        + " name=" + safe(i.getAttribute("name"))
                        + " class=" + safe(i.getAttribute("class"))
                        + " placeholder=" + safe(i.getAttribute("placeholder")));
            }
        } catch (Exception ignored) {}

        // (d) Tất cả button
        try {
            List<WebElement> btns = driver.findElements(By.cssSelector("button, input[type='submit'], input[type='button']"));
            append(log, "\n--- BUTTONS count=" + btns.size() + " ---");
            for (WebElement b : btns) {
                append(log, "  tag=" + b.getTagName()
                        + " type=" + safe(b.getAttribute("type"))
                        + " class=" + safe(b.getAttribute("class"))
                        + " value=" + safe(b.getAttribute("value"))
                        + " text='" + safe(b.getText()) + "'");
            }
        } catch (Exception ignored) {}
    }

    private static WebElement findSubmitButton(WebDriver driver) {
        // Thử nhiều khả năng
        String[] selectors = {
                "input[type='submit']",
                "input.submit_login",
                "button[type='submit']",
                "button.btn-login",
                "//input[@value='Đăng nhập']",
                "//button[contains(text(),'Đăng nhập')]"
        };
        for (String sel : selectors) {
            try {
                if (sel.startsWith("//")) {
                    return driver.findElement(By.xpath(sel));
                }
                return driver.findElement(By.cssSelector(sel));
            } catch (Exception ignored) {}
        }
        return null;
    }

    private static String extractAssociatedLabel(WebDriver driver, WebElement cb) {
        try {
            String id = cb.getAttribute("id");
            if (id != null && !id.isEmpty()) {
                WebElement lbl = driver.findElement(By.cssSelector("label[for='" + id + "']"));
                return safe(lbl.getText());
            }
        } catch (Exception ignored) {}
        try {
            WebElement lbl = cb.findElement(By.xpath("./following-sibling::label[1]"));
            return safe(lbl.getText());
        } catch (Exception ignored) {}
        return "";
    }

    private static String safe(String s) {
        return s == null ? "" : s.replace('\n', ' ').replace('\r', ' ').trim();
    }

    private static void append(StringBuilder sb, String s) {
        sb.append(s).append('\n');
    }

    private static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
