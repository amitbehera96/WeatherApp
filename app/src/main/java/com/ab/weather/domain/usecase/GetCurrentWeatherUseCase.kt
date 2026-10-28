package com.ab.weather.domain.usecase

import com.ab.weather.domain.model.CurrentWeather
import com.ab.weather.domain.model.UserLocation
import com.ab.weather.domain.repository.WeatherRepository
import javax.inject.Inject

class GetCurrentWeatherUseCase @Inject constructor(
    private val repository: WeatherRepository
) {
    suspend operator fun invoke(
        location: UserLocation
    ): CurrentWeather {
        return repository.getCurrentWeather(location)
    }
}