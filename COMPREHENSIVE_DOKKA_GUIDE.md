# Hướng Dẫn Toàn Diện: Tạo và Triển Khai Tài Liệu cho Thư Viện Kotlin Multiplatform với Dokka

> **Sử dụng thư viện KBigNum làm ví dụ thực tế**

## 1. Giới thiệu và Xác nhận

**CÂU TRẢ LỜI NGẮN GỌN:** 
- ✅ **CÓ**, bạn có thể sử dụng Dokka cho thư viện Kotlin Multiplatform
- ✅ **CÓ**, Dokka có thể tự động tạo và triển khai trang web tài liệu từ mã nguồn chung (common code)

### Tại sao Dokka là lựa chọn tốt nhất?

**Dokka** là công cụ chính thức và phù hợp nhất để tạo tài liệu cho dự án Kotlin Multiplatform vì:

- **Hỗ trợ đa nền tảng**: Dokka tự động nhận diện và xử lý mã nguồn từ `commonMain`, `androidMain`, `iosMain`
- **Tích hợp KDoc**: Hỗ trợ đầy đủ các tag KDoc như `@param`, `@return`, `@sample`, `@throws`
- **Tạo API documentation thống nhất**: Kết hợp tài liệu từ tất cả nền tảng thành một trang web duy nhất
- **Tự động triển khai**: Tích hợp dễ dàng với GitHub Actions và GitHub Pages
- **Customizable**: Cho phép tùy chỉnh giao diện, logo, CSS, và layout

## 2. Cấu hình Dokka trong Gradle

### 2.1 Thêm Dokka Plugin

Chỉnh sửa file `build.gradle.kts` gốc của project:

```kotlin
plugins {
    // Các plugin hiện tại
    alias(libs.plugins.androidLibrary).apply(false)
    alias(libs.plugins.kotlinMultiplatform).apply(false)
    id("com.vanniktech.maven.publish") version "0.30.0"
    
    // Code quality plugins
    id("org.jetbrains.kotlinx.kover") version "0.8.3" apply false
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1" apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.6" apply false
    
    // ✨ Dokka plugin - phiên bản mới nhất
    id("org.jetbrains.dokka") version "1.9.20"
}
```

### 2.2 Cấu hình Dokka trong Module Shared

Chỉnh sửa file `shared/build.gradle.kts`:

```kotlin
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    id("com.vanniktech.maven.publish") version "0.30.0"
    
    // Code quality plugins
    id("org.jetbrains.kotlinx.kover")
    id("org.jlleitschuh.gradle.ktlint")
    id("io.gitlab.arturbosch.detekt")
    
    // ✨ Dokka plugin
    id("org.jetbrains.dokka") version "1.9.20"
}

// Existing configuration...
group = "io.github.gatrongdev"
version = "0.0.1"

// ✨ Cấu hình chi tiết cho Dokka
dokka {
    dokkaSourceSets {
        // Cấu hình cho Common code
        named("commonMain") {
            displayName.set("Common")
            platform.set(org.jetbrains.dokka.Platform.common)
            
            // Link đến source code trên GitHub
            sourceLink {
                localDirectory.set(file("src/commonMain/kotlin"))
                remoteUrl.set(uri("https://github.com/gatrongdev/kbignum/tree/main/shared/src/commonMain/kotlin").toURL())
                remoteLineSuffix.set("#L")
            }
            
            // External documentation links
            externalDocumentationLink {
                url.set(uri("https://kotlinlang.org/api/latest/jvm/stdlib/").toURL())
            }
            
            // Documentation samples
            samples.from("src/commonMain/kotlin")
            includes.from("docs/packages.md")
        }
        
        // Cấu hình cho Android
        named("androidMain") {
            displayName.set("Android")
            platform.set(org.jetbrains.dokka.Platform.jvm)
            
            sourceLink {
                localDirectory.set(file("src/androidMain/kotlin"))
                remoteUrl.set(uri("https://github.com/gatrongdev/kbignum/tree/main/shared/src/androidMain/kotlin").toURL())
                remoteLineSuffix.set("#L")
            }
            
            externalDocumentationLink {
                url.set(uri("https://developer.android.com/reference/").toURL())
            }
        }
        
        // Cấu hình cho iOS
        named("iosMain") {
            displayName.set("iOS")
            platform.set(org.jetbrains.dokka.Platform.native)
            
            sourceLink {
                localDirectory.set(file("src/iosMain/kotlin"))
                remoteUrl.set(uri("https://github.com/gatrongdev/kbignum/tree/main/shared/src/iosMain/kotlin").toURL())
                remoteLineSuffix.set("#L")
            }
        }
    }
    
    // Cấu hình giao diện tùy chỉnh
    pluginsMapConfiguration.set(
        mapOf(
            "org.jetbrains.dokka.base.DokkaBase" to """
            {
                "customAssets": ["${file("docs/logo.png")}"],
                "customStyleSheets": ["${file("docs/custom.css")}"],
                "footerMessage": "© 2025 Gatrong Dev - KBigNum Library"
            }
            """
        )
    )
}

// ✨ Tác vụ tùy chỉnh để tạo documentation
tasks.register("generateDocs") {
    dependsOn("dokkaHtml")
    group = "documentation"
    description = "Generate complete HTML documentation for KBigNum library"
    
    doLast {
        println("📚 Documentation generated successfully!")
        println("📂 Location: ${project.buildDir}/dokka/html/index.html")
        println("🌐 To preview locally: open ${project.buildDir}/dokka/html/index.html")
    }
}

// Đảm bảo tài liệu được tạo cùng với build
tasks.named("build") {
    dependsOn("generateDocs")
}

// Existing configuration continues...
kotlin {
    // Your existing Kotlin configuration
}

android {
    // Your existing Android configuration
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

# Quyền cần thiết để deploy lên GitHub Pages
permissions:
  contents: read
  pages: write
  id-token: write

# Chỉ chạy một job deploy cùng lúc
concurrency:
  group: "pages"
  cancel-in-progress: false

jobs:
  generate-docs:
    runs-on: ubuntu-latest
    
    steps:
    - name: 📥 Checkout repository
      uses: actions/checkout@v4
      
    - name: ☕ Set up JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
        
    - name: 🐘 Cache Gradle packages
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
      
    - name: 📚 Generate Dokka documentation
      run: ./gradlew dokkaHtml
      
    - name: 📂 List generated files (for debugging)
      run: |
        echo "Generated documentation files:"
        find shared/build/dokka/html -type f -name "*.html" | head -10
        
    - name: 🚀 Upload Pages artifact
      uses: actions/upload-pages-artifact@v2
      with:
        path: shared/build/dokka/html
  
  # Job deploy riêng biệt
  deploy:
    environment:
      name: github-pages
      url: ${{ steps.deployment.outputs.page_url }}
    runs-on: ubuntu-latest
    needs: generate-docs
    if: github.ref == 'refs/heads/main'
    
    steps:
    - name: 🌐 Deploy to GitHub Pages
      id: deployment
      uses: actions/deploy-pages@v2
      
    - name: ✅ Documentation deployed successfully
      run: |
        echo "📚 Documentation has been deployed to GitHub Pages!"
        echo "🔗 URL: ${{ steps.deployment.outputs.page_url }}"
        echo "📖 Your API documentation is now available online!"
```

### 3.2 Cấu hình GitHub Pages

1. Vào **Settings** → **Pages** trong repository GitHub
2. Chọn **Source**: "GitHub Actions"
3. Workflow sẽ tự động chạy khi push code lên nhánh `main`

## 4. Cách Dokka xử lý mã nguồn chung (Common Code)

### 4.1 Các đặc điểm chính của Dokka với KMP:

1. **Tự động merge documentation**: Dokka tự động kết hợp tài liệu từ `commonMain` với các nền tảng cụ thể
2. **Cross-platform references**: Tạo liên kết chéo giữa common code và platform-specific implementations
3. **Unified API view**: Cung cấp một giao diện API thống nhất cho tất cả nền tảng
4. **Platform indicators**: Hiển thị rõ ràng API nào có sẵn trên nền tảng nào

### 4.2 Cách Dokka xử lý `expect/actual`:

```kotlin
// commonMain/KBigDecimal.kt
/**
 * Common interface documentation sẽ xuất hiện trong tài liệu chính
 */
expect class KBigDecimalImpl : KBigDecimal

// androidMain/KBigDecimalImpl.android.kt  
/**
 * Android-specific implementation notes
 */
actual class KBigDecimalImpl : KBigDecimal {
    // Implementation details
}

// iosMain/KBigDecimalImpl.ios.kt
/**
 * iOS-specific implementation notes  
 */
actual class KBigDecimalImpl : KBigDecimal {
    // Implementation details
}
```

Dokka sẽ:
- Hiển thị interface chung từ `commonMain`
- Thêm tabs/sections cho từng nền tảng
- Ghi chú implementation differences
- Tạo navigation tree thống nhất

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
 * ## Examples:
 * ```kotlin
 * // Basic arithmetic
 * val a = "123.456".toKBigDecimal()
 * val b = "789.123".toKBigDecimal()
 * val sum = a + b  // 912.579
 * 
 * // Precision control
 * val result = a.divide(b, scale = 4, roundingMode = RoundingMode.HALF_UP)
 * ```
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
 * ## Examples:
 * ```kotlin
 * // Square root calculation
 * val number = "16".toKBigDecimal()
 * val sqrt = KBigMath.sqrt(number, scale = 6) // 4.000000
 * 
 * // Factorial calculation
 * val factorial = KBigMath.factorial("10".toKBigInteger()) // 3628800
 * 
 * // Prime checking
 * val isPrime = KBigMath.isPrime("97".toKBigInteger()) // true
 * ```
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
     * Computes n! = n × (n-1) × (n-2) × ... × 2 × 1
     * Uses iterative approach for better memory efficiency.
     * 
     * @param n the KBigInteger to calculate factorial of
     * @return factorial of n
     * @throws IllegalArgumentException if n is negative
     * 
     * @sample io.github.gatrongdev.kbignum.samples.KBigMathSamples.factorial
     */
    fun factorial(n: KBigInteger): KBigInteger
    
    /**
     * Calculate greatest common divisor (GCD) using Euclidean algorithm.
     * 
     * Finds the largest positive integer that divides both a and b.
     * Uses the efficient Euclidean algorithm: GCD(a,b) = GCD(b, a mod b)
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
     * Check if a KBigInteger is a prime number.
     * 
     * Uses trial division algorithm for primality testing.
     * For very large numbers, this may be slow.
     * 
     * @param n the KBigInteger to test for primality
     * @return true if n is prime, false otherwise
     * 
     * @sample io.github.gatrongdev.kbignum.samples.KBigMathSamples.primeCheck
     */
    fun isPrime(n: KBigInteger): Boolean
    
    /**
     * Calculate power: base^exponent.
     * 
     * Uses efficient exponentiation by squaring algorithm.
     * 
     * @param base the base KBigDecimal
     * @param exponent the exponent (must be non-negative)
     * @return base raised to the power of exponent
     * @throws IllegalArgumentException if exponent is negative
     * 
     * @sample io.github.gatrongdev.kbignum.samples.KBigMathSamples.power
     */
    fun pow(base: KBigDecimal, exponent: Int): KBigDecimal
}
```

## 6. Cấu trúc thư mục đề xuất

### 6.1 Tạo thư mục docs/

```
KBigNum/
├── docs/
│   ├── logo.png              # Logo cho documentation
│   ├── custom.css           # CSS tùy chỉnh
│   ├── packages.md          # Module overview
│   └── samples/             # Thư mục chứa sample code
│       ├── KBigDecimalSamples.kt
│       ├── KBigIntegerSamples.kt
│       └── KBigMathSamples.kt
├── shared/
│   ├── src/
│   │   ├── commonMain/
│   │   ├── androidMain/
│   │   └── iosMain/
│   └── build.gradle.kts
├── build.gradle.kts
└── README.md
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
        val number = "123.456789".toKBigDecimal()
        
        // Set scale with different rounding modes
        val rounded = number.setScale(2, RoundingMode.HALF_UP) // 123.46
        val truncated = number.setScale(2, RoundingMode.DOWN) // 123.45
    }
    
    fun addition() {
        val a = "999.999".toKBigDecimal()
        val b = "0.001".toKBigDecimal()
        val result = a.add(b) // 1000.000
    }
    
    fun subtraction() {
        val a = "1000.000".toKBigDecimal()
        val b = "0.001".toKBigDecimal()
        val result = a.subtract(b) // 999.999
    }
    
    fun multiplication() {
        val a = "123.45".toKBigDecimal()
        val b = "67.89".toKBigDecimal()
        val result = a.multiply(b) // 8382.6405
    }
    
    fun division() {
        val a = "100".toKBigDecimal()
        val b = "3".toKBigDecimal()
        val result = a.divide(b, 6) // 33.333333
    }
    
    fun divisionWithRounding() {
        val a = "100".toKBigDecimal()
        val b = "3".toKBigDecimal()
        val result = a.divide(b, 2, RoundingMode.HALF_UP) // 33.33
    }
    
    fun absoluteValue() {
        val negative = "-123.45".toKBigDecimal()
        val positive = negative.abs() // 123.45
    }
    
    fun signumFunction() {
        val positive = "123".toKBigDecimal()
        val negative = "-123".toKBigDecimal()
        val zero = "0".toKBigDecimal()
        
        println(positive.signum()) // 1
        println(negative.signum()) // -1
        println(zero.signum())     // 0
    }
    
    fun scaleAdjustment() {
        val number = "123.456789".toKBigDecimal()
        val scaled = number.setScale(3, RoundingMode.HALF_UP) // 123.457
    }
    
    fun integerConversion() {
        val decimal = "123.789".toKBigDecimal()
        val integer = decimal.toBigInteger() // 123
    }
}
```

### 6.3 Tạo CSS tùy chỉnh

Tạo file `docs/custom.css`:

```css
/* Custom styling for KBigNum documentation */

:root {
    --primary-color: #1976d2;
    --secondary-color: #42a5f5;
    --accent-color: #ff9800;
    --text-color: #212121;
    --background-color: #fafafa;
}

/* Header styling */
.library-name a {
    color: var(--primary-color) !important;
    font-weight: 700;
    font-size: 1.5em;
}

/* Navigation styling */
.navigation-wrapper {
    background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
}

/* Code blocks */
.sample-container {
    background: #f5f5f5;
    border-left: 4px solid var(--primary-color);
    padding: 1rem;
    margin: 1rem 0;
}

/* Platform indicators */
.platform-hinted {
    border-radius: 8px;
    overflow: hidden;
}

.platform-hinted[data-platform-hinted="common"] {
    background: linear-gradient(90deg, #4caf50, #66bb6a);
}

.platform-hinted[data-platform-hinted="jvm"] {
    background: linear-gradient(90deg, #ff9800, #ffb74d);
}

.platform-hinted[data-platform-hinted="native"] {
    background: linear-gradient(90deg, #9c27b0, #ba68c8);
}

/* Custom footer */
.footer {
    background: var(--primary-color);
    color: white;
    text-align: center;
    padding: 2rem;
    margin-top: 2rem;
}

/* Responsive design */
@media (max-width: 768px) {
    .library-name a {
        font-size: 1.2em;
    }
}
```

### 6.4 Tạo Module Overview

Tạo file `docs/packages.md`:

```markdown
# KBigNum Library Documentation

## Overview

KBigNum is a Kotlin Multiplatform library providing arbitrary precision arithmetic for decimal and integer numbers. It offers a unified API across Android and iOS platforms.

## Core Modules

### math.math Package

Contains the main interfaces and implementations for big number operations:

- **KBigDecimal**: Arbitrary precision decimal numbers
- **KBigInteger**: Arbitrary precision integer numbers  
- **KBigMath**: Advanced mathematical operations
- **Extension Functions**: Convenient conversion and operator functions

## Platform Support

- **Android**: Uses Java's BigDecimal and BigInteger
- **iOS**: Uses Foundation's NSDecimalNumber and NSNumber

## Getting Started

Add the dependency to your project and start using big numbers with unlimited precision!
```

## 7. Các lệnh Gradle hữu ích

### 7.1 Lệnh tạo documentation:

```bash
# Tạo HTML documentation
./gradlew dokkaHtml

# Tạo documentation cho multi-module project
./gradlew dokkaHtmlMultiModule

# Tạo documentation partial (cho từng module)
./gradlew dokkaHtmlPartial

# Chạy tác vụ tùy chỉnh
./gradlew generateDocs

# Xem documentation locally
open shared/build/dokka/html/index.html
```

### 7.2 Lệnh debug và troubleshooting:

```bash
# Xem thông tin chi tiết về Dokka tasks
./gradlew tasks --group=documentation

# Chạy với verbose output
./gradlew dokkaHtml --info

# Clean và rebuild documentation
./gradlew clean dokkaHtml

# Kiểm tra cấu hình Dokka
./gradlew help --task dokkaHtml
```

## 8. Kết quả cuối cùng

### 8.1 Trang web documentation hoàn chỉnh

Sau khi setup thành công, bạn sẽ có:

- **📚 Trang web tài liệu hoàn chỉnh**: Được tạo tự động từ mã nguồn
- **🌐 URL truy cập**: `https://gatrongdev.github.io/kbignum/`
- **📱 Responsive design**: Hoạt động tốt trên mobile và desktop
- **🔍 Search functionality**: Tìm kiếm API và functions
- **📖 Complete API reference**: Tài liệu đầy đủ cho tất cả classes và methods
- **💡 Code samples**: Ví dụ sử dụng thực tế
- **🎨 Custom branding**: Logo và styling riêng cho thư viện

### 8.2 Tự động cập nhật

- ✅ **Tự động build**: Mỗi khi push code mới
- ✅ **Tự động deploy**: Lên GitHub Pages
- ✅ **Sync với code**: Tài liệu luôn đồng bộ với source code
- ✅ **Multi-platform**: Hiển thị tài liệu cho tất cả nền tảng

### 8.3 Ví dụ về URL cuối cùng:

```
https://gatrongdev.github.io/kbignum/
├── index.html                    # Trang chủ
├── kbignum/                     # Package overview
│   └── io.github.gatrongdev.kbignum.math.math/
│       ├── -k-big-decimal/      # KBigDecimal documentation
│       ├── -k-big-integer/      # KBigInteger documentation  
│       ├── -k-big-math/         # KBigMath documentation
│       └── index.html           # Package index
└── navigation.html              # Navigation sidebar
```

### 8.4 Tính năng đặc biệt cho KMP:

1. **Platform tabs**: Chuyển đổi giữa Common/Android/iOS
2. **Expect/actual mapping**: Liên kết giữa interface và implementation
3. **Platform availability indicators**: Hiển thị API có sẵn trên nền tảng nào
4. **Cross-references**: Tham chiếu chéo giữa các nền tảng

---

## 🎉 Kết luận

Với hướng dẫn này, bạn đã có thể:

1. ✅ **Setup Dokka** cho Kotlin Multiplatform project
2. ✅ **Tạo tài liệu từ common code** với KDoc hoàn chỉnh  
3. ✅ **Tự động deploy** lên GitHub Pages
4. ✅ **Customize giao diện** với CSS và branding riêng
5. ✅ **Maintain documentation** đồng bộ với source code

**Dokka không chỉ CÓ THỂ mà còn là CÁCH TỐT NHẤT để tạo documentation cho Kotlin Multiplatform libraries!** 🚀

---

*© 2025 Hướng dẫn này được tạo cho thư viện KBigNum - Ví dụ thực tế về Kotlin Multiplatform Documentation*
