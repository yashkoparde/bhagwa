package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.OutlinedFlag
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.location.LocationTracker
import com.example.ui.NavigationDestination
import com.example.ui.WorkoutViewModel
import com.example.ui.components.RouteMapCanvas
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GoldStar
import com.example.ui.theme.StravaOrange
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.ui.components.ShareableWorkoutCardModal

@Composable
fun ActivityDetailScreen(
    activityId: String,
    viewModel: WorkoutViewModel
) {
    val activities by viewModel.activities.collectAsState()
    val milestones by viewModel.milestones.collectAsState()
    val activity = activities.find { it.id == activityId }

    var showShareModal by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    if (activity == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Activity not found", color = Color.White)
        }
        return
    }

    var editTitle by remember(activity.id, activity.title) { mutableStateOf(activity.title) }
    var editNotes by remember(activity.id, activity.notes) { mutableStateOf(activity.notes) }
    var editEffort by remember(activity.id, activity.effortRating) { mutableStateOf(activity.effortRating.toFloat()) }

    val formattedDate = remember(activity.timestampMs) {
        val sdf = SimpleDateFormat("EEEE, MMMM d, yyyy • h:mm a", Locale.getDefault())
        sdf.format(Date(activity.timestampMs))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("activity_detail_screen")
    ) {
        // Navigation Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.navigateTo(NavigationDestination.Feed) },
                modifier = Modifier.testTag("detail_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Text(
                text = activity.type.displayName.uppercase() + " ANALYSIS",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = StravaOrange
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        editTitle = activity.title
                        editNotes = activity.notes
                        editEffort = activity.effortRating.toFloat()
                        showEditDialog = true
                    },
                    modifier = Modifier.testTag("detail_edit_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Workout",
                        tint = StravaOrange
                    )
                }

                IconButton(
                    onClick = { showShareModal = true },
                    modifier = Modifier.testTag("detail_share_card_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share Card",
                        tint = GoldStar
                    )
                }

                IconButton(
                    onClick = { viewModel.deleteActivity(activity.id) },
                    modifier = Modifier.testTag("detail_delete_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFFF5252)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Activity Title & Date with Share & Edit Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity.title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        editTitle = activity.title
                        editNotes = activity.notes
                        editEffort = activity.effortRating.toFloat()
                        showEditDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("edit_workout_title_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Button(
                    onClick = { showShareModal = true },
                    colors = ButtonDefaults.buttonColors(containerColor = StravaOrange),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("share_card_hero_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share Card", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // Edit Activity Dialog
        if (showEditDialog) {
            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                containerColor = DarkSurface,
                title = {
                    Text(
                        text = "Edit Workout Details",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        OutlinedTextField(
                            value = editTitle,
                            onValueChange = { editTitle = it },
                            label = { Text("Workout Name / Title") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = StravaOrange,
                                focusedLabelColor = StravaOrange,
                                unfocusedTextColor = Color.White,
                                focusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("edit_activity_title_field")
                        )

                        OutlinedTextField(
                            value = editNotes,
                            onValueChange = { editNotes = it },
                            label = { Text("Description / Notes") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = StravaOrange,
                                focusedLabelColor = StravaOrange,
                                unfocusedTextColor = Color.White,
                                focusedTextColor = Color.White
                            ),
                            minLines = 2,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("edit_activity_notes_field")
                        )

                        Column {
                            Text(
                                text = "Perceived Effort: ${editEffort.toInt()}/10",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.LightGray
                            )
                            Slider(
                                value = editEffort,
                                onValueChange = { editEffort = it },
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
                        onClick = {
                            val updated = activity.copy(
                                title = editTitle.ifBlank { "Untitled Workout" },
                                notes = editNotes,
                                effortRating = editEffort.toInt()
                            )
                            viewModel.updateActivity(updated)
                            showEditDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StravaOrange),
                        modifier = Modifier.testTag("save_edited_activity_button")
                    ) {
                        Text("Save Changes", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditDialog = false }) {
                        Text("Cancel", color = Color.Gray)
                    }
                }
            )
        }

        if (showShareModal) {
            ShareableWorkoutCardModal(
                activity = activity,
                onDismiss = { showShareModal = false }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Route Map
        RouteMapCanvas(
            routePoints = activity.routePoints,
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            showElevationProfile = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Main Stat Grid Cards
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Top Distance & Time Big Block
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("DISTANCE", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text(
                            text = String.format("%.2f km", activity.distanceMeters / 1000.0),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = StravaOrange
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("MOVING TIME", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text(
                            text = LocationTracker.formatDuration(activity.movingTimeSeconds),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Grid 2x2 of Pace, Elevation, Max Speed, Calories
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DetailMetricItem("Avg Pace", "${activity.avgPaceMinKm} /km", Icons.Default.OutlinedFlag)
                    DetailMetricItem("Elev Gain", String.format("%.0f m", activity.elevationGainMeters), Icons.Default.Landscape)
                    DetailMetricItem("Max Speed", String.format("%.1f km/h", activity.maxSpeedKmh), Icons.Default.Speed)
                    DetailMetricItem("Calories", "${activity.calories} kcal", Icons.Default.LocalFireDepartment)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Splits Table Section
        if (activity.splits.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SPLIT TIMES (1 KM BENCHMARKS)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Split Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("KM", style = MaterialTheme.typography.labelSmall, color = Color.Gray, modifier = Modifier.weight(1f))
                        Text("PACE", style = MaterialTheme.typography.labelSmall, color = Color.Gray, modifier = Modifier.weight(1.5f))
                        Text("TIME", style = MaterialTheme.typography.labelSmall, color = Color.Gray, modifier = Modifier.weight(1.5f))
                        Text("ELEV", style = MaterialTheme.typography.labelSmall, color = Color.Gray, modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    activity.splits.forEach { split ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${split.splitNumber}",
                                fontWeight = FontWeight.Bold,
                                color = StravaOrange,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${split.avgPaceMinKm} /km",
                                color = Color.White,
                                modifier = Modifier.weight(1.5f)
                            )
                            Text(
                                text = LocationTracker.formatDuration(split.durationSeconds),
                                color = Color.LightGray,
                                modifier = Modifier.weight(1.5f)
                            )
                            Text(
                                text = String.format("+%.0fm", split.elevationGainMeters),
                                color = GoldStar,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Earned Milestones Section
        if (activity.earnedMilestones.isNotEmpty()) {
            val earnedList = milestones.filter { activity.earnedMilestones.contains(it.id) }
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Trophy",
                            tint = GoldStar,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "MILESTONES UNLOCKED IN THIS WORKOUT",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = GoldStar
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    earnedList.forEach { m ->
                        Surface(
                            color = DarkSurfaceVariant,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = m.title,
                                    tint = GoldStar,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = m.title, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text(text = m.description, fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Notes & Effort Rating
        if (activity.notes.isNotBlank()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "NOTES", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = activity.notes, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
fun DetailMetricItem(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = StravaOrange,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 10.sp)
        }
        Text(value, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
    }
}
