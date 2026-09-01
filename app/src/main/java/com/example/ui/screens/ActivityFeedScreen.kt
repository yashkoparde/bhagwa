package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.OutlinedFlag
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.model.WorkoutActivity
import com.example.model.WorkoutType
import com.example.ui.NavigationDestination
import com.example.ui.WorkoutViewModel
import com.example.ui.components.RouteMapCanvas
import com.example.ui.components.ShareableWorkoutCardModal
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GoldStar
import com.example.ui.theme.StravaOrange
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ActivityFeedScreen(
    viewModel: WorkoutViewModel
) {
    val activities by viewModel.activities.collectAsState()
    val personalRecords by viewModel.personalRecords.collectAsState()
    var activityToShare by remember { mutableStateOf<WorkoutActivity?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("activity_feed_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Weekly Stats Summary
            item {
                WeeklySummaryHeader(
                    totalDistanceM = personalRecords.totalDistanceMeters,
                    totalCount = personalRecords.totalActivitiesCount,
                    weeklyGoalKm = personalRecords.weeklyGoalKm,
                    onStartClick = { viewModel.startWorkout(WorkoutType.RUN, isSimulated = true) }
                )
            }

            // Feed Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RECENT ACTIVITIES",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${activities.size} Logged",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }

            if (activities.isEmpty()) {
                item {
                    EmptyFeedState(
                        onStartClick = { viewModel.startWorkout(WorkoutType.RUN, isSimulated = true) }
                    )
                }
            } else {
                items(
                    items = activities,
                    key = { it.id }
                ) { activity ->
                    ActivityFeedCard(
                        activity = activity,
                        onClick = {
                            viewModel.navigateTo(NavigationDestination.ActivityDetail(activity.id))
                        },
                        onShareClick = {
                            activityToShare = activity
                        }
                    )
                }
            }
        }

        activityToShare?.let { currentActivity ->
            ShareableWorkoutCardModal(
                activity = currentActivity,
                onDismiss = { activityToShare = null }
            )
        }

        // Quick Record FAB
        FloatingActionButton(
            onClick = { viewModel.startWorkout(WorkoutType.RUN, isSimulated = true) },
            containerColor = StravaOrange,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_record_workout")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Record Workout",
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun WeeklySummaryHeader(
    totalDistanceM: Double,
    totalCount: Int,
    weeklyGoalKm: Double,
    onStartClick: () -> Unit
) {
    val totalKm = totalDistanceM / 1000.0

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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Streak",
                        tint = StravaOrange,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "WEEKLY DASHBOARD",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Surface(
                    color = StravaOrange.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "GOAL: ${weeklyGoalKm.toInt()} KM",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = StravaOrange,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = String.format("%.1f", totalKm),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "TOTAL KM LOGGED",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$totalCount",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black,
                        color = GoldStar
                    )
                    Text(
                        text = "WORKOUTS",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onStartClick,
                colors = ButtonDefaults.buttonColors(containerColor = StravaOrange),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("feed_record_now_button")
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsRun,
                    contentDescription = "Record",
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "RECORD NEW WORKOUT",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun ActivityFeedCard(
    activity: WorkoutActivity,
    onClick: () -> Unit,
    onShareClick: () -> Unit
) {
    val formattedDate = remember(activity.timestampMs) {
        val sdf = SimpleDateFormat("EEEE, MMM d • h:mm a", Locale.getDefault())
        sdf.format(Date(activity.timestampMs))
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("activity_card_${activity.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Title & Activity Type Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = StravaOrange,
                        shape = CircleShape,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = activity.type.getIcon(),
                                contentDescription = activity.type.displayName,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = activity.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Effort Badge
                    Surface(
                        color = DarkSurfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "RPE ${activity.effortRating}/10",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.LightGray,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Share Flex Card Button
                    Surface(
                        color = StravaOrange.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .clickable { onShareClick() }
                            .testTag("feed_share_flex_button_${activity.id}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Flex Card",
                                tint = StravaOrange,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Flex",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = StravaOrange
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Route Map Thumbnail
            if (activity.routePoints.isNotEmpty()) {
                RouteMapCanvas(
                    routePoints = activity.routePoints,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    showElevationProfile = false
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Stat Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FeedStatColumn("Distance", String.format("%.2f km", activity.distanceMeters / 1000.0), StravaOrange)
                FeedStatColumn("Pace", "${activity.avgPaceMinKm} /km", Color.White)
                FeedStatColumn("Time", LocationTracker.formatDuration(activity.durationSeconds), Color.White)
                FeedStatColumn("Elev", String.format("%.0fm", activity.elevationGainMeters), GoldStar)
            }

            // Earned Badges / Milestones tags
            if (activity.earnedMilestones.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Milestones",
                        tint = GoldStar,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${activity.earnedMilestones.size} Milestone Badges Earned!",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldStar
                    )
                }
            }
        }
    }
}

@Composable
fun FeedStatColumn(label: String, value: String, valueColor: Color) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray
        )
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

@Composable
fun EmptyFeedState(onStartClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.DirectionsRun,
                contentDescription = "Empty",
                tint = StravaOrange,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No Workouts Logged Yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Start tracking your runs, rides, and walks with real GPS route map recording!",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onStartClick,
                colors = ButtonDefaults.buttonColors(containerColor = StravaOrange)
            ) {
                Text("Start Your First Workout")
            }
        }
    }
}
