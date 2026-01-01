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
tasks.register("updateBenchmark") {
    group = "documentation"
    description = "Run benchmarks and update README.md with latest performance results"
    
    dependsOn("testDebugUnitTest")
    
    doLast {
        val reportFile = file("build/reports/tests/testDebugUnitTest/classes/io.github.gatrongdev.kbignum.benchmark.PerformanceComparisonTest.html")
        val readmeFile = rootProject.file("README.md")
        
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
            appendLine("`KBignum` offers competitive performance by utilizing efficient algorithms (e.g., bitwise arithmetic for `KBigInteger`). Below is a comparison against Java's native `BigInteger` (highly optimized C intrinsics) on JVM:")
            appendLine()
            
            var currentSection = ""
            for (line in lines) {
                when {
                    line.contains("**Basic Arithmetic") -> {
                        // Extract text between ** markers: "**Basic Arithmetic (2048-bit numbers)**"
                        val sectionTitle = line.replace("**", "").trim()
                        appendLine("### $sectionTitle")
                        appendLine("| Operation | Iterations | Java BigInteger (ms) | KBignum (ms) | Relative Speed |")
                        appendLine("| :--- | :---: | :---: | :---: | :---: |")
                    }
                    line.contains("**Factorial") -> {
                        appendLine()
                        appendLine("### Factorial (Repeated Multiplication)")
                        appendLine("| Operation | Iterations | Java BigInteger (ms) | KBignum (ms) | Relative Speed |")
                        appendLine("| :--- | :---: | :---: | :---: | :---: |")
                    }
                    line.startsWith("| mean") -> {
                        // Parse and format the row
                        val parts = line.split("|").map { it.trim() }.filter { it.isNotEmpty() }
                        if (parts.size >= 5) {
                            val operation = parts[0].removePrefix("mean ").trim()
                            val iterations = parts[1]
                            val javaMs = parts[2]
                            val kMs = parts[3]
                            val relative = parts[4]
                            
                            // Highlight faster operations
                            val formattedRelative = if (relative.startsWith("0.") || relative == "1.00x") {
                                "**$relative${if (relative.startsWith("0.")) " (Faster)" else " (Equal)"}**"
                            } else {
                                relative
                            }
                            
                            appendLine("| **$operation** | $iterations | $javaMs | $kMs | $formattedRelative |")
                        }
                    }
                    line.startsWith("###") || line.startsWith("| Operation") || line.startsWith("| :---") -> {
                        // Skip header lines, we add our own
                    }
                }
            }
            appendLine()
            appendLine("*Note: Benchmarks run on macOS/JVM. Results may vary by device. KBignum prioritizes portability and correctness over raw C-level speed. Division/Modulo use bitwise algorithm which is slower but portable.*")
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
