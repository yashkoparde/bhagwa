package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Milestone
import com.example.ui.NavigationDestination
import com.example.ui.WorkoutViewModel
import com.example.ui.screens.ActivityDetailScreen
import com.example.ui.screens.ActivityFeedScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.MilestonesScreen
import com.example.ui.screens.TrackWorkoutScreen
import com.example.ui.theme.BhagwaTheme
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GoldStar
import com.example.ui.theme.StravaOrange

class MainActivity : ComponentActivity() {

    private val viewModel: WorkoutViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            BhagwaTheme {
                MainAppStructure(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppStructure(viewModel: WorkoutViewModel) {
    val destination by viewModel.currentDestination.collectAsState()
    val newlyEarnedMilestones by viewModel.newlyEarnedMilestonesAlert.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFF121212),
        bottomBar = {
            BhagwaBottomNavigationBar(
                currentDestination = destination,
                onNavigate = { viewModel.navigateTo(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val dest = destination) {
                NavigationDestination.Feed -> ActivityFeedScreen(viewModel = viewModel)
                NavigationDestination.Record -> TrackWorkoutScreen(viewModel = viewModel)
                NavigationDestination.Milestones -> MilestonesScreen(viewModel = viewModel)
                NavigationDestination.Dashboard -> DashboardScreen(viewModel = viewModel)
                is NavigationDestination.ActivityDetail -> ActivityDetailScreen(
                    activityId = dest.activityId,
                    viewModel = viewModel
                )
            }

            // Milestone Achievement Celebration Dialog Overlay
            if (newlyEarnedMilestones.isNotEmpty()) {
                MilestoneAchievementDialog(
                    milestones = newlyEarnedMilestones,
                    onDismiss = { viewModel.dismissMilestoneAlert() }
                )
            }
        }
    }
}

@Composable
fun BhagwaBottomNavigationBar(
    currentDestination: NavigationDestination,
    onNavigate: (NavigationDestination) -> Unit
) {
    NavigationBar(
        containerColor = DarkSurface,
        tonalElevation = 8.dp,
        modifier = Modifier.testTag("bhagwa_bottom_nav_bar")
    ) {
        // Feed Tab
        NavigationBarItem(
            selected = currentDestination is NavigationDestination.Feed,
            onClick = { onNavigate(NavigationDestination.Feed) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Feed") },
            label = { Text("Feed", fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = StravaOrange,
                selectedTextColor = StravaOrange,
                indicatorColor = StravaOrange.copy(alpha = 0.15f),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            ),
            modifier = Modifier.testTag("nav_item_feed")
        )

        // Record GPS Tab
        NavigationBarItem(
            selected = currentDestination is NavigationDestination.Record,
            onClick = { onNavigate(NavigationDestination.Record) },
            icon = { Icon(Icons.Default.RadioButtonChecked, contentDescription = "Record", modifier = Modifier.size(26.dp)) },
            label = { Text("Record", fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = StravaOrange,
                selectedTextColor = StravaOrange,
                indicatorColor = StravaOrange.copy(alpha = 0.25f),
                unselectedIconColor = StravaOrange,
                unselectedTextColor = StravaOrange
            ),
            modifier = Modifier.testTag("nav_item_record")
        )

        // Trophies / Milestones Tab
        NavigationBarItem(
            selected = currentDestination is NavigationDestination.Milestones,
            onClick = { onNavigate(NavigationDestination.Milestones) },
            icon = { Icon(Icons.Default.EmojiEvents, contentDescription = "Milestones") },
            label = { Text("Badges", fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = GoldStar,
                selectedTextColor = GoldStar,
                indicatorColor = GoldStar.copy(alpha = 0.15f),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            ),
            modifier = Modifier.testTag("nav_item_milestones")
        )

        // Stats Dashboard Tab
        NavigationBarItem(
            selected = currentDestination is NavigationDestination.Dashboard,
            onClick = { onNavigate(NavigationDestination.Dashboard) },
            icon = { Icon(Icons.Default.BarChart, contentDescription = "Stats") },
            label = { Text("Stats", fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = StravaOrange,
                selectedTextColor = StravaOrange,
                indicatorColor = StravaOrange.copy(alpha = 0.15f),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            ),
            modifier = Modifier.testTag("nav_item_dashboard")
        )
    }
}

@Composable
fun MilestoneAchievementDialog(
    milestones: List<Milestone>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "Trophy",
                    tint = GoldStar,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "NEW MILESTONE UNLOCKED! 🏆",
                    color = GoldStar,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                milestones.forEach { m ->
                    Surface(
                        color = Color(0xFF282828),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = m.title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                            Text(text = m.description, color = Color.LightGray, fontSize = 13.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = StravaOrange),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dismiss_milestone_dialog_button")
            ) {
                Text("CLAIM BADGES!", fontWeight = FontWeight.Bold)
            }
        }
    )
}

