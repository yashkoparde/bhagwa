package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.*

class LocationMathTest {

    private fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000.0 // Earth radius in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    @Test
    fun testHaversineDistanceAccuracy() {
        // Distance between Bangalore (12.9716, 77.5946) and Belgaum (15.8497, 74.4977) ~ 460km
        val distance = haversine(12.9716, 77.5946, 15.8497, 74.4977)
        assertTrue("Distance should be approximately 460km", distance in 450000.0..470000.0)
    }

    @Test
    fun testZeroDistanceForSamePoint() {
        val distance = haversine(15.8497, 74.4977, 15.8497, 74.4977)
        assertEquals(0.0, distance, 0.001)
    }
}
