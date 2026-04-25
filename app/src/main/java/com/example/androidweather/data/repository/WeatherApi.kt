package com.example.androidweather.data.repository

import com.example.androidweather.BuildConfig
import com.example.androidweather.data.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.*
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.ProxySelector
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds

class WeatherApi {

    private val client = HttpClient(OkHttp) {
        engine {
            config {
                connectTimeout(CONNECT_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
                readTimeout(READ_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
                writeTimeout(WRITE_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
                if (BuildConfig.DEBUG) {
                    try {
                        val proxy = Proxy(Proxy.Type.HTTP, InetSocketAddress(PROXY_HOST, PROXY_PORT))
                        proxy(proxy)
                        android.util.Log.i(TAG, "Proxy enabled: $PROXY_HOST:$PROXY_PORT")
                    } catch (e: Exception) {
                        android.util.Log.w(TAG, "Failed to set up proxy: ${e.message}")
                    }
                }
            }
        }
        install(Logging) {
            level = LogLevel.INFO
            logger = object : Logger {
                override fun log(message: String) {
                    android.util.Log.d("KTOR", message)
                }
            }
        }
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    suspend fun searchCity(cityName: String): List<GeoResult> = withContext(Dispatchers.IO) {
        try {
            android.util.Log.i(TAG, "searchCity: $cityName")
            val url = "https://geocoding-api.open-meteo.com/v1/search?name=${java.net.URLEncoder.encode(cityName, "UTF-8")}&count=10&language=zh"
            val response = client.get(url)
            val geoResponse = response.body<GeoResponse>()
            android.util.Log.i(TAG, "searchCity result: ${geoResponse.results?.size ?: 0} results")
            geoResponse.results ?: emptyList()
        } catch (e: Exception) {
            android.util.Log.e(TAG, "searchCity failed: ${e.message}")
            emptyList()
        }
    }

    suspend fun getWeather(latitude: Double, longitude: Double, cityName: String): Weather = withContext(Dispatchers.IO) {
        try {
            android.util.Log.i(TAG, "getWeather: $cityName ($latitude, $longitude)")
            val url = "https://api.open-meteo.com/v1/forecast?latitude=${latitude}&longitude=${longitude}&current=temperature,apparent_temperature,relative_humidity_2m,wind_speed_10m,weather_code&daily=weather_code,temperature_2m_max,temperature_2m_min&timezone=auto"
            val response = client.get(url)
            val weatherResponse = response.body<WeatherResponse>()

            val current = weatherResponse.current ?: throw IllegalStateException("No current weather data")
            val daily = weatherResponse.daily

            val forecast = if (daily?.time != null && daily.weatherCode != null && daily.tempMax != null && daily.tempMin != null) {
                daily.time.mapIndexed { index, date ->
                    ForecastDay(
                        date = date,
                        weatherCode = daily.weatherCode.getOrNull(index) ?: -1,
                        tempMax = daily.tempMax.getOrNull(index) ?: 0.0,
                        tempMin = daily.tempMin.getOrNull(index) ?: 0.0
                    )
                }
            } else {
                emptyList()
            }

            Weather(
                cityName = cityName,
                temperature = current.temperature ?: 0.0,
                feelsLike = current.apparentTemperature ?: 0.0,
                humidity = current.relativeHumidity ?: 0,
                windSpeed = current.windSpeed ?: 0.0,
                weatherCode = current.weatherCode ?: -1,
                forecast = forecast
            ).also {
                android.util.Log.i(TAG, "getWeather success: ${it.temperature}°C")
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "getWeather failed: ${e.message}")
            throw e
        }
    }

    companion object {
        private const val TAG = "WeatherApi"
        private const val PROXY_HOST = "localhost"
        private const val PROXY_PORT = 10808
        private const val CONNECT_TIMEOUT = 15000
        private const val READ_TIMEOUT = 15000
        private const val WRITE_TIMEOUT = 15000
    }
}
