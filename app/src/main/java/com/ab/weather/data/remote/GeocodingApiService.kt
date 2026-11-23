
package com.ab.weather.data.remote

import com.ab.weather.data.remote.dto.CitySearchResponseDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

interface GeocodingApiService {

    @GET("v1/search")
    suspend fun searchCities(
        @Query("name") query: String,
        @Query("count") count: Int = 10,
        @Query("language") language: String = "en",
        @Query("format") format: String = "json"
    ): CitySearchResponseDto
}
