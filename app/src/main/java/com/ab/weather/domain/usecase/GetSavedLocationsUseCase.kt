package com.ab.weather.domain.usecase

import com.ab.weather.domain.model.WeatherLocation
import com.ab.weather.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSavedLocationsUseCase @Inject constructor(
    private val repository: WeatherRepository
) {
    operator fun invoke(): Flow<List<WeatherLocation>> {
        return repository.getSavedLocations()
    }
}