package com.ab.weather.domain.repository

import com.ab.weather.domain.model.UserLocation

interface LocationRepository {

    suspend fun getCurrentLocation() : UserLocation
}