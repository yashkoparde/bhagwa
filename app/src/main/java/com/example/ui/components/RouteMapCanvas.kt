package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LocationPoint
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GoldStar
import com.example.ui.theme.StravaOrange
import kotlin.math.max
import kotlin.math.min

@Composable
fun RouteMapCanvas(
    routePoints: List<LocationPoint>,
    modifier: Modifier = Modifier,
    showElevationProfile: Boolean = true,
    isLiveTracking: Boolean = false
) {
    var userScale by remember { mutableFloatStateOf(1.0f) }
    var userPanX by remember { mutableFloatStateOf(0.0f) }
    var userPanY by remember { mutableFloatStateOf(0.0f) }

    fun resetView() {
        userScale = 1.0f
        userPanX = 0.0f
        userPanY = 0.0f
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF181818))
            .testTag("route_map_canvas_container")
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        userScale = (userScale * zoom).coerceIn(0.5f, 4.0f)
                        userPanX += pan.x
                        userPanY += pan.y
                    }
                }
        ) {
            val width = size.width
            val height = size.height

            // 1. Draw Map Grid / Topographic Background Lines
            val gridSpacing = 60.dp.toPx()
            val gridColor = Color(0xFF2B2B2B)
            val gridDotColor = Color(0xFF383838)

            var x = 0f
            while (x < width) {
                drawLine(
                    color = gridColor,
                    start = Offset(x, 0f),
                    end = Offset(x, height),
                    strokeWidth = 1f
                )
                x += gridSpacing
            }

            var y = 0f
            while (y < height) {
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1f
                )
                y += gridSpacing
            }

            // Draw grid accent dots at intersections
            var dotX = 0f
            while (dotX < width) {
                var dotY = 0f
                while (dotY < height) {
                    drawCircle(
                        color = gridDotColor,
                        radius = 2.5f,
                        center = Offset(dotX, dotY)
                    )
                    dotY += gridSpacing
                }
                dotX += gridSpacing
            }

            // If no points, show empty map indicator
            if (routePoints.isEmpty()) {
                return@Canvas
            }

            // 2. Compute Latitude / Longitude Bounding Box
            var minLat = Double.MAX_VALUE
            var maxLat = -Double.MAX_VALUE
            var minLng = Double.MAX_VALUE
            var maxLng = -Double.MAX_VALUE

            for (p in routePoints) {
                minLat = min(minLat, p.latitude)
                maxLat = max(maxLat, p.latitude)
                minLng = min(minLng, p.longitude)
                maxLng = max(maxLng, p.longitude)
            }

            val latSpan = max(0.0008, maxLat - minLat)
            val lngSpan = max(0.0008, maxLng - minLng)

            val padding = 60.dp.toPx()
            val drawWidth = width - (padding * 2)
            val drawHeight = height - (padding * 2)

            // Convert lat/lng to Screen XY Coordinates
            fun mapToOffset(pt: LocationPoint): Offset {
                val normX = ((pt.longitude - minLng) / lngSpan).toFloat()
                // Latitude grows North (upwards), screen Y grows downwards
                val normY = (1.0f - ((pt.latitude - minLat) / latSpan)).toFloat()

                val rawX = padding + (normX * drawWidth)
                val rawY = padding + (normY * drawHeight)

                // Apply Pan & Scale around center
                val centerX = width / 2f
                val centerY = height / 2f

                val scaledX = (rawX - centerX) * userScale + centerX + userPanX
                val scaledY = (rawY - centerY) * userScale + centerY + userPanY

                return Offset(scaledX, scaledY)
            }

            val screenOffsets = routePoints.map { mapToOffset(it) }

            // 3. Draw Outer Glow / Aura for Polyline
            val glowPath = Path().apply {
                if (screenOffsets.isNotEmpty()) {
                    moveTo(screenOffsets[0].x, screenOffsets[0].y)
                    for (i in 1 until screenOffsets.size) {
                        lineTo(screenOffsets[i].x, screenOffsets[i].y)
                    }
                }
            }

            drawPath(
                path = glowPath,
                color = Color.Red.copy(alpha = 0.35f),
                style = Stroke(
                    width = 16.dp.toPx() * userScale,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // 4. Draw Core Crisp Polyline
            drawPath(
                path = glowPath,
                color = Color.Red,
                style = Stroke(
                    width = 5.dp.toPx() * userScale,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // 5. Draw Kilometer Split Markers (every ~1000m approx or index interval)
            val pointInterval = max(1, routePoints.size / 5)
            for (i in pointInterval until routePoints.size step pointInterval) {
                if (i < screenOffsets.size) {
                    val pos = screenOffsets[i]
                    val kmLabel = "${(i * 1.0 / pointInterval).toInt()}K"

                    drawCircle(
                        color = Color.Black,
                        radius = 12.dp.toPx(),
                        center = pos
                    )
                    drawCircle(
                        color = GoldStar,
                        radius = 10.dp.toPx(),
                        center = pos,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }

            // 6. Draw START Point Marker (Green Circle)
            if (screenOffsets.isNotEmpty()) {
                val startPos = screenOffsets[0]
                drawCircle(
                    color = Color.White,
                    radius = 10.dp.toPx(),
                    center = startPos
                )
                drawCircle(
                    color = Color(0xFF00E676), // Bright Green
                    radius = 8.dp.toPx(),
                    center = startPos
                )
            }

            // 7. Draw END / CURRENT LOCATION Pulsing Marker (Strava Orange Circle)
            if (screenOffsets.isNotEmpty()) {
                val endPos = screenOffsets.last()

                if (isLiveTracking) {
                    // Pulsing Ring
                    drawCircle(
                        color = StravaOrange.copy(alpha = 0.4f),
                        radius = 18.dp.toPx(),
                        center = endPos
                    )
                }

                drawCircle(
                    color = Color.White,
                    radius = 10.dp.toPx(),
                    center = endPos
                )
                drawCircle(
                    color = StravaOrange,
                    radius = 7.dp.toPx(),
                    center = endPos
                )
            }
        }

        // Overlay Controls & Indicators
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = DarkSurface.copy(alpha = 0.85f),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF444444))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = "Map Style",
                        tint = StravaOrange,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isLiveTracking) "LIVE GPS ROUTE" else "ROUTE MAP",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Reset View Button
        IconButton(
            onClick = { resetView() },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .background(DarkSurface.copy(alpha = 0.85f), CircleShape)
                .testTag("reset_map_view_button")
        ) {
            Icon(
                imageVector = Icons.Default.CenterFocusStrong,
                contentDescription = "Recenter Map",
                tint = Color.White
            )
        }

        // Bottom Elevation Profile Graph Overlay
        if (showElevationProfile && routePoints.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                ElevationGraphOverlay(routePoints = routePoints)
            }
        }
    }
}

@Composable
fun ElevationGraphOverlay(routePoints: List<LocationPoint>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xDD1E1E1E)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Landscape,
                    contentDescription = "Elevation",
                    tint = GoldStar,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "ELEVATION PROFILE",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = Color.LightGray,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
            ) {
                val w = size.width
                val h = size.height

                if (routePoints.size < 2) return@Canvas

                var minAlt = routePoints.minOf { it.altitudeMeters }
                var maxAlt = routePoints.maxOf { it.altitudeMeters }
                if (maxAlt - minAlt < 5.0) {
                    maxAlt += 5.0
                }

                val path = Path()
                val fillPath = Path()

                val pointStep = w / (routePoints.size - 1)

                fillPath.moveTo(0f, h)

                for (i in routePoints.indices) {
                    val alt = routePoints[i].altitudeMeters
                    val normY = 1.0f - ((alt - minAlt) / (maxAlt - minAlt)).toFloat()
                    val px = i * pointStep
                    val py = normY * h

                    if (i == 0) {
                        path.moveTo(px, py)
                        fillPath.lineTo(px, py)
                    } else {
                        path.lineTo(px, py)
                        fillPath.lineTo(px, py)
                    }
                }

                fillPath.lineTo(w, h)
                fillPath.close()

                drawPath(
                    path = fillPath,
                    color = GoldStar.copy(alpha = 0.2f)
                )

                drawPath(
                    path = path,
                    color = GoldStar,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
    }
}
