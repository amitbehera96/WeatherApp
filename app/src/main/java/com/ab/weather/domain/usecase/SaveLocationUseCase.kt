package com.ab.weather.domain.usecase

import com.ab.weather.domain.model.WeatherLocation
import com.ab.weather.domain.repository.WeatherRepository
import javax.inject.Inject

class SaveLocationUseCase @Inject constructor(
    private val repository: WeatherRepository
) {
    suspend operator fun invoke(location: WeatherLocation) {
        repository.saveLocation(location)
    }
}