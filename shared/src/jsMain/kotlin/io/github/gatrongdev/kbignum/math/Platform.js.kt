package io.github.gatrongdev.kbignum.math

private class JavaScriptPlatform(override val name: String) : Platform

private const val NODE_JS_RUNTIME =
    "typeof process !== 'undefined' && process !== null && typeof process.version === 'string'"
private const val BROWSER_RUNTIME =
    "typeof window !== 'undefined' && typeof document !== 'undefined'"

actual fun getPlatform(): Platform =
    when {
        js(NODE_JS_RUNTIME) as Boolean -> JavaScriptPlatform("Node.js " + js("process.version"))
        js(BROWSER_RUNTIME) as Boolean -> JavaScriptPlatform("Browser")
        else -> JavaScriptPlatform("JavaScript")
    }
