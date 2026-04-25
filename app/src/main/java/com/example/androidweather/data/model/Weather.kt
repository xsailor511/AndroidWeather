package com.example.androidweather.data.model

// Domain model
data class Weather(
    val cityName: String,
    val temperature: Double,
    val feelsLike: Double,
    val humidity: Int,
    val windSpeed: Double,
    val weatherCode: Int,
    val forecast: List<ForecastDay>
)

data class ForecastDay(
    val date: String,
    val weatherCode: Int,
    val tempMax: Double,
    val tempMin: Double
)
