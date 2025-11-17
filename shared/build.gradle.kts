import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    id("com.vanniktech.maven.publish") version "0.35.0"

    // Code quality and security plugins
    id("org.jetbrains.kotlinx.kover")
    id("org.jlleitschuh.gradle.ktlint")
    id("io.gitlab.arturbosch.detekt")

    // Dokka plugin for documentation
    id("org.jetbrains.dokka")
}

group = "io.github.gatrongdev"
version = "0.0.17"

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
    compileSdk = 35
    defaultConfig {
        minSdk = 21
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
    description = "Run all code quality checks"
    dependsOn("test", "ktlintCheck", "detekt", "koverXmlReport")
}
