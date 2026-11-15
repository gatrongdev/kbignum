package io.github.gatrongdev.kbignum.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PlatformJsTest {
    @Test
    fun getPlatformReportsTheActiveJavaScriptRuntime() {
        val platformName = getPlatform().name
        if (js("typeof window !== 'undefined'") as Boolean) {
            assertEquals("Browser", platformName)
        } else {
            val nodeVersion: String = js("process.version")
            assertTrue(
                platformName == "Node.js $nodeVersion",
                "Expected Node.js $nodeVersion, got $platformName",
            )
        }
    }
}
