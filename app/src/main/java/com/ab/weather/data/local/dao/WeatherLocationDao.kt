package com.ab.weather.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.ab.weather.data.local.entity.WeatherLocationEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface WeatherLocationDao {

    @Upsert
    suspend fun save(location : WeatherLocationEntity)

    @Query("SELECT * FROM weather_location ORDER BY name ASC ")
    fun observeAll() : Flow<List<WeatherLocationEntity>>

    @Query("SELECT * FROM weather_location WHERE id = :id LIMIT 1")
    fun observeById(id: String) : Flow<WeatherLocationEntity?>

    @Query("SELECT * FROM weather_location WHERE id = :id LIMIT 1")
    suspend fun getById(id : String) : WeatherLocationEntity

    @Query("SELECT * FROM weather_location WHERE id = :id LIMIT 1")
    suspend fun getLocation(id: String): WeatherLocationEntity?

    @Query("DELETE FROM weather_location WHERE id = :id")
    suspend fun deleteById(id: String)

}