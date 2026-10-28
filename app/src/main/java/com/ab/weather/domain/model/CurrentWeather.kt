package com.ab.weather.domain.model

data class CurrentWeather(
    val cityName: String,
    val country: String,
    val temperature: Double,
    val feelsLike: Double,
    val minimumTemperature: Double,
    val maximumTemperature: Double,
    val humidity: Int,
    val description: String,
    val icon: String,
    val windSpeed: Double

)
