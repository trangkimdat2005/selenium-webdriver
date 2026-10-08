# Selenium WebDriver Project

Dự án mẫu **Selenium WebDriver + TestNG** dùng Maven, Java 21, hỗ trợ Chrome / Edge / Firefox.
Đối tượng test chính: chức năng **đăng nhập Văn phòng điện tử UTC** (`https://vanphongdientu.utc.edu.vn/Login`).

> Tài liệu chi tiết: [`docs/HD.md`](docs/HD.md) — hướng dẫn cài đặt, viết test, commit & push, xử lý khi fail.
> File test case nguồn: `docs/TestCase_ChucNangDangNhap_VPDT_UTC.xlsx`.

## Tech stack

| Thành phần | Phiên bản |
| --- | --- |
| Java | 21 (target) — build được trên Java 25 LTS |
| Build tool | Maven 3.9.16 (`D:\tools\apache-maven-3.9.16\`) |
| Test framework | TestNG 7.10.x |
| Automation | Selenium WebDriver 4.25.x |
| Driver manager | WebDriverManager 5.9.x (tự động tải driver) |
| Assertion | TestNG `Assert` |

## Cấu trúc thư mục

```
webdriver-selenium/
├── pom.xml                              # Cấu hình Maven & dependencies
├── README.md                            # File này — tóm tắt ngắn
├── docs/
│   ├── HD.md                            # Hướng dẫn chi tiết (đọc file này)
│   └── TestCase_ChucNangDangNhap_VPDT_UTC.xlsx   # File test case nguồn
├── screenshots/                         # Ảnh chụp khi test fail
└── src/
    ├── main/java/com/example/
    │   ├── DriverFactory.java           # Factory tạo WebDriver (chrome/edge/firefox)
    │   ├── BaseTest.java                # Lớp cha cho test (setUp/tearDown + wait helper)
    │   ├── ScreenshotUtil.java          # Chụp ảnh màn hình khi fail
    │   └── pages/
    │       └── LoginPage.java           # Page Object cho trang đăng nhập VPDT UTC
    └── test/
        ├── java/com/example/tests/
        │   ├── GoogleSearchTest.java    # Test mẫu: tìm kiếm trên Google
        │   └── LoginTest.java           # 26 TC đăng nhập VPDT UTC (FN + VAL + REM)
        └── resources/
            ├── testng.xml               # Cấu hình suite TestNG (browser mặc định)
            ├── testdata.properties      # Tài khoản test (valid / locked)
            └── mock-login.html          # Mock trang login để test offline
```

## Yêu cầu môi trường

- **Java 21+** (đã cài Java 25 LTS trên máy).
- **Maven 3.8+** — cài tại `D:\tools\apache-maven-3.9.16\` (đã thêm vào PATH user).
- **Trình duyệt**: Chrome (khuyến nghị, cài bằng winget), hoặc Edge / Firefox.

> ✅ **WebDriverManager** tự động tải `chromedriver` / `msedgedriver` / `geckodriver` đúng phiên bản trình duyệt — **không cần tải thủ công**.

## Cài đặt nhanh (PowerShell)

```powershell
# Kiểm tra môi trường
java -version
mvn -version

# Cài Chrome (nếu chưa có)
winget install --id Google.Chrome -e --source winget
```

Cài Maven thủ công (nếu máy khác):

```powershell
Invoke-WebRequest -Uri "https://mirrors.cloud.tencent.com/apache/maven/maven-3/3.9.16/binaries/apache-maven-3.9.16-bin.zip" -OutFile "$env:TEMP\maven.zip"
Expand-Archive -Path "$env:TEMP\maven.zip" -DestinationPath "D:\tools"
# Thêm D:\tools\apache-maven-3.9.16\bin vào PATH user
```

## Chạy test

```powershell
# Chạy tất cả test (mặc định Chrome, đọc suite từ testng.xml)
mvn clean test

# Chỉ chạy LoginTest
mvn test -Dtest=LoginTest

# Chỉ chạy 1 method cụ thể
mvn test -Dtest=LoginTest#tc_fn_01_loginSuccessWithValidAccount

# Đổi trình duyệt
mvn test -Dbrowser=edge
mvn test -Dbrowser=firefox

# Chạy headless (không hiện cửa sổ)
mvn test -Dheadless=true
```

## Cấu hình tài khoản test

Mở `src/test/resources/testdata.properties` và thay các giá trị mặc định:

```properties
login.url=https://vanphongdientu.utc.edu.vn/Login

valid.username=huongnt
valid.password=123456@utc

locked.username=<locked_username>
locked.password=<locked_password>
```

> Các test "negative" (sai MK, username không tồn tại, SQLi…) **không cần** tài khoản thật.

## Danh sách test case (LoginTest)

> Tài liệu gốc: `docs/TestCase_ChucNangDangNhap_VPDT_UTC.xlsx`. ID commit dùng `TC01..TC26` (mapping theo `git log`).

| ID commit | Method | Mô tả |
| --- | --- | --- |
| TC01 | (pom + testdata) | Setup framework + testdata (commit gốc) |
| TC02 | `tc_fn_01_loginSuccessWithValidAccount` | Đăng nhập thành công |
| TC03 | `tc_fn_02_loginWithEnterKey` | Submit bằng phím Enter |
| TC04 | `tc_fn_03_redirectByParamR` | Redirect theo tham số `r` |
| TC05 | `tc_fn_04_loginWithWrongPassword` | Sai mật khẩu |
| TC06 | `tc_fn_05_loginWithUnknownUsername` | Username không tồn tại |
| TC07 | `tc_fn_06_lockedAccount` | Tài khoản bị khóa |
| TC08 | `tc_fn_07_passwordIsCaseSensitive` | Mật khẩu phân biệt hoa thường |
| TC09 | `tc_fn_08_usernameWithLeadingTrailingSpaces` | Username có khoảng trắng đầu/cuối |
| TC10 | `tc_fn_09_usernameIsCaseInsensitive` | Username không phân biệt hoa thường |
| TC11 | `tc_fn_10_alreadyLoggedIn` | Đã đăng nhập thì vào lại `/Login` |
| TC12 | `tc_fn_11_protectInternalPages` | Truy cập trang nội bộ khi chưa login |
| TC13 | `tc_fn_12_doubleClickLogin` | Double-click nút Đăng nhập |
| TC14 | `tc_fn_13_sqlInjectionUsername` | Chống SQLi ở Username |
| TC15 | `tc_fn_14_sqlInjectionPassword` | Chống SQLi ở Password |
| TC16 | `tc_fn_15_lockAfterMultipleFailures` | Khóa tạm sau nhiều lần sai |
| TC17 | `tc_val_01_bothFieldsEmpty` | Bỏ trống cả 2 trường |
| TC18 | `tc_val_02_emptyUsername` | Bỏ trống Username |
| TC19 | `tc_val_03_emptyPassword` | Bỏ trống Password (giữ lại username) |
| TC20 | `tc_val_04_whitespaceOnly` | Chỉ nhập khoảng trắng |
| TC21 | `tc_val_05_veryLongString` | Chuỗi 1000 ký tự |
| TC22 | `tc_val_06_specialCharsUnicode` | Ký tự đặc biệt / Unicode / emoji |
| TC23 | `tc_val_07_keepUsernameAfterFailure` | Giữ lại username sau login thất bại |
| TC24 | `tc_rem_01_rememberMeLogin` | Tick "Giữ đăng nhập" |
| TC25 | `tc_rem_02_noRememberMeLogin` | Không tick "Giữ đăng nhập" |
| TC26 | `tc_rem_03_logoutAfterRememberMe` | Đăng xuất khi đang bật "Giữ đăng nhập" |

## Khi test fail

- Ảnh chụp màn hình **tự động** lưu vào `screenshots/<testName>_<yyyyMMdd_HHmmss>.png`.
- Báo cáo TestNG HTML: mở `target/surefire-reports/emailable-report.html`.
- Stack trace chi tiết: xem trong terminal lúc chạy `mvn test`.

## Quy trình commit (tóm tắt)

> ⚠️ Đọc chi tiết tại [`docs/HD.md`](docs/HD.md) mục 8.

1. **Một test case = một commit.**
2. Format: `<type> <TC_ID> <mô tả chữ thường, tiếng Anh, không dấu chấm>`
   - Ví dụ: `test TC03 login with enter key on password field`
3. Stage đúng file: `git add src/test/java/com/example/tests/LoginTest.java` — **không** dùng `git add .`.
4. **Hỏi user trước khi push** — không tự push.

## Tài liệu liên quan

- [`docs/HD.md`](docs/HD.md) — Hướng dẫn chi tiết (cài đặt, viết test, commit rule, xử lý lỗi).
- `docs/TestCase_ChucNangDangNhap_VPDT_UTC.xlsx` — File test case nguồn.
