plugins {
    //trick: for the same plugin versions in all sub-modules
    alias(libs.plugins.android.library).apply(false)
    alias(libs.plugins.kotlin.multiplatform).apply(false)
    id("com.vanniktech.maven.publish") version "0.30.0"
    
    // Code quality and security plugins
    id("org.jetbrains.kotlinx.kover") version "0.8.3" apply false
    id("org.jlleitschuh.gradle.ktlint") version "12.3.0" apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.6" apply false
    
    // Dokka plugin for documentation
    id("org.jetbrains.dokka") version "1.9.20"
}
