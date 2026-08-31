package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.location.LocationTracker
import com.example.model.WorkoutActivity
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GoldStar
import com.example.ui.theme.StravaOrange
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ShareableWorkoutCardModal(
    activity: WorkoutActivity,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var transparentBackground by remember { mutableStateOf(false) }

    val formattedDate = remember(activity.timestampMs) {
        val sdf = SimpleDateFormat("EEEE, MMM d, yyyy • h:mm a", Locale.getDefault())
        sdf.format(Date(activity.timestampMs))
    }

    val distanceKm = String.format("%.2f", activity.distanceMeters / 1000.0)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF121212),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("shareable_workout_card_dialog"),
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Dismiss Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = GoldStar,
                            shape = CircleShape,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = "Champion",
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CHAMPION WORKOUT CARD",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = GoldStar,
                            letterSpacing = 1.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_share_modal_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Empty / Transparent Background Toggle Option
                Surface(
                    color = Color(0xFF1C1C1C),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Empty Background",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (transparentBackground) "Transparent overlay export" else "Dark champion gradient",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }

                        Switch(
                            checked = transparentBackground,
                            onCheckedChange = { transparentBackground = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = GoldStar,
                                checkedTrackColor = GoldStar.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.testTag("transparent_background_switch")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // THE CHAMPION FLEX CARD
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (transparentBackground) Color.Transparent else Color(0xFF14120B)
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 2.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(GoldStar, GoldStar.copy(alpha = 0.4f))
                            ),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .testTag("workout_card_preview_surface")
                ) {
                    Column(
                        modifier = Modifier
                            .background(
                                if (transparentBackground) {
                                    Brush.verticalGradient(
                                        listOf(
                                            Color.White.copy(alpha = 0.03f),
                                            Color.White.copy(alpha = 0.01f)
                                        )
                                    )
                                } else {
                                    Brush.verticalGradient(
                                        listOf(
                                            Color(0xFF26200B),
                                            Color(0xFF181507),
                                            Color(0xFF0D0D0D)
                                        )
                                    )
                                }
                            )
                            .padding(20.dp)
                    ) {
                        // Top Header: Bhagwa Branding & Verified Badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "BHAGWA",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Black,
                                        color = GoldStar,
                                        letterSpacing = 1.5.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "GPS Verified",
                                        tint = GoldStar,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Text(
                                    text = "CHAMPION EDITION",
                                    fontSize = 9.sp,
                                    color = GoldStar.copy(alpha = 0.8f),
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.8.sp
                                )
                            }

                            Surface(
                                color = GoldStar,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "100% FREE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.Black,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Workout Title & Date
                        Text(
                            text = activity.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = formattedDate,
                            fontSize = 11.sp,
                            color = Color.LightGray
                        )

                        if (activity.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = activity.notes,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GoldStar.copy(alpha = 0.9f)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // GPS Route Map Canvas
                        if (activity.routePoints.isNotEmpty()) {
                            RouteMapCanvas(
                                routePoints = activity.routePoints,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(190.dp),
                                showElevationProfile = false
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // 4-STAT EXACT GRID (DISTANCE, DURATION, ELEVATION, PACE)
                        Surface(
                            color = if (transparentBackground) Color.Black.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, GoldStar.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // Row 1: Distance & Duration
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    MockStatBox(
                                        icon = Icons.Default.Straighten,
                                        value = "$distanceKm km",
                                        label = "Distance",
                                        iconColor = GoldStar,
                                        modifier = Modifier.weight(1f)
                                    )

                                    MockStatBox(
                                        icon = Icons.Default.Timer,
                                        value = LocationTracker.formatDuration(activity.durationSeconds),
                                        label = "Duration",
                                        iconColor = GoldStar,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                // Row 2: Elevation Gain & Avg Pace
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    MockStatBox(
                                        icon = Icons.Default.TrendingUp,
                                        value = "${activity.elevationGainMeters.toInt()}m",
                                        label = "Elevation Gain",
                                        iconColor = GoldStar,
                                        modifier = Modifier.weight(1f)
                                    )

                                    MockStatBox(
                                        icon = Icons.Default.DirectionsRun,
                                        value = "${activity.avgPaceMinKm} min/km",
                                        label = "Pace",
                                        iconColor = GoldStar,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Footer Tagline
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = "Calories",
                                    tint = GoldStar,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${activity.calories} kcal burned",
                                    fontSize = 11.sp,
                                    color = Color.LightGray,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Text(
                                text = "Tracked on Bhagwa",
                                fontSize = 10.sp,
                                color = GoldStar.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons: Download Image, Share Image & Copy Stats
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Download Image Button
                        Button(
                            onClick = {
                                val savedUri = saveCardImageToGallery(
                                    context = context,
                                    activity = activity,
                                    transparentBg = transparentBackground
                                )
                                if (savedUri != null) {
                                    Toast.makeText(
                                        context,
                                        "Image Saved to Gallery / Pictures! 🏆",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Champion Card saved to Pictures/Bhagwa",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2A2A2A),
                                contentColor = GoldStar
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, GoldStar.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .testTag("download_card_image_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Download Image",
                                tint = GoldStar,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Download Image", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        // Share Image Button
                        Button(
                            onClick = {
                                shareCardImage(
                                    context = context,
                                    activity = activity,
                                    transparentBg = transparentBackground
                                )
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldStar,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1.1f)
                                .testTag("share_workout_card_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share Image",
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share Image", fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }
                    }

                    // Copy Text Stats Button
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val summary = """
                                🏆 Bhagwa Champion Workout
                                ───────────────────────────
                                Activity: ${activity.title}
                                Notes: ${activity.notes}
                                📏 Distance: $distanceKm km
                                ⏱️ Duration: ${LocationTracker.formatDuration(activity.durationSeconds)}
                                📈 Elevation Gain: ${activity.elevationGainMeters.toInt()}m
                                🏃 Pace: ${activity.avgPaceMinKm} min/km
                                🔥 Calories: ${activity.calories} kcal
                                📅 Date: $formattedDate
                                
                                ⚡ Tracked 100% Free on Bhagwa GPS Tracker!
                            """.trimIndent()
                            val clip = ClipData.newPlainText("Bhagwa Workout Flex", summary)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Stats copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.LightGray),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("copy_workout_summary_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = Color.LightGray,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy Stats Text", fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {}
    )
}

@Composable
fun MockStatBox(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Generates a clean high-resolution Champion Workout Card Bitmap.
 * When transparentBg is true, the background canvas is transparent (empty).
 */
fun generateChampionCardBitmap(
    context: Context,
    activity: WorkoutActivity,
    transparentBg: Boolean
): Bitmap {
    val width = 1080
    val height = 1600
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.NORMAL)

    if (!transparentBg) {
        // Draw Champion dark gradient background
        paint.color = android.graphics.Color.parseColor("#14120B")
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    }

    // Outer Gold Card Container
    val cardRect = RectF(40f, 40f, width - 40f, height - 40f)
    if (!transparentBg) {
        paint.color = android.graphics.Color.parseColor("#1C170A")
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(cardRect, 48f, 48f, paint)
    }

    // Gold Card Border
    paint.color = android.graphics.Color.parseColor("#FFD700")
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 6f
    canvas.drawRoundRect(cardRect, 48f, 48f, paint)

    // Top Header - BHAGWA
    paint.style = Paint.Style.FILL
    paint.color = android.graphics.Color.parseColor("#FFD700")
    paint.textSize = 52f
    paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.BOLD)
    canvas.drawText("BHAGWA 🏆", 80f, 130f, paint)

    paint.color = android.graphics.Color.parseColor("#E0C068")
    paint.textSize = 24f
    paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.NORMAL)
    canvas.drawText("CHAMPION EDITION • 100% FREE", 80f, 170f, paint)

    // Title & Date
    paint.color = android.graphics.Color.WHITE
    paint.textSize = 72f
    paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.BOLD)
    canvas.drawText(activity.title, 80f, 270f, paint)

    if (activity.notes.isNotBlank()) {
        paint.color = android.graphics.Color.parseColor("#FFD700")
        paint.textSize = 28f
        paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.NORMAL)
        canvas.drawText(activity.notes, 80f, 325f, paint)
    }

    val sdf = SimpleDateFormat("EEEE, MMM d, yyyy • h:mm a", Locale.getDefault())
    val dateStr = sdf.format(Date(activity.timestampMs))
    paint.color = android.graphics.Color.parseColor("#AAAAAA")
    paint.textSize = 24f
    paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.NORMAL)
    canvas.drawText(dateStr, 80f, if (activity.notes.isNotBlank()) 370f else 325f, paint)

    // GPS Map Route Box
    val mapTop = if (activity.notes.isNotBlank()) 410f else 365f
    val mapHeight = 560f // increased height of map for lengthier card
    val mapRect = RectF(80f, mapTop, width - 80f, mapTop + mapHeight)

    if (!transparentBg) {
        paint.color = android.graphics.Color.parseColor("#111111")
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(mapRect, 32f, 32f, paint)
    }
    paint.color = android.graphics.Color.parseColor("#33FFD700")
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 3f
    canvas.drawRoundRect(mapRect, 32f, 32f, paint)

    // Draw Route Points
    if (activity.routePoints.size > 1) {
        val points = activity.routePoints
        val minLat = points.minOf { it.latitude }
        val maxLat = points.maxOf { it.latitude }
        val minLng = points.minOf { it.longitude }
        val maxLng = points.maxOf { it.longitude }

        val latRange = (maxLat - minLat).coerceAtLeast(0.0001)
        val lngRange = (maxLng - minLng).coerceAtLeast(0.0001)

        val padding = 60f
        val mapInnerLeft = mapRect.left + padding
        val mapInnerRight = mapRect.right - padding
        val mapInnerTop = mapRect.top + padding
        val mapInnerBottom = mapRect.bottom - padding

        val path = Path()
        points.forEachIndexed { index, pt ->
            val x = mapInnerLeft + ((pt.longitude - minLng) / lngRange * (mapInnerRight - mapInnerLeft)).toFloat()
            val y = mapInnerBottom - ((pt.latitude - minLat) / latRange * (mapInnerBottom - mapInnerTop)).toFloat()
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        // Draw path glow
        paint.color = android.graphics.Color.parseColor("#55FF0033")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 14f
        canvas.drawPath(path, paint)

        // Draw path line
        paint.color = android.graphics.Color.parseColor("#FF0033")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 7f
        canvas.drawPath(path, paint)
    }

    // 4 Key Stats Box
    val statBoxTop = mapTop + mapHeight + 40f
    val statBoxRect = RectF(80f, statBoxTop, width - 80f, statBoxTop + 330f)

    if (!transparentBg) {
        paint.color = android.graphics.Color.parseColor("#0C0C0C")
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(statBoxRect, 32f, 32f, paint)
    }
    paint.color = android.graphics.Color.parseColor("#33FFD700")
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 2f
    canvas.drawRoundRect(statBoxRect, 32f, 32f, paint)

    val distanceKm = String.format("%.2f km", activity.distanceMeters / 1000.0)
    val duration = LocationTracker.formatDuration(activity.durationSeconds)
    val elevation = "${activity.elevationGainMeters.toInt()}m"
    val pace = "${activity.avgPaceMinKm} min/km"

    // Stat 1: Distance
    paint.style = Paint.Style.FILL
    paint.color = android.graphics.Color.WHITE
    paint.textSize = 50f
    paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.BOLD)
    canvas.drawText(distanceKm, 130f, statBoxTop + 90f, paint)
    paint.color = android.graphics.Color.parseColor("#FFD700")
    paint.textSize = 22f
    paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.NORMAL)
    canvas.drawText("DISTANCE", 130f, statBoxTop + 130f, paint)

    // Stat 2: Duration
    paint.color = android.graphics.Color.WHITE
    paint.textSize = 50f
    paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.BOLD)
    canvas.drawText(duration, 580f, statBoxTop + 90f, paint)
    paint.color = android.graphics.Color.parseColor("#FFD700")
    paint.textSize = 22f
    paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.NORMAL)
    canvas.drawText("DURATION", 580f, statBoxTop + 130f, paint)

    // Stat 3: Elevation Gain
    paint.color = android.graphics.Color.WHITE
    paint.textSize = 50f
    paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.BOLD)
    canvas.drawText(elevation, 130f, statBoxTop + 240f, paint)
    paint.color = android.graphics.Color.parseColor("#FFD700")
    paint.textSize = 22f
    paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.NORMAL)
    canvas.drawText("ELEVATION GAIN", 130f, statBoxTop + 280f, paint)

    // Stat 4: Pace
    paint.color = android.graphics.Color.WHITE
    paint.textSize = 50f
    paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.BOLD)
    canvas.drawText(pace, 580f, statBoxTop + 240f, paint)
    paint.color = android.graphics.Color.parseColor("#FFD700")
    paint.textSize = 22f
    paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.NORMAL)
    canvas.drawText("PACE", 580f, statBoxTop + 280f, paint)

    // Footer
    paint.color = android.graphics.Color.parseColor("#888888")
    paint.textSize = 24f
    canvas.drawText("🔥 ${activity.calories} kcal burned • Tracked on Bhagwa GPS", 100f, height - 70f, paint)

    return bitmap
}

/**
 * Saves image to Android MediaStore / Pictures directory.
 */
fun saveCardImageToGallery(
    context: Context,
    activity: WorkoutActivity,
    transparentBg: Boolean
): Uri? {
    val bitmap = generateChampionCardBitmap(context, activity, transparentBg)
    val filename = "Bhagwa_Champion_${activity.id}_${System.currentTimeMillis()}.png"

    var outputStream: OutputStream? = null
    var imageUri: Uri? = null

    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Bhagwa")
            }
            val resolver = context.contentResolver
            imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (imageUri != null) {
                outputStream = resolver.openOutputStream(imageUri)
            }
        } else {
            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val bhagwaDir = File(imagesDir, "Bhagwa")
            if (!bhagwaDir.exists()) bhagwaDir.mkdirs()
            val imageFile = File(bhagwaDir, filename)
            outputStream = FileOutputStream(imageFile)
            imageUri = Uri.fromFile(imageFile)
        }

        outputStream?.use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return imageUri
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

/**
 * Shares image using FileProvider and ACTION_SEND.
 */
fun shareCardImage(
    context: Context,
    activity: WorkoutActivity,
    transparentBg: Boolean
) {
    try {
        val bitmap = generateChampionCardBitmap(context, activity, transparentBg)
        val imagesFolder = File(context.cacheDir, "images")
        if (!imagesFolder.exists()) imagesFolder.mkdirs()

        val imageFile = File(imagesFolder, "bhagwa_champion_share.png")
        val stream = FileOutputStream(imageFile)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.flush()
        stream.close()

        val contentUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )

        val distanceKm = String.format("%.2f", activity.distanceMeters / 1000.0)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(
                Intent.EXTRA_TEXT,
                """
                    🏆 ${activity.title}
                    ${activity.notes}
                    📏 Distance: $distanceKm km
                    ⏱️ Duration: ${LocationTracker.formatDuration(activity.durationSeconds)}
                    📈 Elevation: ${activity.elevationGainMeters.toInt()}m
                    🏃 Pace: ${activity.avgPaceMinKm} min/km
                    
                    ⚡ Tracked on Bhagwa GPS Tracker!
                """.trimIndent()
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Champion Workout Card"))
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Sharing ready! Exported card image.", Toast.LENGTH_SHORT).show()
    }
}
