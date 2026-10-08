# Hướng Dẫn Làm Việc Với Project `webdriver-selenium`

> Tài liệu này dành cho dự án **Selenium WebDriver + TestNG** tại
> `D:\code\ky_I_nam_4\KiemThuPhanMem\webdriver-selenium`.
> Mục tiêu: giúp bạn (sinh viên / người mới) **cài đặt → chạy được → viết test mới → commit đúng chuẩn**.

---

## Mục lục

1. [Tổng quan project](#1-tổng-quan-project)
2. [Cài đặt môi trường](#2-cài-đặt-môi-trường)
3. [Cấu trúc thư mục & file quan trọng](#3-cấu-trúc-thư-mục--file-quan-trọng)
4. [Chạy test bằng Maven](#4-chạy-test-bằng-maven)
5. [Đổi trình duyệt / chạy headless](#5-đổi-trình-duyệt--chạy-headless)
6. [Viết một test case mới (walkthrough)](#6-viết-một-test-case-mới-walkthrough)
7. [Thêm Page Object mới](#7-thêm-page-object-mới)
8. [Quy trình commit & push (workspace rule)](#8-quy-trình-commit--push-workspace-rule)
9. [Khi test fail — xử lý ảnh chụp & log](#9-khi-test-fail--xử-lý-ảnh-chụp--log)
10. [Lỗi thường gặp & cách fix](#10-lỗi-thường-gặp--cách-fix)

---

## 1. Tổng quan project

| Thành phần | Giá trị |
| --- | --- |
| Ngôn ngữ | Java 21 (target), build được trên Java 25 LTS |
| Build tool | Maven 3.9+ |
| Test framework | TestNG 7.10.x |
| Automation | Selenium WebDriver 4.25.x |
| Driver manager | WebDriverManager 5.9.x (tự tải driver) |
| Assertion | AssertJ 3.26.x |
| Trình duyệt hỗ trợ | Chrome / Edge / Firefox (headless tùy chọn) |

**Công thức vàng:** WebDriverManager **tự động tải** `chromedriver` / `msedgedriver` / `geckodriver` đúng phiên bản trình duyệt của bạn — bạn **không cần tải thủ công**.

---

## 2. Cài đặt môi trường

### 2.1. Yêu cầu

- **Java 21+** (khuyến nghị cài Java 25 LTS).
- **Maven 3.8+** — đã cài sẵn tại `D:\tools\apache-maven-3.9.16\` và đã thêm vào `PATH` user.
- **Trình duyệt**: Chrome (khuyến nghị), Edge, hoặc Firefox.

### 2.2. Kiểm tra nhanh

Mở PowerShell và chạy:

```powershell
java -version
mvn -version
```

Nếu cả hai in ra phiên bản → môi trường OK.

### 2.3. Cài Maven (nếu máy khác)

```powershell
# Tải Apache Maven 3.9.16 (Tencent mirror — nhanh ở VN)
Invoke-WebRequest -Uri "https://mirrors.cloud.tencent.com/apache/maven/maven-3/3.9.16/binaries/apache-maven-3.9.16-bin.zip" -OutFile "$env:TEMP\maven.zip"
Expand-Archive -Path "$env:TEMP\maven.zip" -DestinationPath "D:\tools"
# Thêm D:\tools\apache-maven-3.9.16\bin vào PATH user
```

### 2.4. Cài Chrome (nếu chưa có)

```powershell
winget install --id Google.Chrome -e --source winget
```

---

## 3. Cấu trúc thư mục & file quan trọng

```
webdriver-selenium/
├── pom.xml                              # Cấu hình Maven + dependencies
├── README.md                            # Tóm tắt ngắn
├── docs/HD.md                           # File bạn đang đọc
├── screenshots/                         # Ảnh chụp khi test fail
└── src/
    ├── main/java/com/example/
    │   ├── DriverFactory.java           # Factory tạo WebDriver (chrome/edge/firefox)
    │   ├── BaseTest.java                # Lớp cha cho test class
    │   ├── ScreenshotUtil.java          # Chụp ảnh khi fail
    │   └── pages/
    │       └── LoginPage.java           # Page Object cho trang đăng nhập
    └── test/
        ├── java/com/example/tests/
        │   ├── GoogleSearchTest.java    # Test mẫu DuckDuckGo
        │   └── LoginTest.java           # 25 TC đăng nhập VPDT UTC
        └── resources/
            ├── testng.xml               # Cấu hình suite TestNG
            └── testdata.properties      # Tài khoản test
```

### Vai trò từng file

| File | Vai trò |
| --- | --- |
| `pom.xml` | Khai báo Selenium, TestNG, WebDriverManager, AssertJ; ép Java 21; trỏ Surefire vào `testng.xml`. |
| `DriverFactory.java` | Hàm `createDriver(Browser)` — trả về `WebDriver` đã cấu hình (maximized, no-notification). |
| `BaseTest.java` | Cung cấp `initDriver()`, `quitDriver()`, `captureOnFailure()`, `waitForVisible()`, `waitForClickable()`. |
| `ScreenshotUtil.java` | Lưu ảnh vào `screenshots/<testName>_<timestamp>.png`. |
| `pages/LoginPage.java` | Page Object: mọi thao tác trên trang login được đóng gói ở đây. |
| `tests/*Test.java` | Chứa các `@Test` — mỗi method = một test case (TC). |
| `testng.xml` | Suite TestNG: chọn trình duyệt (`parameter browser=chrome`), liệt kê class test. |
| `testdata.properties` | Đọc username/password để test không hard-code trong code. |

---

## 4. Chạy test bằng Maven

Di chuyển vào thư mục project trước:

```powershell
cd "D:\code\ky_I_nam_4\KiemThuPhanMem\webdriver-selenium"
```

### 4.1. Chạy tất cả test (mặc định Chrome)

```powershell
mvn clean test
```

> Lệnh này sẽ: xóa `target/` → compile main + test → Surefire đọc `src/test/resources/testng.xml` → chạy các class được liệt kê.

### 4.2. Chỉ chạy một class test

```powershell
mvn test -Dtest=LoginTest
mvn test -Dtest=GoogleSearchTest
```

### 4.3. Chỉ chạy một method trong một class

```powershell
mvn test -Dtest=LoginTest#tc_fn_01_loginSuccessWithValidAccount
```

### 4.4. Bỏ qua test (skip)

Trong `pom.xml` đã có cấu hình `<skipTests>true</skipTests>` mặc định cho surefire (`Tests are skipped.`). Nếu muốn chạy, **mở `pom.xml` và đổi thành `false`**, hoặc truyền:

```powershell
mvn test -DskipTests=false
```

### 4.5. Xem báo cáo TestNG

Sau khi chạy xong, mở:

```
target/surefire-reports/index.html
target/surefire-reports/emailable-report.html
```

---

## 5. Đổi trình duyệtệnh / chạy headless

### 5.1. Đổi trình duyệt — cách 1: sửa `testng.xml`

Mở `src/test/resources/testng.xml`:

```xml
<parameter name="browser" value="chrome"/>   <!-- đổi thành edge | firefox -->
```

### 5.2. Đổi trình duyệt — cách 2: truyền tham số Maven

```bash
mvn test -Dbrowser=edge
mvn test -Dbrowser=firefox
```

### 5.3. Chạy headless (không hiện cửa sổ trình duyệt)

```bash
mvn test -Dheadless=true
```

Áp dụng cho cả Chrome và Edge (`--headless=new`) và Firefox (`--headless`).

---

## 6. Viết một test case mới (walkthrough)

> Quy ước của workspace: **một test case = một commit**. Mỗi method `@Test` đều phải có **TC_ID** duy nhất (`TC01` → `TC99` → `TC100`...).
>
> Quy ước đặt tên method theo dự án hiện tại:
> - Nhóm **LoginTest**: `tc_fn_NN_shortDescription` (TC_FN_NN).
> - Nhóm khác: dùng `tc_NN_shortDescription` (TC_NN).

### 6.1. Bước 1 — Tìm TC_ID chưa dùng

```powershell
git log --oneline -n 200 | Select-String "test TC|add TC|update TC|fix TC"
```

Lấy số lớn nhất, ví dụ đã có `TC18` thì TC mới là `TC19`.

### 6.2. Bước 2 — Mở file test tương ứng

- Đang test login → mở `src/test/java/com/example/tests/LoginTest.java`.
- Test chức năng khác → tạo file mới trong cùng package `com.example.tests`.

### 6.3. Bước 3 — Thêm method `@Test`

Ví dụ, thêm test "đăng nhập với username có khoảng trắng đầu/cuối" vào `LoginTest.java`:

```java
/** TC19 - Đăng nhập với username có khoảng trắng ở đầu. */
@Test(description = "TC19 - Đăng nhập với username có khoảng trắng ở đầu")
public void tc_19_loginWithLeadingSpaceInUsername() {
    loginPage.typeUsername(" " + validUsername)
             .typePassword(validPassword)
             .clickLogin();

    // Trang web có thể trim rồi cho vào, hoặc báo lỗi — cả hai đều pass nếu không crash
    Assert.assertTrue(
        loginPage.isLoggedIn() || !loginPage.getLoginErrorMessage().isEmpty(),
        "Trang web phải phản hồi (đăng nhập hoặc báo lỗi). URL: " + loginPage.getCurrentUrl()
    );
}
```

### 6.4. Bước 4 — Compile & chạy thử

```powershell
mvn test -Dtest=LoginTest#tc_19_loginWithLeadingSpaceInUsername
```

Nếu pass → sẵn sàng commit.

### 6.5. Bước 5 — Commit theo quy tắc (xem mục 8).

---

## 7. Thêm Page Object mới

> Khi bạn cần test một **trang web khác** (không phải login), hãy tạo Page Object mới để gom tất cả locator + action.

### 7.1. Tạo file `pages/HomePage.java`

```java
package com.example.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/** Page Object cho trang chủ VPDT UTC. */
public class HomePage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // --- Locators ---
    private final By welcomeText = By.cssSelector(".welcome-message");
    private final By logoutLink   = By.id("logout");
    private final By avatarMenu  = By.cssSelector(".user-menu");

    public HomePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public HomePage open(String url) {
        driver.get(url);
        return this;
    }

    public boolean isAtHomePage() {
        return !driver.getCurrentUrl().toLowerCase().contains("/login");
    }

    public String getWelcomeText() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(welcomeText)).getText();
    }

    public LoginPage logout() {
        wait.until(ExpectedConditions.elementToBeClickable(logoutLink)).click();
        return new LoginPage(driver);
    }

    // ---- helpers ----
    private org.openqa.selenium.WebElement waitForVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }
}
```

### 7.2. Sử dụng trong test class

```java
public class HomeTest extends BaseTest {

    private HomePage homePage;

    @Parameters("browser")
    @BeforeMethod
    public void setUp(@Optional("chrome") String browser) {
        initDriver(browser);
        // Mở thẳng trang chủ (giả sử đã có session/cookie hợp lệ)
        homePage = new HomePage(driver).open("https://vanphongdientu.utc.edu.vn/Home");
    }

    @AfterMethod
    public void tearDown(ITestResult result) {
        if (!result.isSuccess()) captureOnFailure(result.getName());
        quitDriver();
    }

    @Test(description = "Hiển thị lời chào sau khi đăng nhập")
    public void shouldShowWelcomeAfterLogin() {
        Assert.assertFalse(homePage.getWelcomeText().isBlank(),
                "Lời chào phải hiển thị");
    }
}
```

### 7.3. Quy tắc khi viết Page Object

1. **Locator là private final**, đặt ở đầu class.
2. **Action trả về `this` (builder pattern)** nếu là chuỗi thao tác (`typeUsername().typePassword().clickLogin()`).
3. **Không có assertion** trong Page Object — chỉ test mới `assert`.
4. **Mỗi trang = một class**, đặt trong `com.example.pages`.
5. **Đặt method `waitForVisible` / `waitForClickable`** ở cuối class (private helper) — đừng lặp code wait ở nhiều nơi.

---

## 8. Quy trình commit & push (workspace rule)

> ⚠️ Đọc kỹ — đây là **workspace rule** bắt buộc, áp dụng cho mọi TC trong project này.

### 8.1. Format commit message

```
<type> <TC_ID> <short description>
```

| Type | Khi nào | Ví dụ |
| --- | --- | --- |
| `add` | Thêm test case / test file mới | `add TC03 login with wrong password` |
| `test` | Viết / hoàn thiện test case | `test TC03 login with wrong password` |
| `update` | Cập nhật test case đã có | `update TC03 login with wrong password` |
| `fix` | Sửa bug trong test case | `fix TC03 login with wrong password` |
| `delete` | Xóa test case | `delete TC03 login with wrong password` |

**Quy tắc cứng:**

- TC_ID có dạng `TC` + 2 chữ số: `TC01`, `TC02`, … `TC99`, `TC100`, `TC101`…
- **Mỗi TC giữ nguyên ID suốt vòng đời**: nếu `add TC03` thì `update TC03`, `fix TC03` sau này phải dùng đúng `TC03`.
- **Mô tả chữ thường**, không dấu chấm cuối, **tiếng Anh** (không trộn tiếng Việt).
- **Một TC = một commit** — không gộp nhiều TC trong một commit.

### 8.2. Workflow khi được nhờ commit

```powershell
# 1. Xem file nào đã thay đổi
git status

# 2. Stage ĐÚNG file test case đó (không dùng `git add .`)
git add src/test/java/com/example/tests/LoginTest.java

# 3. Commit với message đúng format
git commit -m "test TC03 login with wrong password"

# 4. DỪNG LẠI — hỏi user trước khi push
```

### 8.4. Push chỉ khi user đồng ý

```powershell
git push origin <current-branch>
```

> ❌ **Không bao giờ** push khi chưa được phép.
> ❌ **Không bao giờ** dùng `git add .` (có thể kéo theo file không liên quan).
> ❌ **Không bao giờ** commit nhiều TC trong một lần.

---

## 9. Khi test fail — xử lý ảnh chụp & log

### 9.1. Ảnh chụp tự động

`BaseTest.tearDown()` đã gọi `captureOnFailure(testName)` khi `result.isSuccess() == false`. Ảnh được lưu vào:

```
screenshots/<tenTestMethod>_<yyyyMMdd_HHmmss>.png
```

Ví dụ: `screenshots/tc_fn_01_loginSuccessWithValidAccount_20261008_153022.png`

### 9.2. Log TestNG

- Console log: xem trong terminal nơi bạn chạy `mvn test`.
- Report HTML: mở `target/surefire-reports/emailable-report.html`.
- Report XML: `target/surefire-reports/testng-results.xml` (dùng cho CI).

### 9.3. Quy trình khi test fail

1. Mở ảnh trong `screenshots/` xem UI lúc đó.
2. Đọc stack trace trong terminal/report.
3. Nếu là **bug app** → ghi nhận, không sửa test.
4. Nếu là **lỗi test** (locator sai, wait thiếu, data cũ…) → sửa test, commit với type `fix TCxx`.

---

## 10. Lỗi thường gặp & cách fix

### 10.1. `chromedriver` không khớp phiên bản Chrome

WebDriverManager tự xử lý, nhưng nếu bị lỗi:

```powershell
# Xóa cache driver cũ
Remove-Item -Recurse -Force "$env:USERPROFILE\.cache\selenium"
# Chạy lại
mvn test
```

### 10.2. `Tests are skipped.`

Trong `pom.xml` đang có `<skipTests>true</skipTests>`. Mở `pom.xml` và đổi thành `false`, hoặc:

```powershell
mvn test -DskipTests=false
```

### 10.3. Lỗi `location of system modules is not set`

Java đang build bằng `-source 21 -target 21`. Mở `pom.xml` và đổi thành `<release>21</release>`:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.13.0</version>
    <configuration>
        <release>21</release>
    </configuration>
</plugin>
```

### 10.4. Trang web thay đổi giao diện → test fail hàng loạt

1. Mở trang web thật bằng tay → DevTools (F12) → copy CSS selector mới.
2. Cập nhật locator trong `pages/LoginPage.java` (hoặc Page tương ứng).
3. Commit với type `fix TCxx` (đúng TC_ID mà bạn vừa sửa).

### 10.5. `TimeoutException: Expected visibility of ...`

- Element chưa render xong → kiểm tra lại locator (có thể selector sai do web đổi UI).
- Trang load chậm → tăng timeout trong `BaseTest` (đang là 15s) hoặc dùng `wait.until(...)` với điều kiện cụ thể hơn.

### 10.6. Test pass locally nhưng fail trên CI / máy khác

- Khác version trình duyệt → ép cùng version (Chrome ổn định nhất).
- Chạy **headless** trên CI: `mvn test -Dheadless=true`.

### 10.7. Quên điền `valid.username` / `valid.password`

Mở `src/test/resources/testdata.properties` và điền tài khoản thật. Các test "negative" (sai MK, không tồn tại, SQLi…) **không cần** tài khoản thật.

---

## Phụ lục A — Danh sách test case hiện có (LoginTest)

> Tài liệu nguồn: `TestCase_ChucNangDangNhap_VPDT_UTC.xlsx`. Tên trong code theo dạng `tc_fn_NN_*`.

| TC_ID | Mô tả | Method |
| --- | --- | --- |
| TC_FN_01 | Đăng nhập thành công với tài khoản hợp lệ | `tc_fn_01_loginSuccessWithValidAccount` |
| TC_FN_02 | Sai mật khẩu | `tc_fn_02_loginWithWrongPassword` |
| TC_FN_03 | Username không tồn tại | `tc_fn_03_loginWithNonExistingUsername` |
| TC_FN_04 | Để trống username | `tc_fn_04_loginWithEmptyUsername` |
| TC_FN_05 | Để trống password | `tc_fn_05_loginWithEmptyPassword` |
| TC_FN_06 | Tài khoản bị khóa | `tc_fn_06_loginWithLockedAccount` |
| TC_FN_07 → TC_FN_25 | … (xem `LoginTest.java` để biết chi tiết) | … |

## Phụ lục B — Câu lệnh nhanh dùng hằng ngày

```powershell
# Mở project
cd "D:\code\ky_I_nam_4\KiemThuPhanMem\webdriver-selenium"

# Chạy hết
mvn clean test

# Chạy Chrome headless
mvn test -Dheadless=true

# Chạy Edge
mvn test -Dbrowser=edge

# Chạy 1 TC
mvn test -Dtest=LoginTest#tc_fn_01_loginSuccessWithValidAccount

# Xem báo cáo
start target\surefire-reports\emailable-report.html

# Tìm TC_ID tiếp theo
git log --oneline -n 200 | Select-String -Pattern "TC\d+"
```

---

> 📌 **Nhớ**: một TC = một commit. Hỏi user trước khi push. Đọc rule ở mục 8 trước mỗi lần commit.