package com.ab.weather.ui.presentation.weather

import com.ab.weather.domain.model.CurrentWeather
import com.ab.weather.domain.model.ForecastItem
import com.ab.weather.domain.model.UserLocation

data class WeatherUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val location: UserLocation? = null,
    val currentWeather: CurrentWeather? = null,
    val forecast: List<ForecastItem> = emptyList(),
    val errorMessage: String? = null
)
