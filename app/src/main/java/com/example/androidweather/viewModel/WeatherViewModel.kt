package com.example.androidweather.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.androidweather.data.model.ForecastDay
import com.example.androidweather.data.model.GeoResult
import com.example.androidweather.data.repository.CityRepository
import com.example.androidweather.data.repository.SavedCity
import com.example.androidweather.data.repository.WeatherApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

class WeatherViewModel(
    private val cityRepository: CityRepository,
    private val weatherApi: WeatherApi
) : ViewModel() {

    // UI State for city list
    private val _cities = MutableStateFlow<List<SavedCity>>(emptyList())
    val cities: StateFlow<List<SavedCity>> = _cities.asStateFlow()

    // UI State for weather data
    private val _weather = MutableStateFlow<WeatherState>(WeatherState.Empty)
    val weather: StateFlow<WeatherState> = _weather.asStateFlow()

    // UI State for search
    private val _searchResults = MutableStateFlow<List<GeoResult>>(emptyList())
    val searchResults: StateFlow<List<GeoResult>> = _searchResults.asStateFlow()

    // UI State for loading
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // UI State for error messages
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadCities()
    }

    private fun loadCities() {
        viewModelScope.launch {
            cityRepository.getCities()
                .catch { e ->
                    android.util.Log.e(TAG, "loadCities failed: ${e.message}")
                    _errorMessage.value = "加载城市列表失败"
                }
                .collectLatest { cities ->
                    _cities.value = cities
                }
        }
    }

    fun addCity(name: String, latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                cityRepository.addCity(SavedCity(name, latitude, longitude))
                loadCities()
                fetchWeather(name, latitude, longitude)
                _errorMessage.value = null
            } catch (e: Exception) {
                android.util.Log.e(TAG, "addCity failed: ${e.message}")
                _errorMessage.value = "添加城市失败"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun removeCity(city: SavedCity) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                cityRepository.removeCity(city.name)
                loadCities()
                _weather.value = WeatherState.Empty
                _errorMessage.value = null
            } catch (e: Exception) {
                android.util.Log.e(TAG, "removeCity failed: ${e.message}")
                _errorMessage.value = "删除城市失败"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchWeather(cityName: String, latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _isLoading.value = true
            _weather.value = WeatherState.Loading
            try {
                val weather = withTimeout(15000) {
                    weatherApi.getWeather(latitude, longitude, cityName)
                }
                _weather.value = WeatherState.Success(weather)
                _errorMessage.value = null
            } catch (e: Exception) {
                android.util.Log.e(TAG, "fetchWeather failed: ${e.message}")
                _weather.value = WeatherState.Error(e.message ?: "获取天气失败")
                _errorMessage.value = "获取天气数据失败"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchCity(cityName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val results = withTimeout(10000) {
                    weatherApi.searchCity(cityName)
                }
                _searchResults.value = results
                _errorMessage.value = null
            } catch (e: Exception) {
                android.util.Log.e(TAG, "searchCity failed: ${e.message}")
                _searchResults.value = emptyList()
                _errorMessage.value = "搜索城市失败"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    sealed interface WeatherState {
        object Loading : WeatherState
        object Empty : WeatherState
        data class Success(val data: com.example.androidweather.data.model.Weather) : WeatherState
        data class Error(val message: String) : WeatherState
    }

    companion object {
        private const val TAG = "WeatherViewModel"
    }
}

// Factory for creating ViewModel
val WeatherViewModelFactory = viewModelFactory {
    initializer {
        val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!
        val repo = CityRepository(app.applicationContext)
        val api = WeatherApi()
        WeatherViewModel(repo, api)
    }
}
