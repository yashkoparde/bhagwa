package com.example.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.Hiking
import androidx.compose.ui.graphics.vector.ImageVector

enum class WorkoutType(val displayName: String, val caloriesPerKm: Int) {
    RUN("Run", 65),
    CYCLING("Ride", 35),
    WALK("Walk", 50),
    HIKE("Hike", 70);

    fun getIcon(): ImageVector {
        return when (this) {
            RUN -> Icons.AutoMirrored.Filled.DirectionsRun
            CYCLING -> Icons.Default.DirectionsBike
            WALK -> Icons.AutoMirrored.Filled.DirectionsWalk
            HIKE -> Icons.Default.Hiking
        }
    }
}

data class LocationPoint(
    val latitude: Double,
    val longitude: Double,
    val altitudeMeters: Double = 0.0,
    val timestampMs: Long = System.currentTimeMillis(),
    val speedMps: Double = 0.0
)

data class SplitInfo(
    val splitNumber: Int, // 1 for 1st km, 2 for 2nd km
    val durationSeconds: Long,
    val distanceKm: Double = 1.0,
    val avgPaceMinKm: String,
    val elevationGainMeters: Double = 0.0
)

enum class MilestoneCategory(val label: String) {
    DISTANCE("Distance"),
    PACE("Pace & Speed"),
    ELEVATION("Elevation"),
    STREAK("Consistency"),
    COUNT("Milestones Logged")
}

data class Milestone(
    val id: String,
    val title: String,
    val description: String,
    val category: MilestoneCategory,
    val targetValue: Double, // e.g. 5.0 for 5km
    val unit: String, // "km", "min/km", "m", "days"
    val isUnlocked: Boolean = false,
    val unlockedDateMs: Long? = null,
    val iconName: String = "trophy"
)

data class WorkoutActivity(
    val id: String,
    val title: String,
    val type: WorkoutType,
    val timestampMs: Long,
    val durationSeconds: Long,
    val movingTimeSeconds: Long,
    val distanceMeters: Double,
    val elevationGainMeters: Double,
    val calories: Int,
    val avgPaceMinKm: String,
    val maxSpeedKmh: Double,
    val routePoints: List<LocationPoint>,
    val splits: List<SplitInfo>,
    val notes: String = "",
    val effortRating: Int = 5, // 1 to 10
    val earnedMilestones: List<String> = emptyList()
)

data class PersonalRecords(
    val fastest1kSec: Long = Long.MAX_VALUE,
    val fastest5kSec: Long = Long.MAX_VALUE,
    val fastest10kSec: Long = Long.MAX_VALUE,
    val longestDistanceMeters: Double = 0.0,
    val maxElevationGainMeters: Double = 0.0,
    val totalDistanceMeters: Double = 0.0,
    val totalActivitiesCount: Int = 0,
    val weeklyGoalKm: Double = 25.0
)
