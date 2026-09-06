# GPS Location Tracking Engine

The tracking engine uses Google Play Services `FusedLocationProviderClient`:
- Priority: `PRIORITY_HIGH_ACCURACY`
- Interval: 2,000 ms (adaptive up to 5,000 ms during stationary periods)
- Distance filter: 2 meters
- Accuracy filter: Points with accuracy > 25 meters are rejected to prevent indoor drift.
- Distance calculation: Haversine great-circle distance algorithm.
- Speed smoothing: Exponential moving average (EMA) filter over a 5-point sliding window.
