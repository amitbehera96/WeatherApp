package com.ab.weather.domain.model

data class ForecastItem(
    val dateTime: String,
    val minimumTemperature: Double,
    val maximumTemperature: Double,
    val description: String,
    val icon: String
)
