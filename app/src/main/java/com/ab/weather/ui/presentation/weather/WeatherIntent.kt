package com.ab.weather.ui.presentation.weather

import com.ab.weather.domain.model.City
import com.ab.weather.domain.model.WeatherLocation

sealed interface WeatherIntent {
    data object LoadWeather : WeatherIntent
    data object RefreshWeather : WeatherIntent
    data object LocationPermissionDenied : WeatherIntent

    data class SearchCities(
        val query: String
    ) : WeatherIntent

    data class SelectCity(
        val city: City
    ) : WeatherIntent

    data class SelectSavedLocation(
        val location: WeatherLocation
    ) : WeatherIntent
}