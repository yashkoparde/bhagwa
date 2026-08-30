package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.LocalWorkoutRepository
import com.example.location.LocationTracker
import com.example.location.TrackingData
import com.example.location.TrackingState
import com.example.model.Milestone
import com.example.model.PersonalRecords
import com.example.model.WorkoutActivity
import com.example.model.WorkoutType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

sealed interface NavigationDestination {
    data object Feed : NavigationDestination
    data object Record : NavigationDestination
    data object Milestones : NavigationDestination
    data object Dashboard : NavigationDestination
    data class ActivityDetail(val activityId: String) : NavigationDestination
}

class WorkoutViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LocalWorkoutRepository(application)
    val locationTracker = LocationTracker(application)

    val trackingData: StateFlow<TrackingData> = locationTracker.trackingData

    private val _currentDestination = MutableStateFlow<NavigationDestination>(NavigationDestination.Feed)
    val currentDestination: StateFlow<NavigationDestination> = _currentDestination.asStateFlow()

    private val _activities = MutableStateFlow<List<WorkoutActivity>>(emptyList())
    val activities: StateFlow<List<WorkoutActivity>> = _activities.asStateFlow()

    private val _milestones = MutableStateFlow<List<Milestone>>(emptyList())
    val milestones: StateFlow<List<Milestone>> = _milestones.asStateFlow()

    private val _personalRecords = MutableStateFlow(PersonalRecords())
    val personalRecords: StateFlow<PersonalRecords> = _personalRecords.asStateFlow()

    // Active unlocked milestone alert banner
    private val _newlyEarnedMilestonesAlert = MutableStateFlow<List<Milestone>>(emptyList())
    val newlyEarnedMilestonesAlert: StateFlow<List<Milestone>> = _newlyEarnedMilestonesAlert.asStateFlow()

    init {
        loadAllData()
    }

    fun loadAllData() {
        viewModelScope.launch {
            _activities.value = repository.getActivities()
            _milestones.value = repository.getMilestones()
            _personalRecords.value = repository.getPersonalRecords()
        }
    }

    fun navigateTo(dest: NavigationDestination) {
        _currentDestination.value = dest
    }

    fun startWorkout(type: WorkoutType, isSimulated: Boolean = true) {
        locationTracker.startTracking(type, isSimulated)
        _currentDestination.value = NavigationDestination.Record
    }

    fun pauseWorkout() {
        locationTracker.pauseTracking()
    }

    fun resumeWorkout() {
        locationTracker.resumeTracking()
    }

    fun toggleSimulationMode(enableSim: Boolean) {
        locationTracker.toggleSimulationMode(enableSim)
    }

    fun saveCompletedWorkout(
        title: String,
        notes: String,
        effortRating: Int
    ) {
        val finalData = locationTracker.stopTracking()
        val defaultTitle = if (title.isBlank()) {
            "${finalData.workoutType.displayName} Session ⚡"
        } else {
            title
        }

        // Evaluate milestones
        val paceSec = parsePaceToSec(finalData.avgPaceMinKm)
        val earned = repository.evaluateMilestonesForActivity(
            distanceMeters = finalData.totalDistanceMeters,
            durationSeconds = finalData.elapsedSeconds,
            elevationMeters = finalData.elevationGainMeters,
            avgPaceMinKmSeconds = paceSec
        )

        if (earned.isNotEmpty()) {
            _newlyEarnedMilestonesAlert.value = earned
        }

        val activity = WorkoutActivity(
            id = UUID.randomUUID().toString(),
            title = defaultTitle,
            type = finalData.workoutType,
            timestampMs = System.currentTimeMillis(),
            durationSeconds = finalData.elapsedSeconds,
            movingTimeSeconds = finalData.movingSeconds,
            distanceMeters = finalData.totalDistanceMeters,
            elevationGainMeters = finalData.elevationGainMeters,
            calories = finalData.estimatedCalories,
            avgPaceMinKm = finalData.avgPaceMinKm,
            maxSpeedKmh = finalData.maxSpeedKmh,
            routePoints = finalData.routePoints,
            splits = finalData.splits,
            notes = notes,
            effortRating = effortRating,
            earnedMilestones = earned.map { it.id }
        )

        repository.saveActivity(activity)
        loadAllData()

        _currentDestination.value = NavigationDestination.ActivityDetail(activity.id)
    }

    fun dismissMilestoneAlert() {
        _newlyEarnedMilestonesAlert.value = emptyList()
    }

    fun updateActivity(updated: WorkoutActivity) {
        repository.updateActivity(updated)
        loadAllData()
    }

    fun deleteActivity(id: String) {
        repository.deleteActivity(id)
        loadAllData()
        _currentDestination.value = NavigationDestination.Feed
    }

    fun updateWeeklyGoal(goalKm: Double) {
        repository.updateWeeklyGoal(goalKm)
        _personalRecords.value = repository.getPersonalRecords()
    }

    private fun parsePaceToSec(paceStr: String): Long {
        val parts = paceStr.split(":")
        if (parts.size == 2) {
            val m = parts[0].toLongOrNull() ?: 0
            val s = parts[1].toLongOrNull() ?: 0
            return m * 60 + s
        }
        return 0
    }
}
