import com.vanniktech.maven.publish.SonatypeHost
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    id("com.vanniktech.maven.publish") version "0.30.0"

    // Code quality and security plugins
    id("org.jetbrains.kotlinx.kover")
    id("org.jlleitschuh.gradle.ktlint")
    id("io.gitlab.arturbosch.detekt")

    // Dokka plugin for documentation
    id("org.jetbrains.dokka")
}

group = "io.github.gatrongdev"
version = "0.0.17"

fun Project.requiredIntProperty(name: String): Int =
    providers.gradleProperty(name).orNull?.toIntOrNull()
        ?: error("Required Gradle property '$name' is missing or not an Int")

val androidCompileSdk = project.requiredIntProperty("android.compileSdk")
val androidMinSdk = project.requiredIntProperty("android.minSdk")
val androidTargetSdk = project.requiredIntProperty("android.targetSdk")

kotlin {
    androidTarget {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_1_8)
                }
            }
        }
    }

    val xcf = XCFramework()
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
            xcf.add(this)
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            // put your multiplatform dependencies here
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "io.github.gatrongdev.kbignum"
    compileSdk = androidCompileSdk
    defaultConfig {
        minSdk = androidMinSdk
        targetSdk = androidTargetSdk
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    signAllPublications()

    coordinates(group.toString(), "kbignum", version.toString())

    pom {
        name = "KBigNum Library"
        description = "A Kotlin multiplatform library for arbitrary precision numbers, including KBigDecimal and KBigInteger types."
        inceptionYear = "2025"
        url = "https://github.com/gatrongdev/kbignum"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "https://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "gatrongdev"
                name = "Gatrong Dev"
                url = "https://github.com/gatrongdev"
            }
        }
        scm {
            url = "https://github.com/gatrongdev/kbignum"
            connection = "scm:git:https://github.com/gatrongdev/kbignum.git"
            developerConnection = "scm:git:ssh://git@github.com/gatrongdev/kbignum.git"
        }
    }
}

// ==================== Code Quality & Security Configuration ====================

// Kover configuration for test coverage
kover {
    reports {
        filters {
            excludes {
                // Loại trừ các file generated và platform-specific
                packages("*.generated.*")
                annotatedBy("*Generated*")
            }
        }
    }
}

// ktlint configuration for code style
ktlint {
    version.set("1.0.1")
    android.set(false)
    ignoreFailures.set(false)
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.SARIF)
    }
    filter {
        exclude("**/generated/**")
        include("**/kotlin/**")
    }
}

// detekt configuration for static analysis
detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom("$projectDir/config/detekt/detekt.yml")
    baseline = file("$projectDir/config/detekt/baseline.xml")
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    reports {
        html.required.set(true)
        xml.required.set(true)
        txt.required.set(true)
        sarif.required.set(true)
        md.required.set(true)
    }
}

// Tạo task để chạy tất cả checks
tasks.register("runAllChecks") {
    group = "verification"
    description = "Run all tests"
    dependsOn("test")
}

// ==================== Benchmark Update Task ====================

/**
 * Task to run performance benchmarks and update README.md with results.
 * 
 * Usage: ./gradlew shared:updateBenchmark
 * 
 * This task will:
 * 1. Run PerformanceComparisonTest
 * 2. Parse results from HTML report
 * 3. Update README.md Performance section
 */
// Define paths outside task for configuration cache compatibility
val benchmarkReportPath = layout.buildDirectory.file("reports/tests/testDebugUnitTest/classes/io.github.gatrongdev.kbignum.benchmark.PerformanceComparisonTest.html")
val readmePath = rootProject.layout.projectDirectory.file("README.md")

tasks.register("updateBenchmark") {
    group = "documentation"
    description = "Run benchmarks and update README.md with latest performance results"
    
    dependsOn("testDebugUnitTest")
    
    val reportFileProvider = benchmarkReportPath
    val readmeFileProvider = readmePath
    
    doLast {
        val reportFile = reportFileProvider.get().asFile
        val readmeFile = readmeFileProvider.asFile
        
        if (!reportFile.exists()) {
            logger.error("Benchmark report not found. Please run: ./gradlew shared:testDebugUnitTest --tests \"io.github.gatrongdev.kbignum.benchmark.PerformanceComparisonTest\"")
            return@doLast
        }
        
        // Parse benchmark results from HTML report
        val reportContent = reportFile.readText()
        val preContentRegex = Regex("<pre>(.*?)</pre>", RegexOption.DOT_MATCHES_ALL)
        val match = preContentRegex.find(reportContent)
        
        if (match == null) {
            logger.error("Could not parse benchmark results from report")
            return@doLast
        }
        
        val benchmarkOutput = match.groupValues[1].trim()
        val lines = benchmarkOutput.lines().filter { it.isNotBlank() }
        
        // Build new Performance section
        val newPerformanceSection = buildString {
            appendLine("## Performance")
            appendLine("`KBignum` offers competitive performance by utilizing efficient algorithms (Knuth's Algorithm D for division, optimized magnitude arithmetic). Below is a comparison against Java's native implementations on JVM:")
            appendLine()
            
            var inBigInteger = false
            var inBigDecimal = false
            
            for (line in lines) {
                when {
                    line.contains("## KBigInteger") -> {
                        inBigInteger = true
                        inBigDecimal = false
                        appendLine("### KBigInteger")
                        appendLine()
                    }
                    line.contains("## KBigDecimal") -> {
                        inBigInteger = false
                        inBigDecimal = true
                        appendLine("### KBigDecimal")
                        appendLine()
                    }
                    line.contains("**Basic Arithmetic") && line.contains("2048") -> {
                        appendLine("#### 2048-bit Numbers")
                        appendLine("| Operation | Java (ms) | KBignum (ms) | Relative |")
                        appendLine("| :--- | :---: | :---: | :---: |")
                    }
                    line.contains("**Basic Arithmetic") && line.contains("4096") -> {
                        appendLine()
                        appendLine("#### 4096-bit Numbers")
                        appendLine("| Operation | Java (ms) | KBignum (ms) | Relative |")
                        appendLine("| :--- | :---: | :---: | :---: |")
                    }
                    line.contains("**Decimal Arithmetic") && line.contains("20") -> {
                        appendLine("#### 20-digit Decimals")
                        appendLine("| Operation | Java (ms) | KBigDecimal (ms) | Relative |")
                        appendLine("| :--- | :---: | :---: | :---: |")
                    }
                    line.contains("**Decimal Arithmetic") && line.contains("50") -> {
                        appendLine()
                        appendLine("#### 50-digit Decimals")
                        appendLine("| Operation | Java (ms) | KBigDecimal (ms) | Relative |")
                        appendLine("| :--- | :---: | :---: | :---: |")
                    }
                    line.contains("**Factorial") -> {
                        // Skip factorial for cleaner output
                    }
                    line.startsWith("| mean") -> {
                        // Parse and format the row
                        val parts = line.split("|").map { it.trim() }.filter { it.isNotEmpty() }
                        if (parts.size >= 5) {
                            var operation = parts[0].removePrefix("mean ").trim()
                            // Clean up operation name
                            operation = operation.replace(Regex("\\s+\\d+-bit"), "")
                                .replace(Regex("Decimal\\s+"), "")
                                .replace(Regex("\\s+\\d+d"), "")
                            val javaMs = parts[2]
                            val kMs = parts[3]
                            val relative = parts[4]
                            
                            // Skip factorial rows
                            if (operation.contains("Factorial")) continue
                            
                            // Highlight faster/equal operations
                            val formattedRelative = when {
                                relative.startsWith("0.") -> "**$relative ✓**"
                                relative == "1.00x" -> "**1.00x ✓**"
                                else -> relative
                            }
                            
                            appendLine("| **$operation** | $javaMs | $kMs | $formattedRelative |")
                        }
                    }
                }
            }
            appendLine()
            appendLine("*Note: Benchmarks run on macOS/JVM. ✓ indicates KBignum is faster or equal to Java. KBignum prioritizes portability across KMP targets (Android, iOS, JS, Native).*")
        }
        
        // Update README.md
        val readmeContent = readmeFile.readText()
        val performancePattern = Regex(
            """## Performance.*?(?=\n## [A-Z])""",
            setOf(RegexOption.DOT_MATCHES_ALL)
        )
        
        val updatedContent = performancePattern.replace(readmeContent) {
            newPerformanceSection.trimEnd() + "\n\n"
        }
        
        readmeFile.writeText(updatedContent)
        logger.lifecycle("✅ README.md updated with latest benchmark results!")
    }
}
