<div align="center">
  <img width="1200" height="675" alt="Bhagwa Hero Banner" src="bhagwa_hero_banner.jpg" />

  # 🏃 Bhagwa (भगवा)
  ### Next-Generation GPS Workout & Activity Tracker with Google Gemini AI Coaching

  [![Android CI](https://github.com/yashkoparde/bhagwa/actions/workflows/android-ci.yml/badge.svg)](https://github.com/yashkoparde/bhagwa/actions/workflows/android-ci.yml)
  [![Release](https://img.shields.io/github/v/release/yashkoparde/bhagwa?color=orange&label=Release)](https://github.com/yashkoparde/bhagwa/releases/tag/v1.0.0)
  [![Kotlin](https://img.shields.io/badge/Kotlin-2.0.0-7F52FF.svg?logo=kotlin&logoColor=white)](https://kotlinlang.org)
  [![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4.svg?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
  [![Gemini AI](https://img.shields.io/badge/Google%20Gemini-1.5%20Flash-F9AB00.svg?logo=google&logoColor=white)](https://ai.google.dev)
  [![Min SDK](https://img.shields.io/badge/Min%20SDK-26-green.svg)](https://developer.android.com)
  [![Target SDK](https://img.shields.io/badge/Target%20SDK-34-brightgreen.svg)](https://developer.android.com)
  [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

  <p align="center">
    <b>A privacy-first, battery-efficient Android fitness tracker featuring real-time GPS telemetry, custom dynamic canvas route maps, split pacing analytics, shareable branded flex cards, and personalized post-workout coaching powered by Google Gemini.</b>
  </p>

  [Key Features](#-key-features) •
  [Architecture](#-architecture--design) •
  [Screenshots & Cards](#-shareable-workout-flex-cards) •
  [Getting Started](#-getting-started) •
  [Multi-Format Export](#-multi-format-route-exporters) •
  [AI Coaching](#-google-gemini-ai-coaching) •
  [Testing & CI](#-testing--quality-assurance)
</div>

---

## 📖 Overview

**Bhagwa** is an athletic activity and GPS workout tracking application built from the ground up for runners, cyclists, and fitness enthusiasts. Combining high-precision location tracking algorithms with a striking saffron-infused Material 3 design system, Bhagwa delivers an uncompromised tracking experience without privacy tracking bloat, subscriptions, or intrusive advertisements.

Whether you are logging a morning 5K, conquering elevation on trail runs, or training for a full marathon, Bhagwa captures every stride, computes per-kilometer splits with audible voice updates, visualizes your speed gradients on an interactive Compose canvas, and uses Google Gemini 1.5 to provide expert-level coaching feedback directly on your device.

---

## ✨ Key Features

### 📍 Precision GPS & Route Telemetry
- **Fused Location Engine**: Leverages Google Play Services `FusedLocationProviderClient` configured with `PRIORITY_HIGH_ACCURACY` and adaptive polling intervals (2s active, up to 5s stationary).
- **Noise & Drift Filtering**: Rejects inaccurate GPS fixes (>25m accuracy threshold) and implements exponential moving average (EMA) speed smoothing to eliminate erratic pace spikes.
- **Auto-Pause & Battery Optimization**: Automatically detects when velocity drops below 0.5 m/s to pause distance accumulation, conserving battery and maintaining pace fidelity.
- **Resilient Background Service**: Runs as an Android Foreground Service with notification channel updates and safe wake-lock handling.

### 🎨 RouteMapCanvas: Dynamic Speed Gradient Mapping
- **Custom Canvas Rendering**: High-performance Compose `Canvas` rendering that normalizes WGS84 coordinates to screen pixels with automatic aspect-ratio bounding box scaling.
- **Speed Polyline Shaders**: Color-codes your running route with a continuous gradient shader:
  - 🟢 **Aerobic / Recovery Pace** (Emerald Green)
  - 🟡 **Tempo / Threshold Pace** (Vibrant Saffron Yellow)
  - 🔴 **Anaerobic / Sprint Pace** (Fiery Crimson Red)
- **Waypoints & Markers**: Renders starting waypoint pins, checkered finish flags, and kilometer split milestone badges directly along the polyline path.

### ⏱️ Kilometer Splits & Audio Cue Announcements
- **Automated Split Boundaries**: Computes pace, elapsed time, and elevation change for every 1,000 meters (or 1 mile).
- **Extremes Detection**: Automatically highlights your fastest split and toughest kilometer in the workout analysis.
- **Voice Cues via Text-to-Speech**: Built-in `AudioSplitAnnouncer` speaks split milestones aloud through earphones so you never have to break stride to look at your phone.

### 🎴 Shareable Workout Flex Cards & Commemorative Templates
- **Social Media Ready**: Generates high-resolution PNG graphics optimized for Instagram Stories (9:16) and social feeds (1:1).
- **Dynamic Stats Overlay**: Overlays route polyline snapshots, total distance, average pace, duration, elevation ascent, and calories burned over athletic gradient backdrops.
- **Independence Day Edition**: Includes commemorative templates, champion badges, and transparent overlay frames (`view_and_download_cards.html`).

### 🤖 Google Gemini AI Post-Workout Coaching
- **Intelligent Telemetry Analysis**: Summarizes workout cadence, split pacing consistency, and elevation changes into structured prompts sent to Gemini 1.5 Flash.
- **Actionable Coaching Feedback**:
  - **Pacing Assessment**: Identifies early burnout or strong negative splits.
  - **Recovery Protocol**: Personalized hydration, nutrition, and rest guidance.
  - **Next Session Goal**: Targeted cadence and distance recommendations.
- **Offline Fallback & Quota Caching**: Gracefully provides rule-based heuristic guidance when offline and caches AI summaries locally.

### 🏆 Milestones, Badges & Gamification
- **Achievement Badges**: Unlock milestones including First 5K, 10K Club, Half Marathon, Centurion (100km), and Mountain Climber.
- **Consecutive Streak Tracker**: Keeps athletes motivated by logging consecutive daily training streaks.
- **Career Distance Odometer**: Aggregates all-time kilometers, total training hours, and cumulative calories burned.
- **Celebration Modal**: Confetti animation celebration upon unlocking milestones.

### 📂 Multi-Format Route Exporters
- **GPX 1.1 XML**: Full Topografix schema compliance with timestamped `<trkpt>` nodes, elevation, and activity metadata. Compatible with **Strava**, **Garmin Connect**, and **Runkeeper**.
- **GeoJSON**: Exports route tracks as `FeatureCollection` LineString geometries for open GIS tools, QGIS, and web maps.
- **Google Earth KML**: 3D route replay with custom styled route paths in Google Earth.
- **Storage Access Framework (SAF)**: Native Android file picker integration for direct saving or sharing via Android Sharesheet.

---

## 🏛️ Architecture & Design

Bhagwa is designed according to **Clean Architecture** principles and Android Modern Architecture guidelines:

```
+-------------------------------------------------------------------------+
|                        Presentation Layer (Jetpack Compose)             |
|                                                                         |
|  DashboardScreen       TrackWorkoutScreen         ActivityFeedScreen    |
|  MilestonesScreen      ActivityDetailScreen       RouteMapCanvas        |
|  ShareableWorkoutCardModal                                               |
+------------------------------------+------------------------------------+
                                     | (Observes UI State / Sends Intents)
                                     v
+-------------------------------------------------------------------------+
|                         ViewModel Layer (Architecture Components)       |
|                                                                         |
|  WorkoutViewModel (StateFlow, CoroutineScope, SavedStateHandle)         |
+------------------------------------+------------------------------------+
                                     | (Calls Use Cases & Repositories)
                                     v
+-------------------------------------------------------------------------+
|                          Domain & Data Layer                            |
|                                                                         |
|  LocalWorkoutRepository <-----> LocationTracker (FusedLocationProvider) |
|  GeminiCoachingService  <-----> AudioSplitAnnouncer (TTS Engine)        |
|  GpxExporter            <-----> GeoJsonExporter / KmlExporter           |
|  BatteryOptimizationHelper                                              |
+-------------------------------------------------------------------------+
```

### Tech Stack

| Technology | Purpose |
|---|---|
| **Kotlin 2.0.0** | Modern, concise, and safe programming language |
| **Jetpack Compose** | 100% declarative UI framework |
| **Material 3** | Latest design tokens, dynamic color, and typography |
| **Coroutines & StateFlow** | Reactive, asynchronous, lifecycle-aware state management |
| **FusedLocationProviderClient** | High-precision, battery-efficient GPS provider |
| **Google Gemini API** | Server-side & client AI coaching summaries |
| **Navigation Compose** | Single-activity Compose navigation graph |
| **Robolectric & JUnit 5** | JVM-based UI and algorithmic unit testing |
| **GitHub Actions** | Automated CI pipeline for continuous build and test |

---

## 🎨 Design System & Theming

The Bhagwa design system is anchored in an energetic, athletic palette:

| Color Token | Hex Code | Usage |
|---|---|---|
| `BhagwaPrimary` | `#FF9933` | Deep Saffron accent, primary actions, and hero highlights |
| `BhagwaOrange` | `#FF6600` | Secondary energetic gradient tint and split markers |
| `DarkBackground` | `#121212` | True AMOLED dark background for battery efficiency |
| `DarkSurface` | `#1E1E1E` | Elevated cards, metric tiles, and bottom navigation |
| `PaceFast` | `#00E676` | Aerobic pace polyline shader and top split indicator |
| `PaceSlow` | `#FF5252` | Anaerobic/sprint pace polyline shader |

---

## 🚀 Getting Started

### Prerequisites
- [Android Studio Iguana (2023.2.1)](https://developer.android.com/studio) or newer
- **JDK 17** (Temurin, Corretto, or Zulu)
- Android SDK Platform 34
- A physical Android device or emulator with Google Play Services enabled

### Installation Steps

1. **Clone the repository**:
   ```bash
   git clone https://github.com/yashkoparde/bhagwa.git
   cd bhagwa
   ```

2. **Configure Gemini API Key**:
   Create a `.env` file in the project root by copying the template:
   ```bash
   cp .env.example .env
   ```
   Open `.env` and set your key:
   ```properties
   GEMINI_API_KEY=your_gemini_api_key_here
   ```
   *(Get your free API key at [Google AI Developer Portal](https://ai.google.dev))*

3. **Open in Android Studio**:
   - Select **Open** and choose the `bhagwa` directory.
   - Allow Gradle to sync dependencies from Maven Central and Google repositories.

4. **Build and Run**:
   ```bash
   # Build debug APK
   ./gradlew assembleDebug

   # Run on connected device
   ./gradlew installDebug
   ```

---

## 🧪 Testing & Quality Assurance

The codebase includes a comprehensive suite of unit tests, algorithmic validations, and Robolectric UI tests:

```bash
# Run all unit tests
./gradlew testDebugUnitTest

# Run specific location math tests
./gradlew testDebugUnitTest --tests "com.example.LocationMathTest"

# Run split pace calculator tests
./gradlew testDebugUnitTest --tests "com.example.SplitCalculatorTest"
```

### Test Coverage Highlights
- **Haversine Distance Accuracy**: Validates great-circle distance math against known geographic benchmarks.
- **Split Pacing Detection**: Verifies 1,000m interval triggering and pace string formatters (`M:SS`).
- **Compose Screen Lifecycle**: Verifies ViewModel state propagation across screen transitions.
- **Screenshot Regression Tests**: Baseline visual verification for tracking and card previews (`greeting.png`).

---

## 📂 Project Structure

```
bhagwa/
├── .github/
│   ├── workflows/android-ci.yml        # Automated CI build and test pipeline
│   ├── ISSUE_TEMPLATE/                 # Bug report & feature request templates
│   └── PULL_REQUEST_TEMPLATE.md        # PR checklist and review template
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml         # Foreground service & location permissions
│   │   ├── assets/                     # Independence Day edition templates & badges
│   │   ├── java/com/example/
│   │   │   ├── MainActivity.kt         # Single-activity scaffold & navigation
│   │   │   ├── ai/                     # Google Gemini AI coaching service
│   │   │   ├── audio/                  # Text-to-Speech audio split announcer
│   │   │   ├── data/                   # LocalWorkoutRepository with StateFlow
│   │   │   ├── export/                 # GPX, GeoJSON, and KML exporters
│   │   │   ├── location/               # Fused location tracker & battery helper
│   │   │   ├── model/                  # Domain entities, LocationPoint, splits
│   │   │   └── ui/
│   │   │       ├── WorkoutViewModel.kt # State machine & timer coroutines
│   │   │       ├── components/         # RouteMapCanvas & ShareableWorkoutCard
│   │   │       ├── screens/            # Dashboard, Track, Feed, Detail, Milestones
│   │   │       └── theme/              # Color, Type, Theme tokens
│   │   └── res/                        # Drawables, mipmaps, strings, colors, XML rules
│   └── src/test/                       # Unit tests & Robolectric test suite
├── docs/                               # Architecture, location math, and contributing docs
├── gradle/libs.versions.toml           # Gradle version catalog
├── view_and_download_cards.html        # Interactive flex card preview tool
├── extract.py                          # Card asset extraction helper
└── README.md                           # Project documentation
```

---

## 🤝 Contributing

Contributions are welcome! Please read [docs/CONTRIBUTING.md](docs/CONTRIBUTING.md) before submitting a pull request.

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'feat: add AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📄 License

Distributed under the **Apache License 2.0**. See `LICENSE` for more information.

---

<div align="center">
  <sub>Developed with ❤️ by <a href="https://github.com/yashkoparde">YASH B KOPARDE</a></sub>
</div>
