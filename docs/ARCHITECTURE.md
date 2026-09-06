# Bhagwa Architecture Overview

Bhagwa is built using modern Android architecture recommendations:
- **Clean Architecture** with domain, data, and presentation layers
- **Jetpack Compose** for 100% declarative UI rendering
- **StateFlow & Coroutines** for reactive, lifecycle-aware state management
- **Fused Location Provider** for battery-efficient, high-precision GPS tracking
- **Google Gemini API** for personalized AI workout coaching insights

```
+-----------------------------------------------------------+
|                     Presentation Layer                     |
|  TrackWorkoutScreen | ActivityFeedScreen | DashboardScreen|
|  RouteMapCanvas     | ShareableWorkoutCardModal           |
+-----------------------------+-----------------------------+
                              |
                              v
+-----------------------------------------------------------+
|                      ViewModel Layer                      |
|                     WorkoutViewModel                      |
+-----------------------------+-----------------------------+
                              |
                              v
+-----------------------------------------------------------+
|                        Data Layer                         |
|   LocalWorkoutRepository    |       LocationTracker       |
|   GeminiCoachingService     |       GpxExporter           |
+-----------------------------------------------------------+
```
