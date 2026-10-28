package com.ab.weather.domain.usecase

import com.ab.weather.domain.model.ForecastItem
import com.ab.weather.domain.model.UserLocation
import com.ab.weather.domain.repository.WeatherRepository
import javax.inject.Inject

class GetForecastUseCase @Inject constructor(
    private val repository: WeatherRepository
) {
    suspend operator fun invoke(
        location: UserLocation
    ): List<ForecastItem> {
        return repository.getForecast(location)
    }
}