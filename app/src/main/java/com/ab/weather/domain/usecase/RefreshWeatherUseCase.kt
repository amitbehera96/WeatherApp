package com.ab.weather.domain.usecase

import com.ab.weather.domain.model.WeatherLocation
import com.ab.weather.domain.repository.WeatherRepository
import javax.inject.Inject

class RefreshWeatherUseCase @Inject constructor(
    private val repository: WeatherRepository
) {
    suspend operator fun invoke(location: WeatherLocation) {
        repository.refreshWeather(location)
    }
}