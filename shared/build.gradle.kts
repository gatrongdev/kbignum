import com.vanniktech.maven.publish.SonatypeHost
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    id("com.vanniktech.maven.publish") version "0.30.0"

    // Code quality and security plugins
    id("org.jetbrains.kotlinx.kover")
    id("org.jlleitschuh.gradle.ktlint")
    id("dev.detekt")

    // Dokka plugin for documentation
    id("org.jetbrains.dokka")
}

group = "io.github.gatrongdev"
version = "0.0.22"

fun Project.requiredIntProperty(name: String): Int =
    providers.gradleProperty(name).orNull?.toIntOrNull()
        ?: error("Required Gradle property '$name' is missing or not an Int")

val androidCompileSdk = project.providers.gradleProperty("android.compileSdk").orNull?.toIntOrNull() ?: 37
val androidMinSdk = project.providers.gradleProperty("android.minSdk").orNull?.toIntOrNull() ?: 24
kotlin {
    targets.withType<com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget>().configureEach {
        namespace = "io.github.gatrongdev.kbignum"
        compileSdk = androidCompileSdk
        minSdk = androidMinSdk
        withHostTest {}
        compilations.configureEach {
            compileTaskProvider.configure {
                compilerOptions.jvmTarget.set(JvmTarget.JVM_1_8)
            }
        }
    }

    jvm("desktop") {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_17)
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
        val commonMain by getting {
            dependencies {
                // put your multiplatform dependencies here
            }
        }
        val iosMain by creating {
            dependsOn(commonMain)
        }
        val iosX64Main by getting {
            dependsOn(iosMain)
        }
        val iosArm64Main by getting {
            dependsOn(iosMain)
        }
        val iosSimulatorArm64Main by getting {
            dependsOn(iosMain)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        val desktopMain by getting {
            dependencies {
                // No additional desktop-specific dependencies required.
            }
        }
    }

    jvmToolchain(17)
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    signAllPublications()

    coordinates(group.toString(), "kbignum", version.toString())

    pom {
        name = "KBigNum Library"
        description =
            "A Kotlin multiplatform library for arbitrary precision numbers, " +
            "including KBigDecimal and KBigInteger types."
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
        verify {
            rule {
                minBound(87, CoverageUnit.LINE)
                minBound(80, CoverageUnit.BRANCH)
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
    baseline = layout.projectDirectory.file("config/detekt/baseline.xml").asFile
}

tasks.withType<dev.detekt.gradle.Detekt>().configureEach {
    reports {
        html.required.set(true)
        checkstyle.required.set(true)
        sarif.required.set(true)
        markdown.required.set(true)
    }
}

// Tạo task để chạy tất cả checks
tasks.register("runAllChecks") {
    group = "verification"
    description = "Run platform tests, coverage verification, and code quality checks"
    dependsOn("runAllTest", "ktlintCheck", "detekt", "koverXmlReport", "koverVerify")
}

tasks.register("runAllTest") {
    group = "verification"
    description = "Run all code test case checks"
    dependsOn("testAndroid", "desktopTest")
}
