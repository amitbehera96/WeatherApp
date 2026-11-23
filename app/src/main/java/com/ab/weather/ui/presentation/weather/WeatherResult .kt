
package com.ab.weather.ui.presentation.weather

import com.ab.weather.domain.model.CurrentWeather
import com.ab.weather.domain.model.ForecastItem
import com.ab.weather.domain.model.UserLocation
import com.ab.weather.domain.model.WeatherLocation


sealed interface WeatherResult {

    data class Loading(
        val isRefresh: Boolean
    ) : WeatherResult

    data class Success(
        val location: UserLocation,
        val currentWeather: CurrentWeather,
        val forecast: List<ForecastItem>
    ) : WeatherResult

    data class Failure(
        val message: String
    ) : WeatherResult

    data object PermissionDenied : WeatherResult

    data class SearchStarted(
        val query: String
    ) : WeatherResult

    data class SearchSuccess(
        val cities: List<com.ab.weather.domain.model.City>
    ) : WeatherResult

    data class SearchFailure(
        val message: String
    ) : WeatherResult

    data class SavedLocationsUpdated(
        val locations: List<WeatherLocation>
    ) : WeatherResult

    data class SelectedLocationChanged(
        val location: WeatherLocation
    ) : WeatherResult

    data class CachedWeatherUpdated(
        val currentWeather: CurrentWeather?,
        val forecast: List<ForecastItem>
    ) : WeatherResult
}


