package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import com.example.model.Milestone
import com.example.model.MilestoneCategory
import com.example.ui.WorkoutViewModel
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GoldStar
import com.example.ui.theme.StravaOrange
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MilestonesScreen(
    viewModel: WorkoutViewModel
) {
    val milestones by viewModel.milestones.collectAsState()
    val personalRecords by viewModel.personalRecords.collectAsState()

    var selectedCategory by remember { mutableStateOf<MilestoneCategory?>(null) }

    val filteredList = remember(milestones, selectedCategory) {
        if (selectedCategory == null) milestones else milestones.filter { it.category == selectedCategory }
    }

    val unlockedCount = milestones.count { it.isUnlocked }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("milestones_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Trophy Cabinet Summary Banner
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = "Trophies",
                                    tint = GoldStar,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "TROPHY CABINET",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "$unlockedCount of ${milestones.size} Milestones Unlocked",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val progressFraction = if (milestones.isNotEmpty()) unlockedCount.toFloat() / milestones.size.toFloat() else 0f
                        LinearProgressIndicator(
                            progress = { progressFraction },
                            color = GoldStar,
                            trackColor = DarkSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                    }
                }
            }

            // Category Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        Surface(
                            color = if (selectedCategory == null) StravaOrange else DarkSurfaceVariant,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { selectedCategory = null }
                                .testTag("filter_all_milestones")
                        ) {
                            Text(
                                text = "All Badges",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                fontSize = 13.sp
                            )
                        }
                    }

                    items(MilestoneCategory.entries) { cat ->
                        val isSel = selectedCategory == cat
                        Surface(
                            color = if (isSel) StravaOrange else DarkSurfaceVariant,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { selectedCategory = cat }
                                .testTag("filter_category_${cat.name}")
                        ) {
                            Text(
                                text = cat.label,
                                color = if (isSel) Color.White else Color.Gray,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // Milestones Grid Cards
            items(
                items = filteredList,
                key = { it.id }
            ) { m ->
                MilestoneCard(
                    milestone = m,
                    longestDistM = personalRecords.longestDistanceMeters,
                    totalCount = personalRecords.totalActivitiesCount
                )
            }
        }
    }
}

@Composable
fun MilestoneCard(
    milestone: Milestone,
    longestDistM: Double,
    totalCount: Int
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (milestone.isUnlocked) 1.5.dp else 0.dp,
                color = if (milestone.isUnlocked) GoldStar.copy(alpha = 0.8f) else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .testTag("milestone_card_${milestone.id}")
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Trophy / Badge Icon Shield
            Surface(
                color = if (milestone.isUnlocked) GoldStar.copy(alpha = 0.2f) else DarkSurfaceVariant,
                shape = CircleShape,
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (milestone.isUnlocked) Icons.Default.EmojiEvents else Icons.Default.Lock,
                        contentDescription = milestone.title,
                        tint = if (milestone.isUnlocked) GoldStar else Color.Gray,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = milestone.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    if (milestone.isUnlocked) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Unlocked",
                            tint = GoldStar,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Text(
                    text = milestone.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.LightGray
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (milestone.isUnlocked) {
                    val dateStr = milestone.unlockedDateMs?.let {
                        SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(it))
                    } ?: "Achieved!"
                    Text(
                        text = "Unlocked $dateStr",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = GoldStar
                    )
                } else {
                    // Calculate Progress
                    val currentVal = when (milestone.category) {
                        MilestoneCategory.DISTANCE -> longestDistM / 1000.0
                        MilestoneCategory.COUNT -> totalCount.toDouble()
                        else -> 0.0
                    }
                    val fraction = (currentVal / milestone.targetValue).coerceIn(0.0, 1.0).toFloat()

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Progress",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                            Text(
                                text = String.format("%.1f / %.1f %s", currentVal, milestone.targetValue, milestone.unit),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { fraction },
                            color = StravaOrange,
                            trackColor = DarkSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                    }
                }
            }
        }
    }
}
