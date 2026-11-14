plugins {
    // trick: for the same plugin versions in all sub-modules
    alias(libs.plugins.android.kotlin.multiplatform.library).apply(false)
    alias(libs.plugins.kotlin.multiplatform).apply(false)
    id("com.vanniktech.maven.publish") version "0.30.0" apply false

    // Code quality and security plugins
    id("org.jetbrains.kotlinx.kover") version "0.9.9" apply false
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1" apply false
    id("dev.detekt") version "2.0.0-alpha.6" apply false

    // Dokka plugin for documentation
    id("org.jetbrains.dokka") version "1.9.20"
}

tasks.register<Delete>("clean") {
    delete(layout.buildDirectory)
}

tasks.register<Exec>("updateBenchmark") {
    group = "documentation"
    description = "Run the JVM benchmarks and regenerate integer and decimal README charts and summaries"
    dependsOn(":benchmarks:jmh")

    val renderer = layout.projectDirectory.file("scripts/render_benchmark_charts.py")
    val results = layout.projectDirectory.file("benchmarks/build/reports/jmh/results.json")

    doFirst {
        if (!results.asFile.isFile) {
            error("JMH result file was not created: ${results.asFile}")
        }
        commandLine("python3", renderer.asFile.absolutePath, results.asFile.absolutePath)
    }
}

// Keep the documented `./gradlew clean build test` entry point working with the
// Android KMP plugin, which exposes testAndroid/testAndroidHostTest instead of `test`.
tasks.register("test") {
    group = "verification"
    description = "Runs the shared module's Android host and desktop test suites."
    dependsOn(":shared:runAllTest")
}
