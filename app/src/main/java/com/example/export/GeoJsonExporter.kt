package com.example.export

import com.example.model.WorkoutSession

object GeoJsonExporter {
    fun exportToGeoJson(session: WorkoutSession): String {
        val coordinates = session.routePoints.joinToString(",") { point ->
            "[${point.longitude}, ${point.latitude}, ${point.altitudeMeters}]"
        }
        return """{
  "type": "FeatureCollection",
  "features": [
    {
      "type": "Feature",
      "geometry": {
        "type": "LineString",
        "coordinates": [$coordinates]
      },
      "properties": {
        "id": "${session.id}",
        "activity": "${session.type.name}",
        "distanceMeters": ${session.distanceMeters},
        "durationSeconds": ${session.durationSeconds},
        "elevationGainMeters": ${session.elevationGainMeters},
        "caloriesBurned": ${session.caloriesBurned}
      }
    }
  ]
}"""
    }
}
