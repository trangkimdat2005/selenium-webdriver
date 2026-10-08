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
    // NHÓM A: TC_FN_01 .. TC_FN_05  (Đăng nhập cơ bản + Sai mật khẩu)
    // ========================================================================

    /** TC_FN_01 - Đăng nhập thành công với tài khoản hợp lệ. */
    @Test(description = "TC_FN_01 - Đăng nhập thành công với tài khoản hợp lệ")
    public void tc_fn_01_loginSuccessWithValidAccount() {
        loginPage.typeUsername(validUsername)
                 .typePassword(validPassword)
                 .clickLogin();
        Assert.assertTrue(loginPage.isLoggedIn(),
                "Đăng nhập thất bại với tài khoản hợp lệ. URL: " + loginPage.getCurrentUrl());
    }

    // ========================================================================
    // Các TC tiếp theo sẽ được thêm từng commit theo workspace rule
    // ========================================================================
        /** TC_FN_02 - Đăng nhập bằng phím Enter. */
    @Test(description = "TC_FN_02 - Đăng nhập bằng phím Enter")
    public void tc_fn_02_loginWithEnterKey() {
        loginPage.typeUsername(validUsername)
                 .typePassword(validPassword)
                 .submitWithEnterOnPassword();
        Assert.assertTrue(loginPage.isLoggedIn(),
                "Phím Enter không submit form. URL: " + loginPage.getCurrentUrl());
    }

    /** TC_FN_03 - Chuyển hướng theo tham số r. */
    @Test(description = "TC_FN_03 - Chuyển hướng theo tham số r")
    public void tc_fn_03_redirectByParamR() {
        String r = "https://vanphongdientu.utc.edu.vn/";
        driver.get(LOGIN_URL + "?r=" + java.net.URLEncoder.encode(r, java.nio.charset.StandardCharsets.UTF_8));
        loginPage.waitForPageLoaded();
        loginPage.typeUsername(validUsername)
                 .typePassword(validPassword)
                 .clickLogin();
        Assert.assertFalse(loginPage.isOnLoginPage(),
                "Vẫn còn ở trang login, không chuyển hướng. URL: " + loginPage.getCurrentUrl());
    }

    /** TC_FN_04 - Đăng nhập sai mật khẩu. */
    @Test(description = "TC_FN_04 - Đăng nhập sai mật khẩu")
    public void tc_fn_04_loginWithWrongPassword() {
        loginPage.typeUsername(validUsername)
                 .typePassword("SaiMatKhau1")
                 .clickLogin();
        Assert.assertTrue(loginPage.isOnLoginPage(),
                "Sai mật khẩu mà lại chuyển trang. URL: " + loginPage.getCurrentUrl());
        String err = loginPage.getLoginErrorMessage();
        Assert.assertFalse(err.isEmpty(),
                "Phải hiển thị thông báo lỗi khi sai mật khẩu, nhưng không thấy");
    }

    /** TC_FN_05 - Đăng nhập với tên đăng nhập không tồn tại. */
    @Test(description = "TC_FN_05 - Tên đăng nhập không tồn tại")
    public void tc_fn_05_loginWithUnknownUsername() {
        loginPage.typeUsername("khongtontai_9999")
                 .typePassword("anypassword")
                 .clickLogin();
        Assert.assertTrue(loginPage.isOnLoginPage(),
                "Username không tồn tại mà lại đăng nhập được. URL: " + loginPage.getCurrentUrl());
        String err = loginPage.getLoginErrorMessage();
        Assert.assertFalse(err.isEmpty(),
                "Phải hiển thị thông báo lỗi khi username không tồn tại");
    }

// ===== TC_NEXT =====

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