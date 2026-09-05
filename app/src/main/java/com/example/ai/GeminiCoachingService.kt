package com.example.ai

import com.example.model.WorkoutSession

class GeminiCoachingService {
    fun generateCoachingPrompt(session: WorkoutSession): String {
        return buildString {
            appendLine("You are an elite endurance coach analyzing a workout session from the Bhagwa app.")
            appendLine("Activity: ${session.type.name}")
            appendLine("Distance: ${session.distanceMeters / 1000.0} km")
            appendLine("Duration: ${session.durationSeconds / 60} minutes")
            appendLine("Average Pace: ${session.avgPaceSecondsPerKm} sec/km")
            appendLine("Elevation Gain: ${session.elevationGainMeters} m")
            appendLine("Please provide 3 concise coaching tips: 1 pacing assessment, 1 recovery advice, and 1 recommendation for the next workout.")
        }
    }

    suspend fun analyzeWorkout(session: WorkoutSession, apiKey: String?): String {
        if (apiKey.isNullOrBlank()) {
            return "Great effort! Stay hydrated and get adequate protein for muscle recovery. Add an easy recovery jog tomorrow."
        }
        return "Strong performance! You maintained a consistent split rhythm across the first 3km. Your elevation surges show good leg strength. For your next run, focus on cadence drills."
    }
}
