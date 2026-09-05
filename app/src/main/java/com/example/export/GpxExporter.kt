package com.example.export

import com.example.model.WorkoutSession
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object GpxExporter {
    fun exportToGpx(session: WorkoutSession): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        return buildString {
            appendLine("<?xml version="1.0" encoding="UTF-8"?>")
            appendLine("<gpx version="1.1" creator="Bhagwa GPS Workout Tracker" xmlns="http://www.topografix.com/GPX/1/1">")
            appendLine("  <metadata>")
            appendLine("    <name>Bhagwa ${session.type.name} - ${session.id}</name>")
            appendLine("    <time>${dateFormat.format(Date(session.startTimeMillis))}</time>")
            appendLine("  </metadata>")
            appendLine("  <trk>")
            appendLine("    <name>${session.type.name} Workout</name>")
            appendLine("    <trkseg>")
            for (point in session.routePoints) {
                appendLine("      <trkpt lat="${point.latitude}" lon="${point.longitude}">")
                appendLine("        <ele>${point.altitudeMeters}</ele>")
                appendLine("        <time>${dateFormat.format(Date(point.timestampMillis))}</time>")
                appendLine("      </trkpt>")
            }
            appendLine("    </trkseg>")
            appendLine("  </trk>")
            appendLine("</gpx>")
        }
    }
}
