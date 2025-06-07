package io.github.gatrongdev.kbignum

import io.github.gatrongdev.kbignum.math.Greeting
import org.junit.Assert.assertTrue
import org.junit.Test

class AndroidGreetingTest {
    @Test
    fun testExample() {
        assertTrue("Check Android is mentioned", Greeting().greet().contains("Android"))
    }
}