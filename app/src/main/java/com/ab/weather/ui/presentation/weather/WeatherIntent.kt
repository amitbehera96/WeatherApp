package com.ab.weather.ui.presentation.weather

sealed interface WeatherIntent {
    data object LoadWeather : WeatherIntent
    data object RefreshWeather : WeatherIntent
    data object LocationPermissionDenied : WeatherIntent
}