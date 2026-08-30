package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.OutlinedFlag
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.location.LocationTracker
import com.example.location.TrackingState
import com.example.model.WorkoutType
import com.example.ui.WorkoutViewModel
import com.example.ui.components.RouteMapCanvas
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GoldStar
import com.example.ui.theme.StravaOrange

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.ui.components.ShareableWorkoutCardModal

@Composable
fun TrackWorkoutScreen(
    viewModel: WorkoutViewModel
) {
    val context = LocalContext.current
    val trackingData by viewModel.trackingData.collectAsState()
    var selectedType by remember { mutableStateOf(WorkoutType.RUN) }
    var showSaveDialog by remember { mutableStateOf(false) }

    // State for newly saved workout card preview
    val activities by viewModel.activities.collectAsState()
    var newlySavedActivityId by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            viewModel.toggleSimulationMode(false)
        } else {
            // Keep simulation mode if denied
            viewModel.toggleSimulationMode(true)
        }
    }

    fun requestLocationPermissionAndStart(isSimulated: Boolean) {
        if (!isSimulated) {
            val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            if (!hasFine && !hasCoarse) {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            } else {
                viewModel.toggleSimulationMode(false)
            }
        } else {
            viewModel.toggleSimulationMode(true)
        }
    }

    val state = trackingData.state

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("track_workout_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header: Simulation Mode Switch & Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.GpsFixed,
                        contentDescription = "GPS Tracker",
                        tint = StravaOrange,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "BHAGWA GPS RECORD",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // GPS Real vs Simulation Toggle
                Surface(
                    color = DarkSurfaceVariant,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.padding(2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (trackingData.isSimulationMode) "GPS Sim" else "Real GPS",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (trackingData.isSimulationMode) GoldStar else Color.LightGray
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Switch(
                            checked = trackingData.isSimulationMode,
                            onCheckedChange = { enableSim ->
                                if (!enableSim) {
                                    requestLocationPermissionAndStart(isSimulated = false)
                                } else {
                                    viewModel.toggleSimulationMode(true)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = StravaOrange,
                                checkedTrackColor = DarkSurface
                            ),
                            modifier = Modifier.testTag("gps_simulation_switch")
                        )
                    }
                }
            }

            // Sport Selection Bar (When Idle)
            if (state == TrackingState.IDLE) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WorkoutType.entries.forEach { type ->
                        val isSelected = selectedType == type
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedType = type }
                                .testTag("workout_type_chip_${type.name}"),
                            color = if (isSelected) StravaOrange else DarkSurfaceVariant,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = type.getIcon(),
                                    contentDescription = type.displayName,
                                    tint = if (isSelected) Color.White else Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = type.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else Color.Gray
                                )
                            }
                        }
                    }
                }
            }

            // Map View Canvas
            RouteMapCanvas(
                routePoints = trackingData.routePoints,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                isLiveTracking = state == TrackingState.RECORDING
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Main Metrics HUD
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Big Distance Number
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val distanceKm = trackingData.totalDistanceMeters / 1000.0
                        Text(
                            text = String.format("%.2f", distanceKm),
                            fontSize = 54.sp,
                            fontWeight = FontWeight.Black,
                            color = StravaOrange,
                            letterSpacing = (-1.5).sp
                        )
                        Text(
                            text = "KILOMETERS",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.LightGray
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Secondary Metrics Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        MetricBlock(
                            label = "TIME",
                            value = LocationTracker.formatDuration(trackingData.elapsedSeconds),
                            icon = Icons.Default.Timer
                        )
                        MetricBlock(
                            label = "AVG PACE",
                            value = trackingData.avgPaceMinKm + " /km",
                            icon = Icons.Default.OutlinedFlag
                        )
                        MetricBlock(
                            label = "ELEVATION",
                            value = String.format("%.0fm", trackingData.elevationGainMeters),
                            icon = Icons.Default.Landscape
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Tracking Controls Action Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                when (state) {
                    TrackingState.IDLE -> {
                        Button(
                            onClick = {
                                if (!trackingData.isSimulationMode) {
                                    requestLocationPermissionAndStart(isSimulated = false)
                                }
                                viewModel.startWorkout(selectedType, trackingData.isSimulationMode)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StravaOrange),
                            shape = CircleShape,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("start_workout_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Start",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "START ${selectedType.displayName.uppercase()}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    TrackingState.RECORDING -> {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { viewModel.pauseWorkout() },
                                colors = ButtonDefaults.buttonColors(containerColor = GoldStar),
                                shape = CircleShape,
                                modifier = Modifier
                                    .size(72.dp)
                                    .testTag("pause_workout_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Pause,
                                    contentDescription = "Pause",
                                    tint = Color.Black,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }

                    TrackingState.PAUSED -> {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Resume Button
                            Button(
                                onClick = { viewModel.resumeWorkout() },
                                colors = ButtonDefaults.buttonColors(containerColor = StravaOrange),
                                shape = CircleShape,
                                modifier = Modifier
                                    .size(68.dp)
                                    .testTag("resume_workout_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Resume",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            // Finish Button
                            Button(
                                onClick = { showSaveDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                                shape = CircleShape,
                                modifier = Modifier
                                    .size(68.dp)
                                    .testTag("finish_workout_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = "Stop",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }

                    TrackingState.STOPPED -> {}
                }
            }
        }

        // Live Auto-Split Toast Banner
        val lastSplit = trackingData.splits.lastOrNull()
        if (lastSplit != null && state == TrackingState.RECORDING) {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 70.dp)
            ) {
                Surface(
                    color = StravaOrange,
                    shape = RoundedCornerShape(24.dp),
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.OutlinedFlag,
                            contentDescription = "Split",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Split ${lastSplit.splitNumber} (1 km): ${lastSplit.avgPaceMinKm} /km",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // Post Workout Save Dialog Modal
        if (showSaveDialog) {
            SaveWorkoutModal(
                workoutType = trackingData.workoutType,
                distanceMeters = trackingData.totalDistanceMeters,
                durationSeconds = trackingData.elapsedSeconds,
                avgPace = trackingData.avgPaceMinKm,
                onDismiss = { showSaveDialog = false },
                onSave = { title, notes, effort ->
                    showSaveDialog = false
                    viewModel.saveCompletedWorkout(title, notes, effort)
                    val lastSaved = activities.firstOrNull()
                    if (lastSaved != null) {
                        newlySavedActivityId = lastSaved.id
                    }
                }
            )
        }

        // Auto Pop-up Shareable Workout Card upon saving workout
        val savedActivity = activities.find { it.id == newlySavedActivityId }
        if (savedActivity != null) {
            ShareableWorkoutCardModal(
                activity = savedActivity,
                onDismiss = { newlySavedActivityId = null }
            )
        }
    }
}

@Composable
fun MetricBlock(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = StravaOrange,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                fontSize = 10.sp
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun SaveWorkoutModal(
    workoutType: WorkoutType,
    distanceMeters: Double,
    durationSeconds: Long,
    avgPace: String,
    onDismiss: () -> Unit,
    onSave: (title: String, notes: String, effortRating: Int) -> Unit
) {
    var title by remember { mutableStateOf("${workoutType.displayName} Session") }
    var notes by remember { mutableStateOf("") }
    var effort by remember { mutableFloatStateOf(5f) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "Save Workout",
                    tint = StravaOrange,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save Workout",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Workout summary stats preview card
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = String.format("%.2f km", distanceMeters / 1000.0),
                            fontWeight = FontWeight.Bold,
                            color = StravaOrange
                        )
                        Text(
                            text = LocationTracker.formatDuration(durationSeconds),
                            color = Color.White
                        )
                        Text(
                            text = "$avgPace /km",
                            color = GoldStar
                        )
                    }
                }

                // Activity Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Activity Title", color = Color.Gray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StravaOrange,
                        unfocusedBorderColor = Color.DarkGray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_workout_title_input")
                )

                // Notes / Thoughts
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes & How you felt", color = Color.Gray) },
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StravaOrange,
                        unfocusedBorderColor = Color.DarkGray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Effort Rating Slider (1 to 10)
                Column {
                    Text(
                        text = "Perceived Effort (RPE 1-10): ${effort.toInt()}",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Slider(
                        value = effort,
                        onValueChange = { effort = it },
                        valueRange = 1f..10f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = StravaOrange,
                            activeTrackColor = StravaOrange
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(title, notes, effort.toInt()) },
                colors = ButtonDefaults.buttonColors(containerColor = StravaOrange),
                modifier = Modifier.testTag("confirm_save_workout_button")
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = "Save")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Save Workout", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}
