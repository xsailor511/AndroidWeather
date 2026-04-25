package com.example.androidweather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androidweather.data.repository.SavedCity
import com.example.androidweather.ui.AddCityScreen
import com.example.androidweather.ui.WeatherDetail
import com.example.androidweather.viewModel.WeatherViewModel
import com.example.androidweather.viewModel.WeatherViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: WeatherViewModel = viewModel(factory = WeatherViewModelFactory)
            var selectedCity by remember { mutableStateOf<SavedCity?>(null) }
            var showAddCity by remember { mutableStateOf(false) }
            val snackbarHostState = remember { SnackbarHostState() }

            // Collect state
            val cities by viewModel.cities.collectAsState()
            val weather by viewModel.weather.collectAsState()
            val isLoading by viewModel.isLoading.collectAsState()
            val errorMessage by viewModel.errorMessage.collectAsState()

            // Set default city if none selected
            LaunchedEffect(selectedCity, cities) {
                if (selectedCity == null && cities.isNotEmpty()) {
                    selectedCity = cities.first()
                    viewModel.fetchWeather(
                        selectedCity!!.name,
                        selectedCity!!.latitude,
                        selectedCity!!.longitude
                    )
                }
            }

            if (showAddCity) {
                AddCityScreen(
                    onDismiss = { showAddCity = false },
                    onCitySelected = { geoResult ->
                        selectedCity = SavedCity(geoResult.name ?: "", geoResult.latitude ?: 0.0, geoResult.longitude ?: 0.0)
                        showAddCity = false
                        viewModel.fetchWeather(
                            geoResult.name ?: "",
                            geoResult.latitude ?: 0.0,
                            geoResult.longitude ?: 0.0
                        )
                    },
                    viewModel = viewModel
                )
            } else {
                MainScreen(
                    cities = cities,
                    weather = weather,
                    isLoading = isLoading,
                    errorMessage = errorMessage,
                    selectedCity = selectedCity,
                    onCitySelected = { city ->
                        selectedCity = city
                        viewModel.fetchWeather(city.name, city.latitude, city.longitude)
                    },
                    onRemoveCity = { city ->
                        if (selectedCity?.name == city.name) {
                            selectedCity = null
                        }
                        viewModel.removeCity(city)
                    },
                    onAddCity = { showAddCity = true },
                    onClearError = { viewModel.clearError() },
                    snackbarHostState = snackbarHostState
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    cities: List<SavedCity>,
    weather: WeatherViewModel.WeatherState,
    isLoading: Boolean,
    errorMessage: String?,
    selectedCity: SavedCity?,
    onCitySelected: (SavedCity) -> Unit,
    onRemoveCity: (SavedCity) -> Unit,
    onAddCity: () -> Unit,
    onClearError: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "天气",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                },
                actions = {
                    IconButton(onClick = onAddCity) {
                        Icon(Icons.Default.Add, contentDescription = "添加城市")
                    }
                }
            )
        },
        snackbarHost = {
            if (errorMessage != null) {
                SnackbarHost(snackbarHostState) { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = Color.White
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // City list (horizontal scroll)
            if (cities.isNotEmpty()) {
                CityListRow(
                    cities = cities,
                    selectedCity = selectedCity?.name,
                    onCitySelected = onCitySelected,
                    onRemoveCity = onRemoveCity
                )
            }

            // Weather content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when (weather) {
                    is WeatherViewModel.WeatherState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    is WeatherViewModel.WeatherState.Empty -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "暂无天气数据",
                                    fontSize = 18.sp,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "点击右上角 + 添加城市",
                                    fontSize = 14.sp,
                                    color = Color.Gray.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                    is WeatherViewModel.WeatherState.Success -> {
                        WeatherDetail(weather.data)
                    }
                    is WeatherViewModel.WeatherState.Error -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = weather.message,
                                    fontSize = 16.sp,
                                    color = Color.Red
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = onClearError) {
                                    Text("重试")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CityListRow(
    cities: List<SavedCity>,
    selectedCity: String?,
    onCitySelected: (SavedCity) -> Unit,
    onRemoveCity: (SavedCity) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(cities.size) { index ->
            val city = cities[index]
            val isSelected = city.name == selectedCity
            Card(
                modifier = Modifier
                    .height(48.dp)
                    .clickable { onCitySelected(city) },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = city.name,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        modifier = Modifier.size(24.dp),
                        onClick = { onRemoveCity(city) }
                    ) {
                        Text(
                            text = "×",
                            fontSize = 16.sp,
                            color = if (isSelected) Color.White.copy(alpha = 0.7f)
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
