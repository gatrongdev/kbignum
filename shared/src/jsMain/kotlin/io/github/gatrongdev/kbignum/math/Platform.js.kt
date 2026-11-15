package io.github.gatrongdev.kbignum.math

private class NodeJsPlatform : Platform {
    override val name: String = "Node.js " + js("process.version")
}

actual fun getPlatform(): Platform = NodeJsPlatform()
