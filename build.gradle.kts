plugins {
    // trick: for the same plugin versions in all sub-modules
    alias(libs.plugins.android.library).apply(false)
    alias(libs.plugins.kotlin.multiplatform).apply(false)
    id("com.vanniktech.maven.publish") version "0.30.0" apply false

    // Code quality and security plugins
    id("org.jetbrains.kotlinx.kover") version "0.9.1" apply false
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1" apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.6" apply false

    // Dokka plugin for documentation
    id("org.jetbrains.dokka") version "1.9.20"
}

tasks.register<Delete>("clean") {
    delete(layout.buildDirectory)
}

tasks.register<Exec>("updateBenchmark") {
    group = "documentation"
    description = "Run the JVM benchmarks and regenerate the README chart and summary"
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
