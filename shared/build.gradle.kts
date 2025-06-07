import com.vanniktech.maven.publish.SonatypeHost
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
    id("org.jetbrains.dokka") version "1.9.20"
}

group = "io.github.gatrongdev"
version = "0.0.1"

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
