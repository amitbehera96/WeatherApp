
package com.ab.weather.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.ab.weather.domain.model.ForecastItem

@Entity(
    tableName = "weather_forecast",
    primaryKeys = ["locationId", "dateTime"],
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
data class ForecastEntity(
    val locationId: String,
    val dateTime: String,
    val minimumTemperature: Double,
    val maximumTemperature: Double,
    val description: String,
    val icon: String,
    val updatedAt: Long = System.currentTimeMillis()
)

// Room Entity -> Domain Model
fun ForecastEntity.toDomain() = ForecastItem(
    dateTime = dateTime,
    minimumTemperature = minimumTemperature,
    maximumTemperature = maximumTemperature,
    description = description,
    icon = icon
)

// Domain Model -> Room Entity
fun ForecastItem.toEntity(locationId: String) = ForecastEntity(
    locationId = locationId,
    dateTime = dateTime,
    minimumTemperature = minimumTemperature,
    maximumTemperature = maximumTemperature,
    description = description,
    icon = icon
)
