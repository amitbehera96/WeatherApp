package com.ab.weather.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "current_weather",
    foreignKeys = [
        ForeignKey(
            entity = WeatherLocationEntity::class,
            parentColumns = ["id"],
            childColumns = ["locationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("locationId")]
)
data class CurrentWeatherEntity(
    @PrimaryKey
    val locationId: String,
    val temperature: Double,
    val feelsLike: Double,
    val minimumTemperature: Double,
    val maximumTemperature: Double,
    val humidity: Int,
    val description: String,
    val icon: String,
    val windSpeed: Double,
    val updatedAt: Long = System.currentTimeMillis()
)