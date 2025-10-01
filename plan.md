# Kế hoạch Phát hành 0.0.18

## 1. Bối cảnh & Điểm xuất phát
- API số lớn đã hoàn thiện với lớp `expect/actual`, vận hành tốt trên Android/iOS và có bộ test chung dày đặc.
- Tooling build: Maven Publish, Dokka, Kover, ktlint, detekt, CI GitHub Actions sẵn sàng cho chu kỳ release ngắn.
- Bản 0.0.17 tập trung vào hệ thống chiến lược chia (`DivisionStrategy`, `PrecisionScale`), tạo nền tảng cho việc đồng bộ ứng xử đa nền tảng ở bản tiếp theo.

## 2. Vấn đề cần giải quyết
1. **Sai khác làm tròn giữa Android và iOS**: Mapping `NSDecimalNumberHandler` hiện không tương đương `RoundingMode` của JVM (đặc biệt với số âm), gây lệch kết quả.
2. **Hàm `divide` mặc định gây bất ngờ**: Trả về cố định `1.00` khi chia cho chính nó, mất scale gốc và làm behavior không nhất quán.
3. **KBigInteger iOS mất chính xác với số rất lớn**: Khi vượt quá 34 chữ số, code fallback qua `KBigDecimal` dẫn tới sai số trong chia/mod.
4. **Thông số Android SDK bị hard-code**: `compileSdk`, `minSdk`, `targetSdk` đặt trong `shared/build.gradle.kts`, buộc chỉnh tay mỗi lần nâng cấp.

## 3. Dòng việc chính
### 3.1 Đồng bộ làm tròn và scale
**Trạng thái:** ✅ Hoàn thành
- [x] Tạo `enum`/`sealed class` mô tả các chế độ làm tròn trong common module và cung cấp mapping rõ ràng xuống hai nền tảng.
- [x] Sắp xếp lại logic `divide`/`setScale` trên Android & iOS để dùng chung bảng luật, kèm test với số âm/dương.

### 3.2 Chuẩn hoá API chia
**Trạng thái:** ⏳ Chưa thực hiện
- [ ] Bỏ shortcut `1.00`, xây dựng quy tắc mặc định dựa trên `DivisionConfig` (ví dụ dùng scale lớn nhất của toán hạng hoặc một cấu hình toàn cục).
- [ ] Cập nhật toán tử `div` để nhận cấu hình hoặc cho phép người dùng đặt default strategy.
- [ ] Viết regression test khớp kết quả cross-platform với scale khác nhau và số rất lớn.

### 3.3 Nâng cấp số nguyên lớn iOS
**Trạng thái:** ⏳ Chưa thực hiện
- [ ] Phát triển bộ toán học dựa trên chuỗi/mảng (add/sub/mul/div/mod) cho trường hợp vượt quá `NSDecimalNumber`.
- [ ] Thêm test >34 chữ số và benchmark đơn giản để đảm bảo hiệu năng chấp nhận được.

### 3.4 Tập trung hoá cấu hình Android SDK
**Trạng thái:** ✅ Hoàn thành
- [x] Di chuyển giá trị `compileSdk`, `minSdk`, `targetSdk` lên cấu hình cha (`gradle.properties` hoặc `libs.versions.toml`).
- [x] Trong `shared/build.gradle.kts`, đọc các thuộc tính này để module tự đồng bộ khi project cha đổi SDK.
- [x] Ghi chú hướng dẫn override trong README/CHANGELOG.

## 4. Đầu ra mong muốn
- ✅ Test rounding & division chạy qua JVM và iOS simulator cho kết quả như nhau.
- ⏳ Bộ toán học KBigInteger mới trên iOS với kiểm thử chuyên sâu.
- ✅ Build script đọc SDK từ cấu hình trung tâm, không còn số hard-code.
- ✅ Tài liệu và changelog cập nhật, mô tả hành vi mới và hướng dẫn cấu hình.

## 5. Rủi ro & Giảm thiểu
- **Giảm hiệu năng** trong thuật toán chuỗi: thêm benchmark và tối ưu (chia nhị phân, Karatsuba nếu cần).
- **Thay đổi hành vi chia** có thể breaking: cung cấp flag chuyển tiếp và ghi rõ trong changelog.
- **Dự án downstream muốn SDK khác**: cho phép override qua Gradle property, tài liệu hoá rõ ràng.

## 6. Bước tiếp theo đề xuất
1. ~~Thiết kế abstraction làm tròn mới và mapping chi tiết.~~ ✅ Hoàn thành trong 0.0.18.
2. ~~Thiết lập test chéo cho `divide`/`setScale` (bao gồm số âm).~~ ✅ Đã thêm test mới cho rounding và đảm bảo pass trên JVM/iOS.
3. Triển khai arithmetic chuỗi trên iOS và benchmarking tối thiểu. ⏳
4. ~~Refactor build script đọc SDK từ cấu hình cha, cập nhật README/CHANGELOG.~~ ✅ Đã cập nhật `gradle.properties`, `shared/build.gradle.kts`, README và CHANGELOG.
