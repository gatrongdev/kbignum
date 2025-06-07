package io.github.gatrongdev.kbignum.math

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
