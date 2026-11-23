
package com.ab.weather.domain.repository

import com.ab.weather.domain.model.City
import com.ab.weather.domain.model.CurrentWeather
import com.ab.weather.domain.model.ForecastItem
import com.ab.weather.domain.model.UserLocation
import com.ab.weather.domain.model.WeatherLocation
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {

    // Existing weather API operations
    suspend fun getCurrentWeather(
        location: UserLocation
    ): CurrentWeather

    suspend fun getForecast(
        location: UserLocation
    ): List<ForecastItem>

    // City search
    suspend fun searchCities(
        query: String
    ): List<City>

    // GPS location and reverse geocoding
    suspend fun createGpsLocation(
        latitude: Double,
        longitude: Double
    ): WeatherLocation

    // Saved locations
    fun getSavedLocations(): Flow<List<WeatherLocation>>

    suspend fun saveLocation(
        location: WeatherLocation
    )

    suspend fun getLocationById(
        locationId: String
    ): WeatherLocation?

    suspend fun deleteLocation(
        locationId: String
    )

    // Observe cached current weather
    fun observeCurrentWeather(
        locationId: String
    ): Flow<CurrentWeather?>

    // Observe cached forecast
    fun observeForecast(
        locationId: String
    ): Flow<List<ForecastItem>>

    // Fetch weather and persist it in Room
    suspend fun refreshWeather(
        location: WeatherLocation
    )
}
