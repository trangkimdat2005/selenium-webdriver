# Selenium WebDriver Project

Dự án mẫu **Selenium WebDriver + TestNG** dùng Maven, Java 21, hỗ trợ Chrome / Edge / Firefox.

## Cấu trúc

```
webdriver-selenium/
├── pom.xml                              # Cấu hình Maven & dependencies
├── screenshots/                         # Ảnh chụp khi test fail
└── src/
    ├── main/java/com/example/
    │   ├── DriverFactory.java           # Factory tạo WebDriver (chrome/edge/firefox)
    │   ├── BaseTest.java                # Lớp cha cho test (setUp/tearDown + wait helper)
    │   └── ScreenshotUtil.java          # Chụp ảnh màn hình khi fail
    └── test/
        ├── java/com/example/tests/
        │   └── GoogleSearchTest.java    # Test mẫu: tìm kiếm trên Google
        └── resources/
            └── testng.xml               # Cấu hình suite TestNG
```

## Yêu cầu

- **Java 21+** (đã cài Java 25 LTS trên máy)
- **Maven 3.8+** *(hoặc dùng Maven Wrapper)*
- Trình duyệt: Chrome, Edge hoặc Firefox

## Cài Maven (nếu chưa có)

Chạy trong PowerShell với quyền Admin:
```powershell
winget install Apache.Maven
```
Sau đó mở terminal mới và kiểm tra: `mvn -version`

## Chạy test

```bash
# Chạy tất cả test
mvn clean test

# Chỉ chạy trên Chrome
mvn test -Dbrowser=chrome

# Chạy chế độ headless
mvn test -Dheadless=true

# Chạy 1 class cụ thể
mvn test -Dtest=GoogleSearchTest
```

## Đổi trình duyệt

Sửa file `src/test/resources/testng.xml` hoặc truyền tham số `-Dbrowser=edge|firefox|chrome`.

## Lưu ý

- **WebDriverManager** tự động tải driver phù hợp với phiên bản trình duyệt — không cần tải thủ công.
- Khi test thất bại, ảnh màn hình được lưu vào `screenshots/`.
- Java 25 đang cài trên máy có thể build target Java 21 (LTS) để tương thích rộng hơn.
