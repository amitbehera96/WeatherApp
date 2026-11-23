
package com.ab.weather.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CitySearchResponseDto(
    val results: List<CityDto>? = null
)

@Serializable
data class CityDto(
    val id: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String,
    @SerialName("admin1")
    val admin1: String? = null
)
