package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.location.LocationTracker
import com.example.model.WorkoutType
import com.example.ui.WorkoutViewModel
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GoldStar
import com.example.ui.theme.StravaOrange

@Composable
fun DashboardScreen(
    viewModel: WorkoutViewModel
) {
    val personalRecords by viewModel.personalRecords.collectAsState()
    val activities by viewModel.activities.collectAsState()

    var goalSliderVal by remember(personalRecords.weeklyGoalKm) {
        mutableFloatStateOf(personalRecords.weeklyGoalKm.toFloat())
    }

    val totalKm = personalRecords.totalDistanceMeters / 1000.0

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("dashboard_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Text(
                    text = "STATS & RECORDS",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }

            // Weekly Distance Target Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "WEEKLY DISTANCE GOAL",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Text(
                                text = "${String.format("%.1f", totalKm)} / ${goalSliderVal.toInt()} KM",
                                fontWeight = FontWeight.Bold,
                                color = StravaOrange,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Goal Slider
                        Text(
                            text = "Set Target: ${goalSliderVal.toInt()} km / week",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                        Slider(
                            value = goalSliderVal,
                            onValueChange = {
                                goalSliderVal = it
                                viewModel.updateWeeklyGoal(it.toDouble())
                            },
                            valueRange = 5f..100f,
                            steps = 18,
                            colors = SliderDefaults.colors(
                                thumbColor = StravaOrange,
                                activeTrackColor = StravaOrange
                            ),
                            modifier = Modifier.testTag("weekly_goal_slider")
                        )
                    }
                }
            }

            // Personal Records Trophies Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "PRs",
                                tint = GoldStar,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "PERSONAL RECORDS (PRs)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = GoldStar
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        PrRowItem(
                            title = "Fastest 1 Km",
                            value = if (personalRecords.fastest1kSec < Long.MAX_VALUE) LocationTracker.formatDuration(personalRecords.fastest1kSec) + " /km" else "Not set",
                            icon = Icons.Default.Speed
                        )

                        PrRowItem(
                            title = "Fastest 5 Km",
                            value = if (personalRecords.fastest5kSec < Long.MAX_VALUE) LocationTracker.formatDuration(personalRecords.fastest5kSec) else "Not set",
                            icon = Icons.Default.Star
                        )

                        PrRowItem(
                            title = "Fastest 10 Km",
                            value = if (personalRecords.fastest10kSec < Long.MAX_VALUE) LocationTracker.formatDuration(personalRecords.fastest10kSec) else "Not set",
                            icon = Icons.Default.EmojiEvents
                        )

                        PrRowItem(
                            title = "Longest Distance",
                            value = String.format("%.2f km", personalRecords.longestDistanceMeters / 1000.0),
                            icon = Icons.Default.DirectionsRun
                        )

                        PrRowItem(
                            title = "Max Elevation",
                            value = String.format("%.0f m", personalRecords.maxElevationGainMeters),
                            icon = Icons.Default.Landscape
                        )
                    }
                }
            }

            // Lifetime Stats Summary
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "LIFETIME TOTALS",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("TOTAL DISTANCE", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                Text(
                                    text = String.format("%.1f km", totalKm),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    color = StravaOrange
                                )
                            }

                            Column {
                                Text("ACTIVITIES", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                Text(
                                    text = "${personalRecords.totalActivitiesCount}",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }

                            Column {
                                Text("CALORIES", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                val estCals = (totalKm * 55).toInt()
                                Text(
                                    text = "$estCals kcal",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    color = GoldStar
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PrRowItem(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Surface(
        color = DarkSurfaceVariant,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = StravaOrange,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 14.sp
                )
            }

            Text(
                text = value,
                fontWeight = FontWeight.Black,
                color = GoldStar,
                fontSize = 15.sp
            )
        }
    }
}
