package com.example;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.io.FileHandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Tiện ích chụp ảnh màn hình khi test thất bại.
 * Lưu vào thư mục "screenshots/" ở thư mục gốc dự án.
 */
public class ScreenshotUtil {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public static void capture(WebDriver driver, String testName) {
        try {
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            String fileName = testName + "_" + LocalDateTime.now().format(FMT) + ".png";
            Path dest = Paths.get("screenshots", fileName);
            FileHandler.copy(src, dest.toFile());
            System.out.println("[Screenshot] Saved: " + dest.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("[Screenshot] Failed: " + e.getMessage());
        }
    }
}
