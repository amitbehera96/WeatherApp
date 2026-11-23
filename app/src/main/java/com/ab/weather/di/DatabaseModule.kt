package com.ab.weather.di

import android.content.Context
import androidx.room.Room
import com.ab.weather.data.local.WeatherDatabase
import com.ab.weather.data.local.dao.CurrentWeatherDao
import com.ab.weather.data.local.dao.ForecastDao
import com.ab.weather.data.local.dao.WeatherLocationDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): WeatherDatabase {
        return Room.databaseBuilder(
            context,
            WeatherDatabase::class.java,
            "weather_database"
        ).build()
    }

    @Provides
    fun provideLocationDao(db: WeatherDatabase): WeatherLocationDao =
        db.weatherLocationDao()

    @Provides
    fun provideCurrentWeatherDao(db: WeatherDatabase): CurrentWeatherDao =
        db.currentWeatherDao()

    @Provides
    fun provideForecastDao(db: WeatherDatabase): ForecastDao =
        db.forecastDao()
}