package com.example

import org.junit.Assert.assertEquals
import org.junit.Test

class SplitCalculatorTest {

    @Test
    fun testKilometerSplitDetection() {
        val totalDistance = 3450.0 // 3.45 km
        val splitsCount = (totalDistance / 1000).toInt()
        assertEquals(3, splitsCount)
    }

    @Test
    fun testPaceFormatting() {
        val secondsPerKm = 315 // 5:15 min/km
        val minutes = secondsPerKm / 60
        val seconds = secondsPerKm % 60
        val formatted = String.format("%d:%02d", minutes, seconds)
        assertEquals("5:15", formatted)
    }
}
