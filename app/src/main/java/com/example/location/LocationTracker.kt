package com.example.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import com.example.model.LocationPoint
import com.example.model.SplitInfo
import com.example.model.WorkoutType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

enum class TrackingState {
    IDLE,
    RECORDING,
    PAUSED,
    STOPPED
}

data class TrackingData(
    val state: TrackingState = TrackingState.IDLE,
    val workoutType: WorkoutType = WorkoutType.RUN,
    val elapsedSeconds: Long = 0L,
    val movingSeconds: Long = 0L,
    val totalDistanceMeters: Double = 0.0,
    val currentPaceMinKm: String = "--:--",
    val avgPaceMinKm: String = "--:--",
    val currentSpeedKmh: Double = 0.0,
    val maxSpeedKmh: Double = 0.0,
    val elevationGainMeters: Double = 0.0,
    val estimatedCalories: Int = 0,
    val routePoints: List<LocationPoint> = emptyList(),
    val splits: List<SplitInfo> = emptyList(),
    val lastUnlockedMilestoneTitle: String? = null,
    val isSimulationMode: Boolean = true
)

class LocationTracker(private val context: Context) : LocationListener {

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private val _trackingData = MutableStateFlow(TrackingData())
    val trackingData: StateFlow<TrackingData> = _trackingData.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Default + Job())
    private var timerJob: Job? = null
    private var simJob: Job? = null

    // Tracking state internals
    private var lastLocation: LocationPoint? = null
    private var currentSplitKmCounter = 1
    private var lastSplitDistanceMeters = 0.0
    private var lastSplitTimestampMs = 0L

    // Simulation route generator state
    private var simBaseLat = 37.7749
    private var simBaseLng = -122.4194
    private var simAngleRad = 0.0

    fun startTracking(type: WorkoutType, isSimulated: Boolean = true) {
        _trackingData.value = TrackingData(
            state = TrackingState.RECORDING,
            workoutType = type,
            isSimulationMode = isSimulated
        )

        lastLocation = null
        currentSplitKmCounter = 1
        lastSplitDistanceMeters = 0.0
        lastSplitTimestampMs = System.currentTimeMillis()

        // Setup base simulation location
        simBaseLat = 37.7749 + (Math.random() - 0.5) * 0.01
        simBaseLng = -122.4194 + (Math.random() - 0.5) * 0.01
        simAngleRad = Math.random() * Math.PI * 2

        if (!isSimulated) {
            registerLocationUpdates()
        }

        startTimerAndSimulation(isSimulated)
    }

    fun pauseTracking() {
        if (_trackingData.value.state == TrackingState.RECORDING) {
            _trackingData.value = _trackingData.value.copy(state = TrackingState.PAUSED)
        }
    }

    fun resumeTracking() {
        if (_trackingData.value.state == TrackingState.PAUSED) {
            _trackingData.value = _trackingData.value.copy(state = TrackingState.RECORDING)
        }
    }

    fun stopTracking(): TrackingData {
        unregisterLocationUpdates()
        timerJob?.cancel()
        simJob?.cancel()
        val finalData = _trackingData.value.copy(state = TrackingState.STOPPED)
        _trackingData.value = finalData
        return finalData
    }

    fun toggleSimulationMode(enableSim: Boolean) {
        _trackingData.value = _trackingData.value.copy(isSimulationMode = enableSim)
        if (enableSim) {
            unregisterLocationUpdates()
        } else if (_trackingData.value.state == TrackingState.RECORDING) {
            registerLocationUpdates()
        }
    }

    fun clearLastMilestoneAlert() {
        _trackingData.value = _trackingData.value.copy(lastUnlockedMilestoneTitle = null)
    }

    private fun startTimerAndSimulation(isSimulated: Boolean) {
        timerJob?.cancel()
        simJob?.cancel()

        timerJob = scope.launch {
            while (true) {
                delay(1000L)
                val cur = _trackingData.value
                if (cur.state == TrackingState.RECORDING) {
                    val newElapsed = cur.elapsedSeconds + 1
                    val newMoving = cur.movingSeconds + 1
                    _trackingData.value = cur.copy(
                        elapsedSeconds = newElapsed,
                        movingSeconds = newMoving
                    )
                }
            }
        }

        simJob = scope.launch {
            while (true) {
                delay(2000L)
                val cur = _trackingData.value
                if (cur.state == TrackingState.RECORDING && cur.isSimulationMode) {
                    simulateGPSStep()
                }
            }
        }
    }

    private fun simulateGPSStep() {
        val curData = _trackingData.value
        val speedMps = when (curData.workoutType) {
            WorkoutType.RUN -> 3.2 + (Math.random() - 0.5) * 0.4 // ~11.5 km/h
            WorkoutType.CYCLING -> 7.5 + (Math.random() - 0.5) * 1.0 // ~27 km/h
            WorkoutType.WALK -> 1.4 + (Math.random() - 0.5) * 0.2 // ~5 km/h
            WorkoutType.HIKE -> 1.2 + (Math.random() - 0.5) * 0.3
        }

        // Advance simulated position
        simAngleRad += (Math.random() - 0.48) * 0.3
        val distanceStep = speedMps * 2.0 // 2 second interval
        val latOffset = (distanceStep * cos(simAngleRad)) / 111111.0
        val lngOffset = (distanceStep * sin(simAngleRad)) / (111111.0 * cos(Math.toRadians(simBaseLat)))

        simBaseLat += latOffset
        simBaseLng += lngOffset

        val newPoint = LocationPoint(
            latitude = simBaseLat,
            longitude = simBaseLng,
            altitudeMeters = 20.0 + sin(simAngleRad * 2) * 8.0,
            timestampMs = System.currentTimeMillis(),
            speedMps = speedMps
        )

        processNewLocationPoint(newPoint)
    }

    private fun processNewLocationPoint(newPoint: LocationPoint) {
        val cur = _trackingData.value
        if (cur.state != TrackingState.RECORDING) return

        val prev = lastLocation
        lastLocation = newPoint

        val newPoints = cur.routePoints + newPoint

        if (prev == null) {
            _trackingData.value = cur.copy(routePoints = newPoints)
            return
        }

        val stepDistance = calculateHaversineDistanceMeters(
            prev.latitude, prev.longitude,
            newPoint.latitude, newPoint.longitude
        )

        val totalDist = cur.totalDistanceMeters + stepDistance
        val speedKmh = newPoint.speedMps * 3.6
        val newMaxSpeed = maxOf(cur.maxSpeedKmh, speedKmh)

        var elevGain = cur.elevationGainMeters
        if (newPoint.altitudeMeters > prev.altitudeMeters) {
            elevGain += (newPoint.altitudeMeters - prev.altitudeMeters)
        }

        val cals = ((totalDist / 1000.0) * cur.workoutType.caloriesPerKm).toInt()

        // Calculate Paces
        val curPaceMinKm = formatSpeedToPace(newPoint.speedMps)
        val avgPaceMinKm = if (totalDist > 10.0 && cur.movingSeconds > 0) {
            val paceSecPerKm = (cur.movingSeconds / (totalDist / 1000.0)).toLong()
            formatSecondsToPace(paceSecPerKm)
        } else {
            "--:--"
        }

        // Check 1.0 km Auto Split Trigger
        var updatedSplits = cur.splits
        if (totalDist - lastSplitDistanceMeters >= 1000.0) {
            val splitDistKm = 1.0
            val nowMs = System.currentTimeMillis()
            val splitDurationSec = maxOf(1L, (nowMs - lastSplitTimestampMs) / 1000L)
            val splitPaceStr = formatSecondsToPace(splitDurationSec)

            val newSplit = SplitInfo(
                splitNumber = currentSplitKmCounter,
                durationSeconds = splitDurationSec,
                distanceKm = splitDistKm,
                avgPaceMinKm = splitPaceStr,
                elevationGainMeters = elevGain
            )

            updatedSplits = cur.splits + newSplit
            currentSplitKmCounter++
            lastSplitDistanceMeters = totalDist
            lastSplitTimestampMs = nowMs
        }

        _trackingData.value = cur.copy(
            totalDistanceMeters = totalDist,
            currentPaceMinKm = curPaceMinKm,
            avgPaceMinKm = avgPaceMinKm,
            currentSpeedKmh = speedKmh,
            maxSpeedKmh = newMaxSpeed,
            elevationGainMeters = elevGain,
            estimatedCalories = cals,
            routePoints = newPoints,
            splits = updatedSplits
        )
    }

    @SuppressLint("MissingPermission")
    private fun registerLocationUpdates() {
        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    2000L,
                    3.0f,
                    this
                )
            } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    2000L,
                    3.0f,
                    this
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun unregisterLocationUpdates() {
        try {
            locationManager.removeUpdates(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onLocationChanged(location: Location) {
        if (!_trackingData.value.isSimulationMode) {
            val point = LocationPoint(
                latitude = location.latitude,
                longitude = location.longitude,
                altitudeMeters = location.altitude,
                timestampMs = location.time,
                speedMps = location.speed.toDouble()
            )
            processNewLocationPoint(point)
        }
    }

    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}

    companion object {
        fun calculateHaversineDistanceMeters(
            lat1: Double, lon1: Double,
            lat2: Double, lon2: Double
        ): Double {
            val R = 6371000.0 // Earth radius in meters
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)
            val a = sin(dLat / 2) * sin(dLat / 2) +
                    cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                    sin(dLon / 2) * sin(dLon / 2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            return R * c
        }

        fun formatSecondsToPace(secondsPerKm: Long): String {
            if (secondsPerKm <= 0 || secondsPerKm > 3600) return "--:--"
            val m = secondsPerKm / 60
            val s = secondsPerKm % 60
            return String.format("%d:%02d", m, s)
        }

        fun formatSpeedToPace(speedMps: Double): String {
            if (speedMps <= 0.3) return "--:--"
            val paceSecPerKm = (1000.0 / speedMps).toLong()
            return formatSecondsToPace(paceSecPerKm)
        }

        fun formatDuration(seconds: Long): String {
            val hrs = seconds / 3600
            val mins = (seconds % 3600) / 60
            val secs = seconds % 60
            return if (hrs > 0) {
                String.format("%d:%02d:%02d", hrs, mins, secs)
            } else {
                String.format("%02d:%02d", mins, secs)
            }
        }
    }
}
