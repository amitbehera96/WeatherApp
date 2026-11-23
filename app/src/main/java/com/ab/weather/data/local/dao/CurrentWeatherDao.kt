package com.ab.weather.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.ab.weather.data.local.entity.CurrentWeatherEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface CurrentWeatherDao {

    @Upsert
    suspend fun save(weather: CurrentWeatherEntity)

    @Query("SELECT * FROM current_weather WHERE locationId = :locationId")
    fun observe(locationId: String): Flow<CurrentWeatherEntity?>

    @Query("SELECT * FROM current_weather WHERE locationId = :locationId")
    suspend fun get(locationId: String): CurrentWeatherEntity?
}