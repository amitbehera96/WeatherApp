package com.ab.weather.domain.model

data class WeatherLocation(
    val id: String,
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val isCurrentLocation: Boolean = false
)
