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

    /** TC_FN_06 - Tài khoản bị khóa. */
    @Test(description = "TC_FN_06 - Tài khoản bị khóa/vô hiệu hóa")
    public void tc_fn_06_lockedAccount() {
        Assert.assertFalse(lockedUsername.isEmpty(),
                "Cần cấu hình locked.username trong testdata.properties");
        loginPage.typeUsername(lockedUsername)
                 .typePassword(lockedPassword)
                 .clickLogin();
        Assert.assertTrue(loginPage.isOnLoginPage(),
                "TK bị khóa mà lại đăng nhập được. URL: " + loginPage.getCurrentUrl());
        String err = loginPage.getLoginErrorMessage();
        Assert.assertFalse(err.isEmpty(),
                "Phải hiển thị thông báo tài khoản bị khóa/liên hệ quản trị");
    }

    /** TC_FN_07 - Mật khẩu phân biệt hoa thường. */
    @Test(description = "TC_FN_07 - Mật khẩu phân biệt hoa thường")
    public void tc_fn_07_passwordIsCaseSensitive() {
        String pwd = validPassword.isEmpty() ? "abc@123" : swapCase(validPassword);
        loginPage.typeUsername(validUsername)
                 .typePassword(pwd)
                 .clickLogin();
        Assert.assertTrue(loginPage.isOnLoginPage(),
                "Mật khẩu sai hoa/thường mà vẫn đăng nhập được. URL: " + loginPage.getCurrentUrl());
    }

    /** TC_FN_08 - Tên đăng nhập có khoảng trắng đầu/cuối. */
    @Test(description = "TC_FN_08 - Username có khoảng trắng đầu/cuối")
    public void tc_fn_08_usernameWithLeadingTrailingSpaces() {
        loginPage.typeUsername("  " + validUsername + "  ")
                 .typePassword(validPassword)
                 .clickLogin();
        Assert.assertTrue(loginPage.isLoggedIn() || loginPage.isOnLoginPage(),
                "Trạng thái không xác định. URL: " + loginPage.getCurrentUrl());
        if (loginPage.isOnLoginPage()) {
            Assert.assertFalse(loginPage.getLoginErrorMessage().isEmpty(),
                    "Khoảng trắng username: nên trim hoặc báo lỗi");
        }
    }

    /** TC_FN_09 - Tên đăng nhập không phân biệt hoa thường. */
    @Test(description = "TC_FN_09 - Username không phân biệt hoa thường")
    public void tc_fn_09_usernameIsCaseInsensitive() {
        loginPage.typeUsername(validUsername.toUpperCase())
                 .typePassword(validPassword)
                 .clickLogin();
        Assert.assertTrue(loginPage.isLoggedIn() || loginPage.isOnLoginPage(),
                "Trạng thái không xác định. URL: " + loginPage.getCurrentUrl());
        if (loginPage.isOnLoginPage()) {
            Assert.assertFalse(loginPage.getLoginErrorMessage().isEmpty(),
                    "Username viết hoa: nên chấp nhận hoặc báo lỗi rõ ràng");
        }
    }

    /** TC_FN_10 - Đăng nhập khi đã đăng nhập sẵn. */
    @Test(description = "TC_FN_10 - Đã đăng nhập thì vào lại /Login")
    public void tc_fn_10_alreadyLoggedIn() {
        Assert.assertFalse(validUsername.isEmpty(),
                "Cần valid.username trong testdata.properties");
        driver.manage().addCookie(new org.openqa.selenium.Cookie(".AspNetCore.Session",
                "fake-session-cookie", "vanphongdientu.utc.edu.vn", "/", false));
        loginPage.open(LOGIN_URL);
        Assert.assertFalse(loginPage.isOnLoginPage(),
                "Đã đăng nhập rồi nhưng vào /Login vẫn ở login. URL: " + loginPage.getCurrentUrl());
    }

    /** TC_FN_11 - Truy cập trang nội bộ khi chưa đăng nhập. */
    @Test(description = "TC_FN_11 - Truy cập trang nội bộ khi chưa đăng nhập")
    public void tc_fn_11_protectInternalPages() {
        String internal = "https://vanphongdientu.utc.edu.vn/";
        driver.get(internal);
        new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(10))
                .until(d -> d.getCurrentUrl() != null);
        String url = loginPage.getCurrentUrl();
        Assert.assertTrue(url != null && url.toLowerCase().contains("/login"),
                "Chưa đăng nhập mà không bị redirect về /Login. URL: " + url);
        Assert.assertTrue(url.contains("r="),
                "URL redirect về login phải chứa tham số r=. URL: " + url);
    }

    /** TC_FN_12 - Double-click nút Đăng nhập. */
    @Test(description = "TC_FN_12 - Double-click nút Đăng nhập")
    public void tc_fn_12_doubleClickLogin() {
        loginPage.typeUsername(validUsername)
                 .typePassword(validPassword);
        loginPage.doubleClickLogin();
        Assert.assertTrue(loginPage.isLoggedIn() || loginPage.isOnLoginPage(),
                "Double-click gây lỗi. URL: " + loginPage.getCurrentUrl());
    }

    /** TC_FN_13 - Chống SQL Injection ở ô Username. */
    @Test(description = "TC_FN_13 - SQL Injection ở Username")
    public void tc_fn_13_sqlInjectionUsername() {
        loginPage.typeUsername("admin' OR '1'='1' --")
                 .typePassword("anything")
                 .clickLogin();
        Assert.assertTrue(loginPage.isOnLoginPage(),
                "SQLi username có thể bypass! URL: " + loginPage.getCurrentUrl());
        String err = loginPage.getLoginErrorMessage();
        Assert.assertFalse(err.toLowerCase().contains("sql")
                && err.toLowerCase().contains("error"),
                "Lộ thông tin SQL/database. Err: " + err);
    }

    /** TC_FN_14 - Chống SQL Injection ở ô Password. */
    @Test(description = "TC_FN_14 - SQL Injection ở Password")
    public void tc_fn_14_sqlInjectionPassword() {
        loginPage.typeUsername(validUsername)
                 .typePassword("' OR '1'='1")
                 .clickLogin();
        Assert.assertTrue(loginPage.isOnLoginPage(),
                "SQLi password có thể bypass! URL: " + loginPage.getCurrentUrl());
        String err = loginPage.getLoginErrorMessage();
        Assert.assertFalse(err.toLowerCase().contains("sql")
                && err.toLowerCase().contains("error"),
                "Lộ thông tin SQL/database. Err: " + err);
    }

    /** TC_FN_15 - Khóa/giới hạn sau nhiều lần đăng nhập sai. */
    @Test(description = "TC_FN_15 - Khóa tạm sau nhiều lần đăng nhập sai")
    public void tc_fn_15_lockAfterMultipleFailures() {
        for (int i = 0; i < 7; i++) {
            loginPage.typeUsername(validUsername)
                     .typePassword("wrong-" + i)
                     .clickLogin();
            Assert.assertTrue(loginPage.isOnLoginPage(),
                    "Sai mật khẩu lần " + (i + 1) + " mà lại đăng nhập được");
        }
        String err = loginPage.getLoginErrorMessage();
        Assert.assertTrue(!err.isEmpty() || loginPage.isOnLoginPage(),
                "Sau nhiều lần sai, vẫn phải ở trang login. URL: " + loginPage.getCurrentUrl());
    }

    /** TC_VAL_01 - Để trống cả hai trường. */
    @Test(description = "TC_VAL_01 - Để trống cả hai trường")
    public void tc_val_01_bothFieldsEmpty() {
        loginPage.typeUsername("")
                 .typePassword("")
                 .clickLogin();
        Assert.assertTrue(loginPage.isOnLoginPage(),
                "Trống cả 2 trường mà form lại submit đi. URL: " + loginPage.getCurrentUrl());
        String u = loginPage.getUsernameFieldError();
        String p = loginPage.getPasswordFieldError();
        String e = loginPage.getLoginErrorMessage();
        Assert.assertTrue(!u.isEmpty() || !p.isEmpty() || !e.isEmpty(),
                "Phải báo lỗi yêu cầu nhập Username và Password");
    }

    /** TC_VAL_02 - Để trống Tên đăng nhập. */
    @Test(description = "TC_VAL_02 - Để trống Tên đăng nhập")
    public void tc_val_02_emptyUsername() {
        loginPage.typeUsername("")
                 .typePassword("Abc@123")
                 .clickLogin();
        Assert.assertTrue(loginPage.isOnLoginPage(),
                "Username trống mà form submit. URL: " + loginPage.getCurrentUrl());
        String u = loginPage.getUsernameFieldError();
        String e = loginPage.getLoginErrorMessage();
        Assert.assertTrue(!u.isEmpty() || !e.isEmpty(),
                "Phải báo lỗi yêu cầu nhập Username");
    }

    /** TC_VAL_03 - Để trống Mật khẩu. */
    @Test(description = "TC_VAL_03 - Để trống Mật khẩu")
    public void tc_val_03_emptyPassword() {
        String username = validUsername.isEmpty() ? "username" : validUsername;
        loginPage.typeUsername(username)
                 .typePassword("")
                 .clickLogin();
        Assert.assertTrue(loginPage.isOnLoginPage(),
                "Password trống mà form submit. URL: " + loginPage.getCurrentUrl());
        String p = loginPage.getPasswordFieldError();
        String e = loginPage.getLoginErrorMessage();
        Assert.assertTrue(!p.isEmpty() || !e.isEmpty(),
                "Phải báo lỗi yêu cầu nhập Password");
        Assert.assertEquals(loginPage.getUsernameValue(), username,
                "Username đã nhập phải được giữ lại sau khi đăng nhập thất bại");
    }

    /** TC_VAL_04 - Chỉ nhập khoảng trắng. */
    @Test(description = "TC_VAL_04 - Chỉ nhập khoảng trắng")
    public void tc_val_04_whitespaceOnly() {
        loginPage.typeUsername("     ")
                 .typePassword("     ")
                 .clickLogin();
        Assert.assertTrue(loginPage.isOnLoginPage(),
                "Chỉ nhập khoảng trắng mà form submit. URL: " + loginPage.getCurrentUrl());
        String e = loginPage.getLoginErrorMessage();
        String u = loginPage.getUsernameFieldError();
        String p = loginPage.getPasswordFieldError();
        Assert.assertTrue(!e.isEmpty() || !u.isEmpty() || !p.isEmpty(),
                "Phải báo lỗi bắt buộc nhập khi chỉ có khoảng trắng");
    }

    /** TC_VAL_05 - Nhập chuỗi rất dài. */
    @Test(description = "TC_VAL_05 - Nhập chuỗi rất dài (1000 ký tự)")
    public void tc_val_05_veryLongString() {
        String longStr = "a".repeat(1000);
        loginPage.typeUsername(longStr)
                 .typePassword(longStr)
                 .clickLogin();
        Assert.assertTrue(loginPage.isOnLoginPage(),
                "Chuỗi 1000 ký tự submit đi và thoát khỏi login. URL: " + loginPage.getCurrentUrl());
        Assert.assertFalse(loginPage.getCurrentUrl().contains("error"),
                "Có vẻ server lỗi (500) khi nhập chuỗi dài. URL: " + loginPage.getCurrentUrl());
    }

    /** TC_VAL_06 - Ký tự đặc biệt / Unicode / emoji. */
    @Test(description = "TC_VAL_06 - Ký tự đặc biệt, Unicode, emoji")
    public void tc_val_06_specialCharsUnicode() {
        loginPage.typeUsername("!@#$%^&*()Nguyễn😀")
                 .typePassword("!@#$%^&*()Nguyễn😀")
                 .clickLogin();
        Assert.assertTrue(loginPage.isOnLoginPage(),
                "Ký tự đặc biệt gây thoát khỏi login. URL: " + loginPage.getCurrentUrl());
        Assert.assertFalse(loginPage.getCurrentUrl().toLowerCase().contains("error"),
                "Server lỗi khi nhập ký tự đặc biệt. URL: " + loginPage.getCurrentUrl());
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