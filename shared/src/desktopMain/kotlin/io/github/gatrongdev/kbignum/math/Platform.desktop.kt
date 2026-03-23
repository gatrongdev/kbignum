package io.github.gatrongdev.kbignum.math

private class DesktopPlatform : Platform {
    override val name: String = System.getProperty("os.name") ?: "Desktop"
}

actual fun getPlatform(): Platform = DesktopPlatform()
