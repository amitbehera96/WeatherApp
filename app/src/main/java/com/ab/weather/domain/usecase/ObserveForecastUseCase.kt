package com.ab.weather.domain.usecase

import com.ab.weather.domain.model.ForecastItem
import com.ab.weather.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveForecastUseCase @Inject constructor(
    private val repository: WeatherRepository
) {
    operator fun invoke(locationId: String): Flow<List<ForecastItem>> {
        return repository.observeForecast(locationId)
    }
}