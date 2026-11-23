package com.ab.weather.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ab.weather.domain.model.WeatherLocation

@Entity(tableName = "weather_location")
data class WeatherLocationEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val region: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val isCurrentLocation: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

fun WeatherLocationEntity.toDomain() = WeatherLocation(
    id = id,
    name = name,
    country = country,
    latitude = latitude,
    longitude = longitude,
    isCurrentLocation = isCurrentLocation
)

fun WeatherLocation.toEntity() = WeatherLocationEntity(
    id = id,
    name = name,
    region = "",
    country = country,
    latitude = latitude,
    longitude = longitude,
    isCurrentLocation = isCurrentLocation
)