plugins {
    id("me.champeau.jmh") version "0.7.3"
}

dependencies {
    add("jmh", project(":shared"))
}

jmh {
    jmhVersion = "1.36"
    benchmarkMode = listOf("avgt")
    warmupIterations = 3
    iterations = 5
    fork = 2
    warmup = "500ms"
    timeOnIteration = "500ms"
    timeUnit = "ns"
    threads = 1
    resultFormat = "JSON"
    resultsFile = layout.buildDirectory.file("reports/jmh/results.json").get().asFile
}
