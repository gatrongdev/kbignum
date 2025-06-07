# Hướng dẫn thiết lập CI/CD Pipeline cho KBigNum

## 📋 Tổng quan

Quy trình CI/CD này được thiết kế để tự động thực hiện các kiểm tra chất lượng code, chạy test, và quét bảo mật cho dự án Kotlin Multiplatform KBigNum.

## 🚀 Các bước thiết lập

### 1. Đăng ký tài khoản và lấy token

#### 📊 Codecov (Test Coverage)
1. Truy cập [codecov.io](https://codecov.io)
2. Đăng nhập bằng tài khoản GitHub của bạn
3. Thêm repository KBigNum vào Codecov
4. Vào **Settings** > **General** của repository
5. Sao chép **Repository Upload Token**

### 2. Tạo GitHub Secrets

1. Vào repository GitHub của bạn
2. Click **Settings** > **Secrets and variables** > **Actions**
3. Click **New repository secret** và tạo hai secrets:

   **CODECOV_TOKEN**
   - Name: `CODECOV_TOKEN`
   - Secret: Paste token từ Codecov


### 3. Bật Dependabot Security Alerts

1. Vào **Settings** > **Code security and analysis**
2. Bật các tùy chọn sau:
   - ✅ **Dependency graph**
   - ✅ **Dependabot alerts**
   - ✅ **Dependabot security updates**

### 4. Cấu trúc files đã được tạo

```
.github/
  workflows/
    main.yml                    # GitHub Actions workflow
shared/
  config/
    detekt/
      detekt.yml               # Detekt configuration
      baseline.xml             # Detekt baseline
  build.gradle.kts             # Updated với plugins
build.gradle.kts               # Updated với plugins
```

## 🔧 Các plugin và công cụ được sử dụng

- **Kover** `0.8.3`: Test coverage analysis
- **ktlint** `12.1.1`: Code style formatting
- **detekt** `1.23.6`: Static code analysis  
- **Codecov**: Coverage reporting

## 📈 Quy trình CI/CD

Pipeline sẽ tự động chạy khi:
- Push code lên nhánh `main` hoặc `dev`
- Tạo Pull Request tới nhánh `main` hoặc `dev`

**Các bước thực hiện:**
1. ✅ Build và chạy tests
2. 📊 Tạo báo cáo test coverage
3. 📤 Upload coverage lên Codecov
4. 🎨 Kiểm tra code style với ktlint
5. 🔍 Phân tích static code với detekt
6. 🔒 Quét lỗ hổng bảo mật với Snyk

## 🛠️ Lệnh chạy thủ công

Bạn có thể chạy các checks này locally:

```bash
# Chạy tất cả checks
./gradlew runAllChecks

# Chạy từng check riêng lẻ
./gradlew test                # Tests
./gradlew koverXmlReport      # Coverage report
./gradlew ktlintCheck         # Code style
./gradlew detekt              # Static analysis
```

## 📝 Lưu ý quan trọng

- Pipeline sẽ **fail** nếu có lỗ hổng bảo mật với severity **HIGH** hoặc cao hơn
- Code coverage report sẽ được comment trực tiếp lên Pull Request
- Tất cả code phải pass qua ktlint và detekt checks
- Detekt baseline có thể được cập nhật nếu cần thiết

## 🔧 Tùy chỉnh

- **detekt.yml**: Chỉnh sửa rules cho static analysis
- **ktlint**: Có thể tùy chỉnh trong `shared/build.gradle.kts`

## 📞 Hỗ trợ

Nếu gặp vấn đề với pipeline, hãy kiểm tra:
1. GitHub Secrets đã được tạo đúng chưa
2. Tokens có còn hiệu lực không
3. Logs trong GitHub Actions tab

---

✅ **Pipeline đã sẵn sàng hoạt động sau khi hoàn thành các bước trên!**
