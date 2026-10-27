package com.ab.weather.domain.usecase

import com.ab.weather.domain.model.UserLocation
import com.ab.weather.domain.repository.LocationRepository
import javax.inject.Inject

class GetCurrentUserLocationUseCase @Inject constructor(
    private val repository: LocationRepository
) {
    suspend operator fun invoke () : UserLocation{
        return repository.getCurrentLocation()
    }
}