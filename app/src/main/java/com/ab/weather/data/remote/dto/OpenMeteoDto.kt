package com.ab.weather.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class OpenMeteoResponseDto (
    val current : CurrentWeatherDto,
    val daily : DailyWeatherDto
)


@Serializable
data class CurrentWeatherDto(
    val time: String = "",
    @SerialName("temperature_2m")
    val temperature: Double = 0.0,
    @SerialName("relative_humidity_2m")
    val humidity: Int = 0,
    @SerialName("apparent_temperature")
    val feelsLike: Double = 0.0,
    @SerialName("weather_code")
    val weatherCode: Int = 0,
    @SerialName("wind_speed_10m")
    val windSpeed: Double = 0.0
)


@Serializable
data class DailyWeatherDto(
    val time: List<String> = emptyList(),
    @SerialName("weather_code")
    val weatherCodes: List<Int> = emptyList(),
    @SerialName("temperature_2m_min")
    val minimumTemperatures: List<Double> = emptyList(),
    @SerialName("temperature_2m_max")
    val maximumTemperatures: List<Double> = emptyList()
)