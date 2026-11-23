package com.ab.weather.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ab.weather.data.local.entity.ForecastEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface ForecastDao {

    @Query(
        "SELECT * FROM weather_forecast " +
                "WHERE locationId = :locationId ORDER BY dateTime ASC"
    )
    fun observe(locationId: String): Flow<List<ForecastEntity>>

    @Query(
        "SELECT * FROM weather_forecast " +
                "WHERE locationId = :locationId ORDER BY dateTime ASC"
    )
    suspend fun get(locationId: String): List<ForecastEntity>

    @Query("DELETE FROM weather_forecast WHERE locationId = :locationId")
    suspend fun clear(locationId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(items: List<ForecastEntity>)
}