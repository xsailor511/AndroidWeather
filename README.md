# AndroidWeather

A Kotlin Android weather app using Jetpack Compose. Save cities and view current weather conditions plus a 7-day forecast, powered by the free [Open-Meteo API](https://open-meteo.com/) (no API key required).

## Features

- Save and manage multiple cities
- View current weather: temperature, feels like, humidity, wind speed, pressure, weather code
- 7-day forecast with high/low temperatures
- City weather code to emoji icon mapping (WMO standard)
- Search and add new cities via geocoding API

## Tech Stack

- **Language**: Kotlin (JVM 17)
- **UI**: Jetpack Compose (Material 3)
- **Networking**: Ktor Client with OkHttp engine
- **Serialization**: Kotlinx Serialization
- **Persistence**: DataStore Preferences
- **Architecture**: MVVM with a single ViewModel, StateFlow for UI state
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 34 (Android 14)

## Build & Run

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Install on connected device/emulator
./gradlew installDebug
```

Requires Android Studio with Jetpack Compose support.

## Architecture

```
app/
├── WeatherApplication.kt          # Application singleton
├── MainActivity.kt                # Entry point + MainScreen
├── data/
│   ├── model/
│   │   ├── Weather.kt             # Domain models
│   │   └── WeatherResponse.kt     # API response DTOs
│   └── repository/
│       ├── CityRepository.kt      # DataStore city persistence
│       └── WeatherApi.kt          # Ktor HTTP client + Open-Meteo APIs
├── ui/
│   ├── WeatherDetail.kt           # Weather display composables
│   └── AddCityScreen.kt           # City search dialog
└── viewModel/
    └── WeatherViewModel.kt        # Business logic + StateFlow state
```

### Key Patterns

- **Single ViewModel** manages all business logic: city CRUD, weather fetching, search
- **StateFlow** drives all UI state (cities, weather, loading/error states)
- **Sealed interface `WeatherState`** for weather UI state: Loading / Empty / Success / Error
- **Ktor + OkHttp** for HTTP calls to `api.open-meteo.com` and `geocoding-api.open-meteo.com`
- **DataStore Preferences** stores saved cities as JSON-encoded `List<SavedCity>`
- No dependency injection — manual construction via `WeatherViewModelFactory`

### API Mapping

| DTO | Domain Model |
|-----|-------------|
| `WeatherResponse.Current` | `Weather` |
| `WeatherResponse.Daily` | `List<ForecastDay>` |

Weather codes follow [WMO codes](https://open-meteo.com/en/docs), mapped to emoji via `getWeatherIcon()`.

## Project Structure Quick Reference

| File | Purpose |
|------|---------|
| `app/src/main/java/com/example/androidweather/MainActivity.kt` | App entry, MainScreen, CityListRow |
| `app/src/main/java/com/example/androidweather/viewModel/WeatherViewModel.kt` | Business logic + factory |
| `app/src/main/java/com/example/androidweather/ui/WeatherDetail.kt` | Weather display UI + helpers |
| `app/src/main/java/com/example/androidweather/ui/AddCityScreen.kt` | Add city search dialog |
| `app/src/main/java/com/example/androidweather/data/repository/CityRepository.kt` | City persistence |
| `app/src/main/java/com/example/androidweather/data/repository/WeatherApi.kt` | Ktor HTTP client |
| `app/src/main/java/com/example/androidweather/data/model/Weather.kt` | Domain models |
| `app/src/main/java/com/example/androidweather/data/model/WeatherResponse.kt` | API DTOs |

## Notes

- No test infrastructure configured (no androidTest or unit tests)
- No R8/minification enabled (`isMinifyEnabled = false`)
- All strings are hardcoded in Chinese
