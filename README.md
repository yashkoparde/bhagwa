<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://ai.google.dev/static/site-assets/images/share-ais-513315318.png" />
</div>

# Bhagwa - GPS Workout & Activity Tracker

[![Android CI](https://github.com/yashkoparde/bhagwa/actions/workflows/android-ci.yml/badge.svg)](https://github.com/yashkoparde/bhagwa/actions/workflows/android-ci.yml)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.0-purple.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-blue.svg)](https://developer.android.com/jetpack/compose)
[![Gemini](https://img.shields.io/badge/Google%20Gemini-AI%20Coaching-orange.svg)](https://ai.google.dev)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

**Bhagwa** is a cutting-edge GPS workout and activity tracker engineered for runners, cyclists, and fitness enthusiasts. Built with Jetpack Compose, Material 3, and integrated with Google Gemini AI for smart post-workout coaching insights.

---

## Features

- **Live Route Recording & GPS Tracking**: High-precision fused location tracking with real-time route rendering.
- **Dynamic Speed Polyline**: Custom Compose Canvas route map color-coded by pacing speed.
- **Kilometer Split Analytics**: Automated split pacing alerts with audio announcements.
- **Shareable Workout Flex Cards**: High-resolution branded cards with route polyline for social sharing.
- **Independence Day Edition**: Special commemorative templates celebrating national fitness.
- **Gemini AI Coaching**: Intelligent analysis of workout splits, cadence, and heart rate exertion.
- **Multi-Format Export**: Export route data to GPX, GeoJSON, and KML formats compatible with Strava & Garmin.
- **Milestones & Gamification**: Streak counter, achievement badges, and personal records showcase.

---

## Screenshots & Visuals

- **Flex Cards Preview**: Open `view_and_download_cards.html` in any modern web browser to preview flex cards.
- **Test Baseline**: Run `./gradlew test` to execute unit tests and screenshot verifications.

---

## Getting Started

### Prerequisites
- [Android Studio Iguana | 2023.2.1](https://developer.android.com/studio) or newer
- JDK 17
- Android SDK 34

### Run Locally
1. Clone the repository:
   ```bash
   git clone https://github.com/yashkoparde/bhagwa.git
   cd bhagwa
   ```
2. Set your Gemini API key in `.env`:
   ```bash
   cp .env.example .env
   # Edit .env and set GEMINI_API_KEY=your_key_here
   ```
3. Open in Android Studio, allow Gradle sync to complete, and run on emulator or physical device.

---

## Architecture
See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) and [docs/LOCATION_TRACKING.md](docs/LOCATION_TRACKING.md) for detailed design specifications.

## License
Distributed under the Apache 2.0 License.
