package com.example.androidweather.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidweather.data.model.Weather

@Composable
fun WeatherDetail(weather: Weather) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // City name
        Text(
            text = weather.cityName,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.DarkGray
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Main temperature
        val bgBrush = getWeatherGradient(weather.weatherCode)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(bgBrush, RoundedCornerShape(24.dp))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                // Weather icon
                Text(
                    text = getWeatherIcon(weather.weatherCode),
                    fontSize = 64.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Temperature
                Text(
                    text = "${weather.temperature.toInt()}",
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "°C",
                    fontSize = 24.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "体感 ${weather.feelsLike.toInt()}°C",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Details grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            WeatherDetailCard("湿度", "${weather.humidity}%", getWeatherIcon(803))
            WeatherDetailCard("风速", "${weather.windSpeed.toInt()} km/h", getWeatherIcon(95))
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            WeatherDetailCard("体感", "${weather.feelsLike.toInt()}°C", getWeatherIcon(804))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Forecast
        Text(
            text = "预报",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.DarkGray
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(weather.forecast.take(7)) { day ->
                ForecastCard(day)
            }
        }
    }
}

@Composable
fun WeatherDetailCard(label: String, value: String, icon: String) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray
            )
        }
    }
}

@Composable
fun ForecastCard(day: com.example.androidweather.data.model.ForecastDay) {
    Card(
        modifier = Modifier
            .width(80.dp)
            .height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround
        ) {
            val dateParts = day.date.split("-")
            Text(
                text = "${dateParts[1]}-${dateParts[2]}",
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(text = getWeatherIcon(day.weatherCode), fontSize = 28.sp)
            Text(
                text = "${day.tempMax.toInt()}°",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Red
            )
            Text(
                text = "${day.tempMin.toInt()}°",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Blue
            )
        }
    }
}

fun getWeatherIcon(weatherCode: Int): String {
    return when {
        weatherCode == 0 -> "🌤️" // Clear sky
        weatherCode in 1..3 -> "☁️" // Partly cloudy
        weatherCode in 45..48 -> "🌫️" // Fog
        weatherCode in 51..57 -> "🌦️" // Drizzle
        weatherCode in 61..67 -> "🌧️" // Rain
        weatherCode in 71..77 -> "🌨️" // Snow
        weatherCode in 80..82 -> "🌦️" // Rain showers
        weatherCode in 85..86 -> "🌨️" // Snow
        weatherCode in 95..99 -> "⛈️" // Thunderstorm
        else -> "🌤️"
    }
}

fun getWeatherGradient(weatherCode: Int): Brush {
    return when {
        weatherCode == 0 -> Brush.verticalGradient(
            colors = listOf(Color(0xFF2196F3), Color(0xFF64B5F6))
        )
        weatherCode in 1..3 -> Brush.verticalGradient(
            colors = listOf(Color(0xFF607D8B), Color(0xFFB0BEC5))
        )
        weatherCode in 45..48 -> Brush.verticalGradient(
            colors = listOf(Color(0xFF78909C), Color(0xFFBDBDBD))
        )
        weatherCode in 51..67 -> Brush.verticalGradient(
            colors = listOf(Color(0xFF37474F), Color(0xFF546E7A))
        )
        weatherCode in 71..82 -> Brush.verticalGradient(
            colors = listOf(Color(0xFFE8EAF6), Color(0xFFC5CAE9))
        )
        weatherCode in 95..99 -> Brush.verticalGradient(
            colors = listOf(Color(0xFF1A237E), Color(0xFF3F51B5))
        )
        else -> Brush.verticalGradient(
            colors = listOf(Color(0xFF2196F3), Color(0xFF64B5F6))
        )
    }
}
