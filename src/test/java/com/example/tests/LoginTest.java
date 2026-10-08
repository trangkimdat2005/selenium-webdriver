package com.example.tests;

import com.example.BaseTest;
import com.example.pages.LoginPage;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Test các chức năng đăng nhập Văn phòng điện tử UTC.
 * Tài liệu: TestCase_ChucNangDangNhap_VPDT_UTC.xlsx
 *
 * Mỗi test method tương ứng 1 TC; mỗi TC commit riêng theo workspace rule.
 * File testdata.properties chứa:
 *   - valid.username, valid.password  (tài khoản hợp lệ)
 *   - locked.username, locked.password (tài khoản bị khóa)
 */
public class LoginTest extends BaseTest {

    private static final String LOGIN_URL = "https://vanphongdientu.utc.edu.vn/Login";

    protected LoginPage loginPage;
    protected static String validUsername;
    protected static String validPassword;
    protected static String lockedUsername;
    protected static String lockedPassword;

    @BeforeClass
    public void loadTestData() throws IOException {
        Properties p = new Properties();
        try (InputStream in = getClass().getClassLoader()
                .getResourceAsStream("testdata.properties")) {
            Assert.assertNotNull(in, "Không tìm thấy testdata.properties trong classpath");
            p.load(in);
        }
        validUsername = p.getProperty("valid.username", "");
        validPassword = p.getProperty("valid.password", "");
        lockedUsername = p.getProperty("locked.username", "");
        lockedPassword = p.getProperty("locked.password", "");
    }

    @Parameters("browser")
    @BeforeMethod
    public void setUp(@Optional("chrome") String browser) {
        initDriver(browser);
        // luôn mở lại trang login sạch cho mỗi test
        loginPage = new LoginPage(driver).open(LOGIN_URL);
        // xóa cookie để các test case giả định "trình duyệt duyệt đã xóa cookie"
        driver.manage().deleteAllCookies();
        driver.navigate().refresh();
        loginPage = new LoginPage(driver); // bind lại sau refresh
    }

    @AfterMethod
    public void tearDown(ITestResult result) {
        if (!result.isSuccess()) {
            captureOnFailure(result.getName());
        }
        quitDriver();
    }

    // ========================================================================
    // Các TC sẽ được thêm vào từng commit theo workspace rule (1 TC = 1 commit)
    // ========================================================================

    /** Đảo hoa/thường cho mỗi ký tự chữ cái. */
    protected static String swapCase(String s) {
        if (s == null || s.isEmpty()) return s;
        StringBuilder sb = new StringBuilder(s.length());
        for (char c : s.toCharArray()) {
            if (Character.isUpperCase(c)) sb.append(Character.toLowerCase(c));
            else if (Character.isLowerCase(c)) sb.append(Character.toUpperCase(c));
            else sb.append(c);
        }
        return sb.toString();
    }
}