package com.ab.weather.domain.usecase

import com.ab.weather.domain.model.City
import com.ab.weather.domain.repository.WeatherRepository
import javax.inject.Inject

class SearchCitiesUseCase @Inject constructor(
    private val repository: WeatherRepository
) {
    suspend operator fun invoke(query: String): List<City> {
        return repository.searchCities(query)
    }
}