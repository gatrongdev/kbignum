plugins {
    id("me.champeau.jmh") version "0.7.3"
}

dependencies {
    add("jmh", project(":shared"))
}

jmh {
    jmhVersion = "1.36"
    benchmarkMode = listOf("avgt")
    warmupIterations = 2
    iterations = 4
    fork = 3
    warmup = "250ms"
    timeOnIteration = "250ms"
    timeUnit = "ns"
    threads = 1
    resultFormat = "JSON"
    resultsFile = layout.buildDirectory.file("reports/jmh/results.json").get().asFile
}
