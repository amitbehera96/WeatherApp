package com.ab.weather.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ab.weather.data.local.dao.CurrentWeatherDao
import com.ab.weather.data.local.dao.ForecastDao
import com.ab.weather.data.local.dao.WeatherLocationDao
import com.ab.weather.data.local.entity.CurrentWeatherEntity
import com.ab.weather.data.local.entity.ForecastEntity
import com.ab.weather.data.local.entity.WeatherLocationEntity


@Database( entities = [
    WeatherLocationEntity::class,
    CurrentWeatherEntity::class,
    ForecastEntity::class
],
    version = 1,
    exportSchema = false)
abstract class WeatherDatabase : RoomDatabase() {

    abstract fun weatherLocationDao(): WeatherLocationDao

    abstract fun currentWeatherDao(): CurrentWeatherDao

    abstract fun forecastDao(): ForecastDao
}