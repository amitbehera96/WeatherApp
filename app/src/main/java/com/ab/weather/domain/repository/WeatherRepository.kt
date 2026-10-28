package com.ab.weather.domain.repository

import com.ab.weather.domain.model.CurrentWeather
import com.ab.weather.domain.model.ForecastItem
import com.ab.weather.domain.model.UserLocation

interface WeatherRepository {
    suspend fun getCurrentWeather(
        location: UserLocation
    ): CurrentWeather

    suspend fun getForecast(
        location: UserLocation
    ): List<ForecastItem>
}