package com.example.androidweather.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import kotlinx.serialization.json.Json

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Serializable
data class SavedCity(
    val name: String,
    val latitude: Double,
    val longitude: Double
)

class CityRepository(private val context: Context) {

    companion object {
        private val CITY_LIST_KEY = stringPreferencesKey("city_list")
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    suspend fun addCity(city: SavedCity) {
        val cities = getCities().first().toMutableList()
        if (!cities.any { it.name == city.name }) {
            cities.add(city)
            context.dataStore.edit { preferences ->
                preferences[CITY_LIST_KEY] = json.encodeToString(serializer<List<SavedCity>>(), cities)
            }
        }
    }

    suspend fun removeCity(cityName: String) {
        context.dataStore.edit { preferences ->
            val current = getCities().first()
            preferences[CITY_LIST_KEY] = json.encodeToString(serializer<List<SavedCity>>(),
                current.filter { it.name != cityName }
            )
        }
    }

    fun getCities(): Flow<List<SavedCity>> {
        return context.dataStore.data.map { preferences ->
            val serialized = preferences[CITY_LIST_KEY] ?: "[]"
            try {
                json.decodeFromString<List<SavedCity>>(serialized)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}
