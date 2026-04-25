# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview
AndroidWeather is a Kotlin Android weather app using Jetpack Compose. It lets users save cities and view current weather + 7-day forecast via the free Open-Meteo API (no API key needed).

## Build & Run
```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run on connected device/emulator
./gradlew installDebug
```

Requires Android Studio with Compose support. compileSdk/targetSdk = 34, minSdk = 26, JVM 17.

## Architecture

```
app/
├── WeatherApplication.kt          # Application singleton (Ktor OkHttp engine init)
├── MainActivity.kt                # Entry point + MainScreen + CityListRow composables
├── data/
│   ├── model/
│   │   ├── Weather.kt             # Domain models: Weather, ForecastDay
│   │   └── WeatherResponse.kt     # DTOs: WeatherResponse, Current, Daily, GeoResponse, GeoResult
│   └── repository/
│       ├── CityRepository.kt      # DataStore persistence for saved cities (JSON encoding)
│       └── WeatherApi.kt          # Ktor HTTP client wrapping Open-Meteo + GeoCoding APIs
├── ui/
│   ├── WeatherDetail.kt           # WeatherDetail, WeatherDetailCard, ForecastCard composables
│   └── AddCityScreen.kt           # AddCityScreen dialog with search
└── viewModel/
    └── WeatherViewModel.kt        # Single ViewModel managing cities, weather, search state (StateFlow)
```

### Key patterns
- **Single ViewModel** (`WeatherViewModel`) manages all business logic: city CRUD, weather fetching, city search.
- **StateFlow** drives all UI state (cities, weather, isLoading, errorMessage, searchResults).
- **Ktor + OkHttp engine** for networking with Kotlinx Serialization. Targets `api.open-meteo.com` and `geocoding-api.open-meteo.com`.
- **DataStore Preferences** stores saved cities as a JSON-encoded `List<SavedCity>`.
- **No dependency injection** — `WeatherViewModelFactory` manually constructs dependencies in the ViewModel companion object.
- **Sealed interface `WeatherState`** for weather UI state: Loading / Empty / Success / Error.

### API mapping
- `WeatherResponse.Current` → `Weather` domain model (temperature, feelsLike, humidity, windSpeed, weatherCode, pressure)
- `WeatherResponse.Daily` → `List<ForecastDay>` (date, weatherCode, tempMax, tempMin)
- Weather codes follow WMO codes; `getWeatherIcon()` in WeatherDetail.kt maps them to emoji

## Source File Quick Reference
| Path | Purpose |
|------|---------|
| `app/src/main/java/com/example/androidweather/MainActivity.kt` | App entry, MainScreen, CityListRow |
| `app/src/main/java/com/example/androidweather/viewModel/WeatherViewModel.kt` | All business logic + factory |
| `app/src/main/java/com/example/androidweather/ui/WeatherDetail.kt` | Weather display UI + weather code helpers |
| `app/src/main/java/com/example/androidweather/ui/AddCityScreen.kt` | Add city search dialog |
| `app/src/main/java/com/example/androidweather/data/repository/CityRepository.kt` | City persistence via DataStore |
| `app/src/main/java/com/example/androidweather/data/repository/WeatherApi.kt` | Ktor HTTP client + API calls |
| `app/src/main/java/com/example/androidweather/data/model/Weather.kt` | Domain models |
| `app/src/main/java/com/example/androidweather/data/model/WeatherResponse.kt` | API response DTOs |

## Development Notes
- No test infrastructure configured (no androidTest or unit tests).
- No proguard/r8 rules enabled (`isMinifyEnabled = false`).
- All strings are hardcoded in Chinese — no localization layer.
- The `+` button in the top bar opens the AddCityScreen dialog for adding cities.
- City switching happens via horizontal LazyRow at the top of MainScreen.
