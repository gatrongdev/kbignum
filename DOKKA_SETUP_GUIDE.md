# Hướng Dẫn Toàn Diện: Tạo và Triển Khai Tài Liệu cho Thư Viện Kotlin Multiplatform với Dokka

## 1. Giới thiệu và Xác nhận

**Dokka** là công cụ chính thức và phù hợp nhất cho việc tạo tài liệu cho dự án Kotlin Multiplatform. Dokka được phát triển bởi JetBrains và có khả năng:

- ✅ **Hỗ trợ đầy đủ Kotlin Multiplatform**: Tự động phát hiện và xử lý mã nguồn chung (common code) cùng với các implementation riêng cho từng nền tảng
- ✅ **Tạo tài liệu thống nhất**: Hợp nhất tài liệu từ `commonMain`, `androidMain`, `iosMain` thành một API reference duy nhất
- ✅ **Triển khai tự động**: Có thể tích hợp với CI/CD để tự động tạo và triển khai trang web tài liệu
- ✅ **Customizable**: Hỗ trợ tùy chỉnh giao diện, logo, CSS và liên kết

## 2. Cấu hình Dokka trong Gradle

### 2.1 Thêm Plugin Dokka

Cập nhật file `shared/build.gradle.kts`:

```kotlin
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    id("com.vanniktech.maven.publish") version "0.30.0"
    
    // Code quality plugins
    id("org.jetbrains.kotlinx.kover")
    id("org.jlleitschuh.gradle.ktlint")
    id("io.gitlab.arturbosch.detekt")
    
    // Thêm Dokka plugin
    id("org.jetbrains.dokka") version "1.9.20"
}
```

### 2.2 Cấu hình Chi Tiết Dokka

Thêm cấu hình Dokka vào cuối file `shared/build.gradle.kts`:

```kotlin
// ==================== Dokka Documentation Configuration ====================

tasks.withType<org.jetbrains.dokka.gradle.DokkaTask>().configureEach {
    moduleName.set("KBigNum")
    moduleVersion.set(version.toString())
    
    dokkaSourceSets {
        named("commonMain") {
            displayName.set("Common")
            platform.set(org.jetbrains.dokka.Platform.common)
            
            // Liên kết đến mã nguồn GitHub
            sourceLink {
                localDirectory.set(file("src/commonMain/kotlin"))
                remoteUrl.set(java.net.URL("https://github.com/gatrongdev/kbignum/tree/main/shared/src/commonMain/kotlin"))
                remoteLineSuffix.set("#L")
            }
            
            // External documentation links
            externalDocumentationLink {
                url.set(java.net.URL("https://kotlinlang.org/api/latest/jvm/stdlib/"))
            }
            
            // Include samples
            samples.from("src/commonTest/kotlin")
        }
        
        named("androidMain") {
            displayName.set("Android")
            platform.set(org.jetbrains.dokka.Platform.jvm)
            
            sourceLink {
                localDirectory.set(file("src/androidMain/kotlin"))
                remoteUrl.set(java.net.URL("https://github.com/gatrongdev/kbignum/tree/main/shared/src/androidMain/kotlin"))
                remoteLineSuffix.set("#L")
            }
            
            externalDocumentationLink {
                url.set(java.net.URL("https://developer.android.com/reference/"))
            }
        }
        
        named("iosMain") {
            displayName.set("iOS")
            platform.set(org.jetbrains.dokka.Platform.native)
            
            sourceLink {
                localDirectory.set(file("src/iosMain/kotlin"))
                remoteUrl.set(java.net.URL("https://github.com/gatrongdev/kbignum/tree/main/shared/src/iosMain/kotlin"))
                remoteLineSuffix.set("#L")
            }
        }
    }
    
    // Tùy chỉnh giao diện
    pluginsMapConfiguration.set(mapOf(
        "org.jetbrains.dokka.base.DokkaBase" to """
        {
            "customAssets": ["${file("docs/assets/logo.png")}"],
            "customStyleSheets": ["${file("docs/assets/custom.css")}"],
            "footerMessage": "© 2025 Gatrong Dev - KBigNum Library"
        }
        """
    ))
}

// Tác vụ tùy chỉnh để tạo tài liệu
tasks.register("generateDocs") {
    group = "documentation"
    description = "Generate complete documentation for KBigNum library"
    dependsOn("dokkaHtml")
    
    doLast {
        println("📚 Documentation generated successfully!")
        println("📁 Location: ${project.buildDir}/dokka/html/index.html")
        println("🌐 Ready for deployment to GitHub Pages")
    }
}
```

### 2.3 Cập nhật Root Build File

Cập nhật file `build.gradle.kts` ở root để thêm Dokka:

```kotlin
plugins {
    //trick: for the same plugin versions in all sub-modules
    alias(libs.plugins.androidLibrary).apply(false)
    alias(libs.plugins.kotlinMultiplatform).apply(false)
    id("com.vanniktech.maven.publish") version "0.30.0"
    
    // Code quality and security plugins
    id("org.jetbrains.kotlinx.kover") version "0.8.3" apply false
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1" apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.6" apply false
    
    // Dokka plugin for documentation
    id("org.jetbrains.dokka") version "1.9.20"
}
```

## 3. Tự động Triển khai với GitHub Actions

### 3.1 Tạo Workflow File

Tạo file `.github/workflows/docs.yml`:

```yaml
name: 📚 Generate and Deploy Documentation

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

permissions:
  contents: read
  pages: write
  id-token: write

concurrency:
  group: "pages"
  cancel-in-progress: false

jobs:
  build-docs:
    name: 🔨 Build Documentation
    runs-on: ubuntu-latest
    
    steps:
    - name: 📥 Checkout Repository
      uses: actions/checkout@v4
      
    - name: ☕ Set up JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
        
    - name: 📦 Cache Gradle packages
      uses: actions/cache@v3
      with:
        path: |
          ~/.gradle/caches
          ~/.gradle/wrapper
        key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
        restore-keys: |
          ${{ runner.os }}-gradle-
          
    - name: 🔧 Grant execute permission for gradlew
      run: chmod +x gradlew
      
    - name: 🏗️ Build Project
      run: ./gradlew build
      
    - name: 📚 Generate Documentation
      run: ./gradlew dokkaHtml
      
    - name: 📤 Upload Documentation Artifact
      uses: actions/upload-pages-artifact@v3
      with:
        path: './build/dokka/html'

  deploy-docs:
    name: 🚀 Deploy to GitHub Pages
    environment:
      name: github-pages
      url: ${{ steps.deployment.outputs.page_url }}
    runs-on: ubuntu-latest
    needs: build-docs
    if: github.ref == 'refs/heads/main'
    
    steps:
    - name: 🌐 Deploy to GitHub Pages
      id: deployment
      uses: actions/deploy-pages@v4

  notify:
    name: 📢 Notify Success
    runs-on: ubuntu-latest
    needs: [build-docs, deploy-docs]
    if: success() && github.ref == 'refs/heads/main'
    
    steps:
    - name: ✅ Documentation Deployed
      run: |
        echo "🎉 Documentation has been successfully deployed!"
        echo "📖 View at: https://gatrongdev.github.io/kbignum/"
```

### 3.2 Kích hoạt GitHub Pages

1. Vào repository Settings
2. Chọn **Pages** từ sidebar
3. Trong **Source**, chọn **GitHub Actions**
4. Save settings

## 4. Giải thích cách Dokka xử lý mã nguồn chung (Common Code)

Dokka có khả năng đặc biệt khi làm việc với Kotlin Multiplatform:

### 4.1 Đặc điểm chính:

- **Tự động phát hiện Common Code**: Dokka tự động quét `commonMain` và tạo tài liệu chính từ các interface và class chung
- **Hợp nhất Platform Implementations**: Tài liệu từ `androidMain` và `iosMain` được hợp nhất vào API reference chung
- **Tham chiếu chéo thông minh**: Tạo liên kết giữa expect/actual declarations
- **Giao diện API thống nhất**: Hiển thị một API duy nhất với ghi chú về platform-specific implementations

### 4.2 Cách hoạt động:

1. **Phân tích Common Source Sets**: Dokka đầu tiên phân tích `commonMain` để hiểu cấu trúc API chung
2. **Mapping Platform Implementations**: Sau đó mapping các `actual` implementations từ `androidMain` và `iosMain`
3. **Tạo Documentation Tree**: Tổ chức thành cây tài liệu với common API làm gốc
4. **Generate Unified Output**: Tạo HTML output thống nhất với platform annotations

## 5. Ví dụ về KDoc trong Common Code

### 5.1 Interface KBigDecimal với KDoc hoàn chỉnh:

```kotlin
package io.github.gatrongdev.kbignum.math.math

/**
 * Interface for arbitrary precision decimal numbers in Kotlin Multiplatform.
 * 
 * KBigDecimal provides cross-platform arithmetic operations for decimal numbers 
 * with unlimited precision. This interface abstracts platform-specific implementations
 * while maintaining a consistent API across Android and iOS.
 * 
 * ## Platform Implementations:
 * - **Android**: Uses Java's `java.math.BigDecimal` for optimal JVM performance
 * - **iOS**: Uses Foundation's `NSDecimalNumber` for native iOS integration
 * 
 * ## Thread Safety:
 * All implementations are immutable and thread-safe.
 * 
 * @sample io.github.gatrongdev.kbignum.samples.KBigDecimalSamples.basicArithmetic
 * @sample io.github.gatrongdev.kbignum.samples.KBigDecimalSamples.precisionControl
 * 
 * @see KBigInteger for integer-only operations
 * @see KBigMath for advanced mathematical functions
 * @see RoundingMode for available rounding modes
 * 
 * @author Gatrong Dev
 * @since 1.0.0
 */
interface KBigDecimal : Comparable<KBigDecimal> {
    
    /**
     * Adds another KBigDecimal to this value.
     * 
     * @param other the KBigDecimal to add to this value
     * @return a new KBigDecimal representing the sum
     * @sample io.github.gatrongdev.kbignum.samples.KBigDecimalSamples.addition
     */
    fun add(other: KBigDecimal): KBigDecimal

    /**
     * Subtracts another KBigDecimal from this value.
     * 
     * @param other the KBigDecimal to subtract from this value
     * @return a new KBigDecimal representing the difference
     * @sample io.github.gatrongdev.kbignum.samples.KBigDecimalSamples.subtraction
     */
    fun subtract(other: KBigDecimal): KBigDecimal

    /**
     * Multiplies this value by another KBigDecimal.
     * 
     * @param other the KBigDecimal to multiply with this value
     * @return a new KBigDecimal representing the product
     * @sample io.github.gatrongdev.kbignum.samples.KBigDecimalSamples.multiplication
     */
    fun multiply(other: KBigDecimal): KBigDecimal

    /**
     * Divides this value by another KBigDecimal with specified scale.
     * 
     * @param other the KBigDecimal to divide this value by
     * @param scale the number of decimal places in the result
     * @return a new KBigDecimal representing the quotient
     * @throws ArithmeticException if other is zero
     * @sample io.github.gatrongdev.kbignum.samples.KBigDecimalSamples.division
     */
    fun divide(other: KBigDecimal, scale: Int): KBigDecimal

    /**
     * Divides this value by another KBigDecimal with specified scale and rounding mode.
     * 
     * @param other the KBigDecimal to divide this value by
     * @param scale the number of decimal places in the result
     * @param mode the rounding mode to apply (see [RoundingMode])
     * @return a new KBigDecimal representing the quotient
     * @throws ArithmeticException if other is zero
     * @see RoundingMode
     * @sample io.github.gatrongdev.kbignum.samples.KBigDecimalSamples.divisionWithRounding
     */
    fun divide(other: KBigDecimal, scale: Int, mode: Int): KBigDecimal

    /**
     * Returns the absolute value of this KBigDecimal.
     * 
     * @return a new KBigDecimal with the absolute value
     * @sample io.github.gatrongdev.kbignum.samples.KBigDecimalSamples.absoluteValue
     */
    fun abs(): KBigDecimal

    /**
     * Returns the signum function of this KBigDecimal.
     * 
     * @return -1, 0, or 1 as this value is negative, zero, or positive respectively
     * @sample io.github.gatrongdev.kbignum.samples.KBigDecimalSamples.signumFunction
     */
    fun signum(): Int

    /**
     * Sets the scale of this KBigDecimal with the specified rounding mode.
     * 
     * @param scale the new scale for the result
     * @param roundingMode the rounding mode to apply
     * @return a new KBigDecimal with the specified scale
     * @see RoundingMode
     * @sample io.github.gatrongdev.kbignum.samples.KBigDecimalSamples.scaleAdjustment
     */
    fun setScale(scale: Int, roundingMode: Int): KBigDecimal

    /**
     * Converts this KBigDecimal to a KBigInteger by discarding the fractional part.
     * 
     * @return a new KBigInteger representing the integer part
     * @sample io.github.gatrongdev.kbignum.samples.KBigDecimalSamples.integerConversion
     */
    fun toBigInteger(): KBigInteger

    /**
     * Returns the string representation of this KBigDecimal.
     * 
     * @return string representation without unnecessary trailing zeros
     */
    override fun toString(): String

    /**
     * Returns the raw string representation of this KBigDecimal.
     * 
     * @return exact string representation including all digits
     */
    fun getString(): String

    // Additional utility functions with default implementations
    
    /**
     * Returns the negation of this KBigDecimal.
     * 
     * @return a new KBigDecimal with the opposite sign
     */
    fun negate(): KBigDecimal = KBigDecimalFactory.ZERO.subtract(this)
    
    /**
     * Returns the scale of this KBigDecimal.
     * 
     * @return the number of digits to the right of the decimal point
     */
    fun scale(): Int = 0 // Default implementation, overridden in actual implementations
    
    /**
     * Returns the precision of this KBigDecimal.
     * 
     * @return the total number of significant digits
     */
    fun precision(): Int = toString().replace(".", "").replace("-", "").length
    
    /**
     * Checks if this KBigDecimal is zero.
     * 
     * @return true if this value is zero, false otherwise
     */
    fun isZero(): Boolean = signum() == 0
    
    /**
     * Checks if this KBigDecimal is positive.
     * 
     * @return true if this value is greater than zero, false otherwise
     */
    fun isPositive(): Boolean = signum() > 0
    
    /**
     * Checks if this KBigDecimal is negative.
     * 
     * @return true if this value is less than zero, false otherwise
     */
    fun isNegative(): Boolean = signum() < 0
}
```

### 5.2 Object KBigMath với KDoc chi tiết:

```kotlin
/**
 * Mathematical utility functions for KBigDecimal and KBigInteger.
 * 
 * This object provides advanced mathematical operations that are not available
 * in the basic arithmetic interfaces. All functions are implemented using
 * platform-agnostic algorithms for consistent behavior across Android and iOS.
 * 
 * ## Performance Considerations:
 * - All operations are optimized for accuracy over speed
 * - Large number operations may take significant time
 * - Consider using background threads for intensive calculations
 * 
 * @author Gatrong Dev
 * @since 1.0.0
 */
object KBigMath {
    
    /**
     * Calculate square root of a KBigDecimal using Newton's method.
     * 
     * Uses iterative Newton-Raphson algorithm for high precision calculation.
     * The algorithm converges quadratically for most inputs.
     * 
     * @param value the KBigDecimal to calculate square root of
     * @param scale the number of decimal places in the result (default: 10)
     * @return square root of the input value
     * @throws IllegalArgumentException if value is negative
     * 
     * @sample io.github.gatrongdev.kbignum.samples.KBigMathSamples.squareRoot
     */
    fun sqrt(value: KBigDecimal, scale: Int = 10): KBigDecimal
    
    /**
     * Calculate factorial of a KBigInteger (n!).
     * 
     * Computes n! = n × (n-1) × (n-2) × ... × 2 × 1 for non-negative integers.
     * 
     * @param n the KBigInteger to calculate factorial of
     * @return factorial of the input value
     * @throws IllegalArgumentException if n is negative
     * 
     * @sample io.github.gatrongdev.kbignum.samples.KBigMathSamples.factorial
     */
    fun factorial(n: KBigInteger): KBigInteger
    
    /**
     * Calculate greatest common divisor (GCD) using Euclidean algorithm.
     * 
     * @param a first KBigInteger
     * @param b second KBigInteger  
     * @return the greatest common divisor of a and b
     * 
     * @sample io.github.gatrongdev.kbignum.samples.KBigMathSamples.gcd
     */
    fun gcd(a: KBigInteger, b: KBigInteger): KBigInteger
    
    /**
     * Calculate least common multiple (LCM).
     * 
     * Uses the identity: LCM(a,b) = |a×b| / GCD(a,b)
     * 
     * @param a first KBigInteger
     * @param b second KBigInteger
     * @return the least common multiple of a and b
     * 
     * @sample io.github.gatrongdev.kbignum.samples.KBigMathSamples.lcm
     */
    fun lcm(a: KBigInteger, b: KBigInteger): KBigInteger
    
    /**
     * Check if a KBigInteger is prime using basic trial division.
     * 
     * **Note**: This is a basic implementation suitable for educational purposes.
     * For production use with very large numbers, consider more advanced algorithms
     * like Miller-Rabin primality test.
     * 
     * @param n the KBigInteger to test for primality
     * @return true if n is prime, false otherwise
     * 
     * @sample io.github.gatrongdev.kbignum.samples.KBigMathSamples.primeCheck
     */
    fun isPrime(n: KBigInteger): Boolean
}
```

## 6. Cấu trúc thư mục đề xuất

Tạo cấu trúc thư mục để chứa tài nguyên tùy chỉnh:

```
KBigNum/
├── docs/
│   ├── assets/
│   │   ├── logo.png                 # Logo cho documentation
│   │   └── custom.css              # CSS tùy chỉnh
│   ├── samples/
│   │   ├── KBigDecimalSamples.kt   # Code samples cho KBigDecimal
│   │   ├── KBigIntegerSamples.kt   # Code samples cho KBigInteger
│   │   └── KBigMathSamples.kt      # Code samples cho KBigMath
│   └── templates/
│       └── custom-template.html    # HTML template tùy chỉnh
├── shared/
│   └── src/
│       ├── commonMain/
│       ├── commonTest/
│       ├── androidMain/
│       └── iosMain/
└── .github/
    └── workflows/
        └── docs.yml                # GitHub Actions workflow
```

### 6.1 Tạo CSS tùy chỉnh

Tạo file `docs/assets/custom.css`:

```css
/* KBigNum Documentation Custom Styles */

:root {
    --primary-color: #6366f1;
    --secondary-color: #8b5cf6;
    --accent-color: #10b981;
    --background-color: #f8fafc;
    --text-color: #334155;
}

.library-header {
    background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
    color: white;
    padding: 2rem;
    border-radius: 8px;
    margin-bottom: 2rem;
}

.platform-badge {
    display: inline-block;
    padding: 0.25rem 0.75rem;
    border-radius: 0.375rem;
    font-size: 0.875rem;
    font-weight: 600;
    margin-right: 0.5rem;
}

.platform-android {
    background-color: #22c55e;
    color: white;
}

.platform-ios {
    background-color: #3b82f6;
    color: white;
}

.platform-common {
    background-color: var(--accent-color);
    color: white;
}

/* Code highlighting improvements */
pre code {
    background-color: #1e293b;
    color: #e2e8f0;
    border-radius: 6px;
    padding: 1rem;
}

.sample-code {
    border-left: 4px solid var(--primary-color);
    background-color: var(--background-color);
    padding: 1rem;
    margin: 1rem 0;
}
```

### 6.2 Tạo Sample Code

Tạo file `docs/samples/KBigDecimalSamples.kt`:

```kotlin
package io.github.gatrongdev.kbignum.samples

import io.github.gatrongdev.kbignum.math.math.*

/**
 * Code samples for KBigDecimal documentation.
 */
class KBigDecimalSamples {
    
    fun basicArithmetic() {
        val a = "123.456".toKBigDecimal()
        val b = "789.123".toKBigDecimal()
        
        val sum = a + b              // 912.579
        val difference = a - b       // -665.667
        val product = a * b          // 97408.723088
        val quotient = a.divide(b, 4) // 0.1564
    }
    
    fun precisionControl() {
        val value = "123.456789012345".toKBigDecimal()
        
        // Set scale with different rounding modes
        val rounded = value.setScale(2, RoundingMode.HALF_UP) // 123.46
        val precise = value.setScale(6, RoundingMode.HALF_EVEN) // 123.456789
    }
    
    fun addition() {
        val price1 = "19.99".toKBigDecimal()
        val price2 = "25.50".toKBigDecimal()
        val total = price1 + price2 // 45.49
    }
    
    fun subtraction() {
        val balance = "100.00".toKBigDecimal()
        val withdrawal = "75.25".toKBigDecimal()
        val remaining = balance - withdrawal // 24.75
    }
    
    fun multiplication() {
        val rate = "0.0825".toKBigDecimal()  // 8.25%
        val amount = "1000.00".toKBigDecimal()
        val tax = amount * rate // 82.50
    }
    
    fun division() {
        val total = "100.00".toKBigDecimal()
        val parts = "3".toKBigDecimal()
        val each = total.divide(parts, 2) // 33.33
    }
    
    fun divisionWithRounding() {
        val dividend = "10.00".toKBigDecimal()
        val divisor = "3.00".toKBigDecimal()
        
        val result = dividend.divide(divisor, 4, RoundingMode.HALF_UP) // 3.3333
    }
    
    fun absoluteValue() {
        val negative = "-123.45".toKBigDecimal()
        val positive = negative.abs() // 123.45
    }
    
    fun signumFunction() {
        val positive = "123.45".toKBigDecimal()
        val negative = "-67.89".toKBigDecimal()
        val zero = "0.00".toKBigDecimal()
        
        positive.signum() // 1
        negative.signum() // -1
        zero.signum()     // 0
    }
    
    fun scaleAdjustment() {
        val value = "123.4".toKBigDecimal()
        val scaled = value.setScale(4, RoundingMode.HALF_UP) // 123.4000
    }
    
    fun integerConversion() {
        val decimal = "123.789".toKBigDecimal()
        val integer = decimal.toBigInteger() // 123
    }
}
```

## 7. Các lệnh Gradle hữu ích

### 7.1 Lệnh cơ bản:

```bash
# Tạo documentation HTML
./gradlew dokkaHtml

# Tạo documentation cho multimodule project
./gradlew dokkaHtmlMultiModule

# Tạo documentation partial (cho từng module riêng)
./gradlew dokkaHtmlPartial

# Tạo documentation với custom task
./gradlew generateDocs

# Xem preview documentation locally
./gradlew dokkaHtml && open build/dokka/html/index.html
```

### 7.2 Lệnh nâng cao:

```bash
# Clean và rebuild documentation
./gradlew clean dokkaHtml

# Tạo documentation với verbose output
./gradlew dokkaHtml --info

# Chạy với parallel processing
./gradlew dokkaHtml --parallel

# Tạo documentation và chạy tests
./gradlew test dokkaHtml

# Kiểm tra format và tạo documentation
./gradlew ktlintCheck detekt dokkaHtml
```

### 7.3 Automation script

Tạo script `scripts/build-docs.sh`:

```bash
#!/bin/bash

echo "🚀 Starting KBigNum Documentation Build..."

# Clean previous builds
echo "🧹 Cleaning previous builds..."
./gradlew clean

# Run code quality checks
echo "🔍 Running code quality checks..."
./gradlew ktlintCheck detekt

# Run tests
echo "🧪 Running tests..."
./gradlew test

# Generate documentation
echo "📚 Generating documentation..."
./gradlew dokkaHtml

# Check if documentation was generated successfully
if [ -f "build/dokka/html/index.html" ]; then
    echo "✅ Documentation generated successfully!"
    echo "📁 Location: $(pwd)/build/dokka/html/index.html"
    
    # Open documentation in browser (macOS)
    if [[ "$OSTYPE" == "darwin"* ]]; then
        echo "🌐 Opening documentation in browser..."
        open build/dokka/html/index.html
    fi
else
    echo "❌ Documentation generation failed!"
    exit 1
fi

echo "🎉 Build completed successfully!"
```

## 8. Kết quả cuối cùng

Sau khi hoàn thành các bước trên, bạn sẽ có:

### 8.1 Trang web tài liệu hoàn chỉnh:

- **URL**: `https://gatrongdev.github.io/kbignum/`
- **Cấu trúc**: 
  - Trang chủ với overview của library
  - API Reference đầy đủ cho tất cả classes và interfaces
  - Code samples có thể chạy được
  - Links đến source code trên GitHub
  - Platform-specific implementations được đánh dấu rõ ràng

### 8.2 Tính năng nổi bật:

- ✅ **Tự động cập nhật**: Mỗi khi push code mới, tài liệu được tự động rebuild
- ✅ **Cross-platform**: Hiển thị thống nhất API cho Android và iOS
- ✅ **Searchable**: Có chức năng tìm kiếm trong documentation
- ✅ **Responsive**: Hoạt động tốt trên mobile và desktop
- ✅ **SEO friendly**: Metadata và structure tối ưu cho search engines

### 8.3 Workflow tự động:

1. **Developer pushes code** → GitHub Actions trigger
2. **Build và test project** → Đảm bảo code quality
3. **Generate documentation** → Dokka tạo HTML files
4. **Deploy to GitHub Pages** → Tài liệu được publish tự động
5. **Notification** → Team được thông báo về deployment thành công

## 9. Troubleshooting

### 9.1 Common Issues:

**Issue**: Documentation không hiển thị code samples
**Solution**: Đảm bảo `samples.from("src/commonTest/kotlin")` được cấu hình đúng

**Issue**: Platform-specific documentation bị thiếu
**Solution**: Kiểm tra `sourceLink` configuration cho từng platform

**Issue**: GitHub Actions fail
**Solution**: Kiểm tra permissions trong repository settings

### 9.2 Performance Tips:

- Sử dụng `--parallel` flag để build nhanh hơn
- Cache Gradle dependencies trong CI/CD
- Exclude unnecessary files khỏi documentation generation

---

**🎯 Kết luận**: Dokka không chỉ có thể mà còn là lựa chọn tốt nhất để tạo và triển khai tài liệu cho thư viện Kotlin Multiplatform. Với cấu hình trên, bạn sẽ có một hệ thống documentation hoàn chỉnh, tự động và professional cho thư viện KBigNum.
