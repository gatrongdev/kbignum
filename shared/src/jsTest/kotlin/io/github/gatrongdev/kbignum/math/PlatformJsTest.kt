package io.github.gatrongdev.kbignum.math

import kotlin.test.Test
import kotlin.test.assertTrue

class PlatformJsTest {
    @Test
    fun getPlatformReportsNodeJsRuntime() {
        assertTrue(getPlatform().name.startsWith("Node.js v"))
    }
}
