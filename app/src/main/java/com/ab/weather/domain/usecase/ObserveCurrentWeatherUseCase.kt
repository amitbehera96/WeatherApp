package com.ab.weather.domain.usecase

import com.ab.weather.domain.model.CurrentWeather
import com.ab.weather.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveCurrentWeatherUseCase @Inject constructor(
    private val repository: WeatherRepository
) {
    operator fun invoke(locationId: String): Flow<CurrentWeather?> {
        return repository.observeCurrentWeather(locationId)
    }
}