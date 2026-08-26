package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.LocationPoint
import com.example.model.Milestone
import com.example.model.MilestoneCategory
import com.example.model.PersonalRecords
import com.example.model.SplitInfo
import com.example.model.WorkoutActivity
import com.example.model.WorkoutType
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class LocalWorkoutRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("bhagwa_local_data", Context.MODE_PRIVATE)

    init {
        if (!prefs.contains("initialized_v3")) {
            seedInitialData()
            prefs.edit().putBoolean("initialized_v3", true).apply()
        }
    }

    fun updateActivity(updated: WorkoutActivity) {
        val current = getActivities().toMutableList()
        val index = current.indexOfFirst { it.id == updated.id }
        if (index != -1) {
            current[index] = updated
        } else {
            current.add(0, updated)
        }
        val jsonArray = JSONArray()
        for (item in current) {
            jsonArray.put(serializeWorkoutActivity(item))
        }
        prefs.edit().putString("activities_list", jsonArray.toString()).apply()
    }

    // --- ACTIVITIES ---

    fun getActivities(): List<WorkoutActivity> {
        val jsonStr = prefs.getString("activities_list", "[]") ?: "[]"
        val list = mutableListOf<WorkoutActivity>()
        try {
            val jsonArray = JSONArray(jsonStr)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(parseWorkoutActivity(obj))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list.sortedByDescending { it.timestampMs }
    }

    fun saveActivity(activity: WorkoutActivity) {
        val current = getActivities().toMutableList()
        current.removeAll { it.id == activity.id }
        current.add(0, activity) // Newest first

        val jsonArray = JSONArray()
        for (item in current) {
            jsonArray.put(serializeWorkoutActivity(item))
        }
        prefs.edit().putString("activities_list", jsonArray.toString()).apply()

        // Update PRs & Check Milestones
        updatePersonalRecordsAndCheckMilestones(activity)
    }

    fun getActivityById(id: String): WorkoutActivity? {
        return getActivities().find { it.id == id }
    }

    fun deleteActivity(id: String) {
        val current = getActivities().filterNot { it.id == id }
        val jsonArray = JSONArray()
        for (item in current) {
            jsonArray.put(serializeWorkoutActivity(item))
        }
        prefs.edit().putString("activities_list", jsonArray.toString()).apply()
    }

    // --- MILESTONES ---

    fun getMilestones(): List<Milestone> {
        val jsonStr = prefs.getString("milestones_list", null)
        if (jsonStr == null) {
            return getDefaultMilestones()
        }
        val list = mutableListOf<Milestone>()
        try {
            val jsonArray = JSONArray(jsonStr)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    Milestone(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        description = obj.getString("description"),
                        category = MilestoneCategory.valueOf(obj.getString("category")),
                        targetValue = obj.getDouble("targetValue"),
                        unit = obj.getString("unit"),
                        isUnlocked = obj.getBoolean("isUnlocked"),
                        unlockedDateMs = if (obj.has("unlockedDateMs") && !obj.isNull("unlockedDateMs")) obj.getLong("unlockedDateMs") else null,
                        iconName = obj.optString("iconName", "trophy")
                    )
                )
            }
        } catch (e: Exception) {
            return getDefaultMilestones()
        }
        return list
    }

    fun saveMilestones(milestones: List<Milestone>) {
        val jsonArray = JSONArray()
        for (m in milestones) {
            val obj = JSONObject().apply {
                put("id", m.id)
                put("title", m.title)
                put("description", m.description)
                put("category", m.category.name)
                put("targetValue", m.targetValue)
                put("unit", m.unit)
                put("isUnlocked", m.isUnlocked)
                put("unlockedDateMs", m.unlockedDateMs ?: JSONObject.NULL)
                put("iconName", m.iconName)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString("milestones_list", jsonArray.toString()).apply()
    }

    // --- PERSONAL RECORDS & GOALS ---

    fun getPersonalRecords(): PersonalRecords {
        val jsonStr = prefs.getString("personal_records", null) ?: return PersonalRecords()
        return try {
            val obj = JSONObject(jsonStr)
            PersonalRecords(
                fastest1kSec = obj.optLong("fastest1kSec", Long.MAX_VALUE),
                fastest5kSec = obj.optLong("fastest5kSec", Long.MAX_VALUE),
                fastest10kSec = obj.optLong("fastest10kSec", Long.MAX_VALUE),
                longestDistanceMeters = obj.optDouble("longestDistanceMeters", 0.0),
                maxElevationGainMeters = obj.optDouble("maxElevationGainMeters", 0.0),
                totalDistanceMeters = obj.optDouble("totalDistanceMeters", 0.0),
                totalActivitiesCount = obj.optInt("totalActivitiesCount", 0),
                weeklyGoalKm = obj.optDouble("weeklyGoalKm", 25.0)
            )
        } catch (e: Exception) {
            PersonalRecords()
        }
    }

    fun updateWeeklyGoal(goalKm: Double) {
        val currentPr = getPersonalRecords()
        val updated = currentPr.copy(weeklyGoalKm = goalKm)
        savePersonalRecords(updated)
    }

    private fun savePersonalRecords(pr: PersonalRecords) {
        val obj = JSONObject().apply {
            put("fastest1kSec", pr.fastest1kSec)
            put("fastest5kSec", pr.fastest5kSec)
            put("fastest10kSec", pr.fastest10kSec)
            put("longestDistanceMeters", pr.longestDistanceMeters)
            put("maxElevationGainMeters", pr.maxElevationGainMeters)
            put("totalDistanceMeters", pr.totalDistanceMeters)
            put("totalActivitiesCount", pr.totalActivitiesCount)
            put("weeklyGoalKm", pr.weeklyGoalKm)
        }
        prefs.edit().putString("personal_records", obj.toString()).apply()
    }

    // Check newly unlocked milestones for an activity
    fun evaluateMilestonesForActivity(
        distanceMeters: Double,
        durationSeconds: Long,
        elevationMeters: Double,
        avgPaceMinKmSeconds: Long
    ): List<Milestone> {
        val currentMilestones = getMilestones().toMutableList()
        val unlockedNow = mutableListOf<Milestone>()
        val totalActivities = getActivities().size + 1
        val distKm = distanceMeters / 1000.0

        for (i in currentMilestones.indices) {
            val m = currentMilestones[i]
            if (m.isUnlocked) continue

            var unlockConditionMet = false
            when (m.category) {
                MilestoneCategory.DISTANCE -> {
                    if (distKm >= m.targetValue) unlockConditionMet = true
                }
                MilestoneCategory.PACE -> {
                    // targetValue is pace in seconds per km e.g. 300 sec (5:00 min/km)
                    if (avgPaceMinKmSeconds in 1..m.targetValue.toLong()) unlockConditionMet = true
                }
                MilestoneCategory.ELEVATION -> {
                    if (elevationMeters >= m.targetValue) unlockConditionMet = true
                }
                MilestoneCategory.COUNT -> {
                    if (totalActivities >= m.targetValue) unlockConditionMet = true
                }
                MilestoneCategory.STREAK -> {
                    if (totalActivities >= m.targetValue) unlockConditionMet = true
                }
            }

            if (unlockConditionMet) {
                val newlyUnlocked = m.copy(isUnlocked = true, unlockedDateMs = System.currentTimeMillis())
                currentMilestones[i] = newlyUnlocked
                unlockedNow.add(newlyUnlocked)
            }
        }

        if (unlockedNow.isNotEmpty()) {
            saveMilestones(currentMilestones)
        }
        return unlockedNow
    }

    private fun updatePersonalRecordsAndCheckMilestones(activity: WorkoutActivity) {
        val currentPr = getPersonalRecords()
        var new1k = currentPr.fastest1kSec
        var new5k = currentPr.fastest5kSec
        var new10k = currentPr.fastest10kSec

        val activityDist = activity.distanceMeters

        // Check splits for fastest 1k
        for (split in activity.splits) {
            if (split.durationSeconds > 0 && split.durationSeconds < new1k) {
                new1k = split.durationSeconds
            }
        }

        if (activityDist >= 5000) {
            val paceSeconds = parsePaceToSeconds(activity.avgPaceMinKm)
            val est5kSec = (paceSeconds * 5.0).toLong()
            if (est5kSec < new5k) new5k = est5kSec
        }

        if (activityDist >= 10000) {
            val paceSeconds = parsePaceToSeconds(activity.avgPaceMinKm)
            val est10kSec = (paceSeconds * 10.0).toLong()
            if (est10kSec < new10k) new10k = est10kSec
        }

        val longestDist = maxOf(currentPr.longestDistanceMeters, activityDist)
        val maxElev = maxOf(currentPr.maxElevationGainMeters, activity.elevationGainMeters)
        val totalDist = currentPr.totalDistanceMeters + activityDist
        val totalCount = currentPr.totalActivitiesCount + 1

        val updatedPr = PersonalRecords(
            fastest1kSec = new1k,
            fastest5kSec = new5k,
            fastest10kSec = new10k,
            longestDistanceMeters = longestDist,
            maxElevationGainMeters = maxElev,
            totalDistanceMeters = totalDist,
            totalActivitiesCount = totalCount,
            weeklyGoalKm = currentPr.weeklyGoalKm
        )
        savePersonalRecords(updatedPr)

        // Evaluate milestone checklist
        val paceSec = parsePaceToSeconds(activity.avgPaceMinKm)
        evaluateMilestonesForActivity(
            distanceMeters = activity.distanceMeters,
            durationSeconds = activity.durationSeconds,
            elevationMeters = activity.elevationGainMeters,
            avgPaceMinKmSeconds = paceSec
        )
    }

    private fun parsePaceToSeconds(paceStr: String): Long {
        val parts = paceStr.split(":")
        if (parts.size == 2) {
            val mins = parts[0].toLongOrNull() ?: 0
            val secs = parts[1].toLongOrNull() ?: 0
            return mins * 60 + secs
        }
        return 0
    }

    // --- JSON PARSING HELPERS ---

    private fun serializeWorkoutActivity(act: WorkoutActivity): JSONObject {
        return JSONObject().apply {
            put("id", act.id)
            put("title", act.title)
            put("type", act.type.name)
            put("timestampMs", act.timestampMs)
            put("durationSeconds", act.durationSeconds)
            put("movingTimeSeconds", act.movingTimeSeconds)
            put("distanceMeters", act.distanceMeters)
            put("elevationGainMeters", act.elevationGainMeters)
            put("calories", act.calories)
            put("avgPaceMinKm", act.avgPaceMinKm)
            put("maxSpeedKmh", act.maxSpeedKmh)
            put("notes", act.notes)
            put("effortRating", act.effortRating)

            val routeArr = JSONArray()
            for (p in act.routePoints) {
                routeArr.put(JSONObject().apply {
                    put("lat", p.latitude)
                    put("lng", p.longitude)
                    put("alt", p.altitudeMeters)
                    put("ts", p.timestampMs)
                    put("speed", p.speedMps)
                })
            }
            put("routePoints", routeArr)

            val splitsArr = JSONArray()
            for (s in act.splits) {
                splitsArr.put(JSONObject().apply {
                    put("splitNumber", s.splitNumber)
                    put("durationSeconds", s.durationSeconds)
                    put("distanceKm", s.distanceKm)
                    put("avgPaceMinKm", s.avgPaceMinKm)
                    put("elevationGainMeters", s.elevationGainMeters)
                })
            }
            put("splits", splitsArr)

            val milestonesArr = JSONArray()
            for (mId in act.earnedMilestones) {
                milestonesArr.put(mId)
            }
            put("earnedMilestones", milestonesArr)
        }
    }

    private fun parseWorkoutActivity(obj: JSONObject): WorkoutActivity {
        val routeList = mutableListOf<LocationPoint>()
        if (obj.has("routePoints")) {
            val routeArr = obj.getJSONArray("routePoints")
            for (i in 0 until routeArr.length()) {
                val p = routeArr.getJSONObject(i)
                routeList.add(
                    LocationPoint(
                        latitude = p.getDouble("lat"),
                        longitude = p.getDouble("lng"),
                        altitudeMeters = p.optDouble("alt", 0.0),
                        timestampMs = p.optLong("ts", 0L),
                        speedMps = p.optDouble("speed", 0.0)
                    )
                )
            }
        }

        val splitsList = mutableListOf<SplitInfo>()
        if (obj.has("splits")) {
            val splitsArr = obj.getJSONArray("splits")
            for (i in 0 until splitsArr.length()) {
                val s = splitsArr.getJSONObject(i)
                splitsList.add(
                    SplitInfo(
                        splitNumber = s.getInt("splitNumber"),
                        durationSeconds = s.getLong("durationSeconds"),
                        distanceKm = s.optDouble("distanceKm", 1.0),
                        avgPaceMinKm = s.getString("avgPaceMinKm"),
                        elevationGainMeters = s.optDouble("elevationGainMeters", 0.0)
                    )
                )
            }
        }

        val earnedMilestones = mutableListOf<String>()
        if (obj.has("earnedMilestones")) {
            val mArr = obj.getJSONArray("earnedMilestones")
            for (i in 0 until mArr.length()) {
                earnedMilestones.add(mArr.getString(i))
            }
        }

        return WorkoutActivity(
            id = obj.getString("id"),
            title = obj.getString("title"),
            type = WorkoutType.valueOf(obj.optString("type", "RUN")),
            timestampMs = obj.getLong("timestampMs"),
            durationSeconds = obj.getLong("durationSeconds"),
            movingTimeSeconds = obj.optLong("movingTimeSeconds", obj.getLong("durationSeconds")),
            distanceMeters = obj.getDouble("distanceMeters"),
            elevationGainMeters = obj.getDouble("elevationGainMeters"),
            calories = obj.optInt("calories", 0),
            avgPaceMinKm = obj.getString("avgPaceMinKm"),
            maxSpeedKmh = obj.optDouble("maxSpeedKmh", 0.0),
            routePoints = routeList,
            splits = splitsList,
            notes = obj.optString("notes", ""),
            effortRating = obj.optInt("effortRating", 5),
            earnedMilestones = earnedMilestones
        )
    }

    private fun getDefaultMilestones(): List<Milestone> {
        return listOf(
            Milestone("m_1", "First Step", "Complete your very first activity in Bhagwa", MilestoneCategory.COUNT, 1.0, "activity", isUnlocked = true, unlockedDateMs = System.currentTimeMillis() - 86400000L * 3, iconName = "footsteps"),
            Milestone("m_2", "1K Club", "Log an activity of at least 1.0 km", MilestoneCategory.DISTANCE, 1.0, "km", isUnlocked = true, unlockedDateMs = System.currentTimeMillis() - 86400000L * 3, iconName = "trophy"),
            Milestone("m_3", "5K Finisher", "Complete a full 5.0 km run or ride", MilestoneCategory.DISTANCE, 5.0, "km", isUnlocked = true, unlockedDateMs = System.currentTimeMillis() - 86400000L * 1, iconName = "star"),
            Milestone("m_4", "10K Master", "Conquer a 10.0 km distance workout", MilestoneCategory.DISTANCE, 10.0, "km", isUnlocked = false, iconName = "crown"),
            Milestone("m_5", "Half Marathon Hunter", "Log 21.1 km in a single session", MilestoneCategory.DISTANCE, 21.1, "km", isUnlocked = false, iconName = "medal"),
            Milestone("m_6", "Speed Demon", "Achieve a split pace under 5:00 min/km", MilestoneCategory.PACE, 300.0, "sec", isUnlocked = true, unlockedDateMs = System.currentTimeMillis() - 86400000L * 1, iconName = "lightning"),
            Milestone("m_7", "Sub 4:30 Rocket", "Achieve a split pace under 4:30 min/km", MilestoneCategory.PACE, 270.0, "sec", isUnlocked = false, iconName = "rocket"),
            Milestone("m_8", "Hill Climber", "Gain 50 meters of elevation in one workout", MilestoneCategory.ELEVATION, 50.0, "m", isUnlocked = true, unlockedDateMs = System.currentTimeMillis() - 86400000L * 1, iconName = "mountain"),
            Milestone("m_9", "Peak Performer", "Gain 150 meters of elevation in one workout", MilestoneCategory.ELEVATION, 150.0, "m", isUnlocked = false, iconName = "mountain_peak"),
            Milestone("m_10", "3 Activity Streak", "Log 3 total activities to build your streak", MilestoneCategory.COUNT, 3.0, "activities", isUnlocked = false, iconName = "fire"),
            Milestone("m_11", "10 Activity Veteran", "Log 10 workouts in Bhagwa", MilestoneCategory.COUNT, 10.0, "activities", isUnlocked = false, iconName = "shield")
        )
    }

    private fun seedInitialData() {
        val now = System.currentTimeMillis()
        saveMilestones(getDefaultMilestones())

        // Seed the exact Belagavi 15.07 km Run with exact user stats:
        // Distance: 15.07 km, Duration: 2:00:35 (7235 sec), Elevation Gain: 144m, Pace: 8.00 min/km
        val belagaviPoints = generateBelagaviRoutePoints()
        val belagaviSplits = listOf(
            SplitInfo(1, 485, 1.0, "8:05", 8.0),
            SplitInfo(2, 478, 1.0, "7:58", 12.0),
            SplitInfo(3, 482, 1.0, "8:02", 15.0),
            SplitInfo(4, 475, 1.0, "7:55", 10.0),
            SplitInfo(5, 480, 1.0, "8:00", 14.0),
            SplitInfo(6, 488, 1.0, "8:08", 11.0),
            SplitInfo(7, 472, 1.0, "7:52", 9.0),
            SplitInfo(8, 480, 1.0, "8:00", 12.0),
            SplitInfo(9, 484, 1.0, "8:04", 8.0),
            SplitInfo(10, 476, 1.0, "7:56", 10.0),
            SplitInfo(11, 481, 1.0, "8:01", 7.0),
            SplitInfo(12, 479, 1.0, "7:59", 9.0),
            SplitInfo(13, 485, 1.0, "8:05", 11.0),
            SplitInfo(14, 477, 1.0, "7:57", 5.0),
            SplitInfo(15, 493, 1.07, "8:00", 3.0)
        )

        // 15th August 2026 5:05 AM timestamp
        val calendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.YEAR, 2026)
            set(java.util.Calendar.MONTH, java.util.Calendar.AUGUST)
            set(java.util.Calendar.DAY_OF_MONTH, 15)
            set(java.util.Calendar.HOUR_OF_DAY, 5)
            set(java.util.Calendar.MINUTE, 5)
            set(java.util.Calendar.SECOND, 0)
        }
        val aug15Time = calendar.timeInMillis

        val belagaviAct = WorkoutActivity(
            id = "belagavi_15k_run",
            title = "Independence Day Run Saturday 5:05 am",
            type = WorkoutType.RUN,
            timestampMs = aug15Time,
            durationSeconds = 7235, // 2:00:35
            movingTimeSeconds = 7180,
            distanceMeters = 15070.0, // 15.07 km
            elevationGainMeters = 144.0, // 144 m
            calories = 985,
            avgPaceMinKm = "8.00",
            maxSpeedKmh = 11.2,
            routePoints = belagaviPoints,
            splits = belagaviSplits,
            notes = "15 | 08 for 15km 8pace",
            effortRating = 9,
            earnedMilestones = listOf("m_1", "m_2", "m_3", "m_4", "m_8", "m_9")
        )

        val sample2Points = generateSampleRoutePoints(15.8520, 74.4980, 5200.0, 18)
        val sample2Splits = listOf(
            SplitInfo(1, 310, 1.0, "5:10", 12.0),
            SplitInfo(2, 298, 1.0, "4:58", 15.0),
            SplitInfo(3, 305, 1.0, "5:05", 8.0),
            SplitInfo(4, 292, 1.0, "4:52", 10.0),
            SplitInfo(5, 285, 1.0, "4:45", 5.0)
        )

        val sample2Act = WorkoutActivity(
            id = UUID.randomUUID().toString(),
            title = "Camp Morning Recovery Jog 🌅",
            type = WorkoutType.RUN,
            timestampMs = now - (86400000L * 3), // 3 days ago
            durationSeconds = 1490,
            movingTimeSeconds = 1450,
            distanceMeters = 5200.0,
            elevationGainMeters = 50.0,
            calories = 340,
            avgPaceMinKm = "4:46",
            maxSpeedKmh = 14.8,
            routePoints = sample2Points,
            splits = sample2Splits,
            notes = "Cool breeze through the military grounds.",
            effortRating = 6,
            earnedMilestones = listOf("m_1", "m_2", "m_3")
        )

        saveActivity(belagaviAct)
        saveActivity(sample2Act)
    }

    private fun generateBelagaviRoutePoints(): List<LocationPoint> {
        // Belagavi coordinate waypoints matching user map (Ganeshpur Rd, Vinayaka Nagar, Camp, Hotel Sanman, Khade Bazar, Belagavi Station, Military Mahadev Zoo, Maratha, Elphinstone Rd)
        val coords = listOf(
            Pair(15.8640, 74.4780), // Vinayaka Nagar / Healing Hands
            Pair(15.8655, 74.4820),
            Pair(15.8620, 74.4850),
            Pair(15.8670, 74.4880),
            Pair(15.8650, 74.4920),
            Pair(15.8610, 74.4930), // Elphinstone Road junction
            Pair(15.8580, 74.4960),
            Pair(15.8640, 74.5010), // Ganeshpur Road
            Pair(15.8710, 74.5020),
            Pair(15.8740, 74.5070),
            Pair(15.8790, 74.5120), // Golf Course north loop
            Pair(15.8760, 74.5180),
            Pair(15.8740, 74.5220),
            Pair(15.8710, 74.5260), // Hospital / East apex
            Pair(15.8680, 74.5240),
            Pair(15.8660, 74.5210), // Hotel Sanman
            Pair(15.8630, 74.5190), // Khade Bazar
            Pair(15.8610, 74.5180),
            Pair(15.8590, 74.5160),
            Pair(15.8560, 74.5190),
            Pair(15.8530, 74.5220), // Belagavi East / Shastri Nagar
            Pair(15.8490, 74.5180), // Railway Station area
            Pair(15.8460, 74.5150),
            Pair(15.8420, 74.5120), // Military Mahadev Zoo
            Pair(15.8440, 74.5080),
            Pair(15.8480, 74.5050), // Camp area
            Pair(15.8530, 74.5040),
            Pair(15.8580, 74.5020),
            Pair(15.8610, 74.4980),
            Pair(15.8640, 74.4780) // Loop back
        )

        val points = mutableListOf<LocationPoint>()
        val baseTime = System.currentTimeMillis() - 7235000L
        for (i in coords.indices) {
            val (lat, lng) = coords[i]
            val alt = 750.0 + (Math.sin(i.toDouble() / 2.0) * 25.0) + (if (i % 3 == 0) 15.0 else 0.0)
            points.add(
                LocationPoint(
                    latitude = lat,
                    longitude = lng,
                    altitudeMeters = alt,
                    timestampMs = baseTime + (i * (7235000L / coords.size)),
                    speedMps = 2.08 // ~8.00 min/km
                )
            )
        }
        return points
    }

    private fun generateSampleRoutePoints(startLat: Double, startLng: Double, totalDistM: Double, count: Int): List<LocationPoint> {
        val points = mutableListOf<LocationPoint>()
        var curLat = startLat
        var curLng = startLng
        var curAlt = 15.0

        for (i in 0 until count) {
            val angle = (i * 18) * (Math.PI / 180.0)
            curLat += Math.sin(angle) * 0.0018
            curLng += Math.cos(angle) * 0.0022
            curAlt += (Math.sin(i.toDouble()) * 4.0)

            points.add(
                LocationPoint(
                    latitude = curLat,
                    longitude = curLng,
                    altitudeMeters = curAlt,
                    timestampMs = System.currentTimeMillis() - ((count - i) * 60000L),
                    speedMps = 3.5
                )
            )
        }
        return points
    }
}
