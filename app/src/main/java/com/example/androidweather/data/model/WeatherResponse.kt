package com.example.androidweather.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Geocoding API response
@Serializable
data class GeoResponse(
    @SerialName("results")
    val results: List<GeoResult>? = null
)

@Serializable
data class GeoResult(
    @SerialName("id")
    val id: Int? = null,

    @SerialName("name")
    val name: String? = null,

    @SerialName("latitude")
    val latitude: Double? = null,

    @SerialName("longitude")
    val longitude: Double? = null,

    @SerialName("country")
    val country: String? = null,

    @SerialName("admin1")
    val admin1: String? = null
)

// Weather API response
@Serializable
data class WeatherResponse(
    @SerialName("current")
    val current: Current? = null,

    @SerialName("daily")
    val daily: Daily? = null,

    @SerialName("timezone")
    val timezone: String? = null
)

@Serializable
data class Current(
    @SerialName("temperature")
    val temperature: Double? = null,

    @SerialName("apparent_temperature")
    val apparentTemperature: Double? = null,

    @SerialName("relative_humidity_2m")
    val relativeHumidity: Int? = null,

    @SerialName("wind_speed_10m")
    val windSpeed: Double? = null,

    @SerialName("weather_code")
    val weatherCode: Int? = null,

)

@Serializable
data class Daily(
    @SerialName("time")
    val time: List<String>? = null,

    @SerialName("weather_code")
    val weatherCode: List<Int>? = null,

    @SerialName("temperature_2m_max")
    val tempMax: List<Double>? = null,

    @SerialName("temperature_2m_min")
    val tempMin: List<Double>? = null
)
