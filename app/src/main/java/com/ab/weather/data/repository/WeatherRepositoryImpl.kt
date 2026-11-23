
package com.ab.weather.data.repository

import android.content.Context
import android.location.Geocoder
import android.os.Build
import androidx.room.withTransaction
import com.ab.weather.data.local.WeatherDatabase
import com.ab.weather.data.local.dao.CurrentWeatherDao
import com.ab.weather.data.local.dao.ForecastDao
import com.ab.weather.data.local.dao.WeatherLocationDao
import com.ab.weather.data.local.entity.CurrentWeatherEntity
import com.ab.weather.data.local.entity.ForecastEntity
import com.ab.weather.data.local.entity.toDomain
import com.ab.weather.data.local.entity.toEntity
import com.ab.weather.data.remote.GeocodingApiService
import com.ab.weather.data.remote.WeatherApiService
import com.ab.weather.domain.model.City
import com.ab.weather.domain.model.CurrentWeather
import com.ab.weather.domain.model.ForecastItem
import com.ab.weather.domain.model.UserLocation
import com.ab.weather.domain.model.WeatherLocation as DomainWeatherLocation
import com.ab.weather.domain.repository.WeatherRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class WeatherRepositoryImpl @Inject constructor(
    private val weatherApiService: WeatherApiService,
    private val geocodingApiService: GeocodingApiService,
    private val weatherLocationDao: WeatherLocationDao,
    private val currentWeatherDao: CurrentWeatherDao,
    private val forecastDao: ForecastDao,
    private val database: WeatherDatabase,
    @ApplicationContext private val context: Context
) : WeatherRepository {

    private suspend fun fetchWeather(location: UserLocation) =
        weatherApiService.getWeather(
            latitude = location.latitude,
            longitude = location.longitude
        )

    // Existing weather methods

    override suspend fun getCurrentWeather(
        location: UserLocation
    ): CurrentWeather {

        val response = fetchWeather(location)

        val todayMin =
            response.daily.minimumTemperatures.firstOrNull()
                ?: response.current.temperature

        val todayMax =
            response.daily.maximumTemperatures.firstOrNull()
                ?: response.current.temperature

        return CurrentWeather(
            cityName = "Current location",
            country = "",
            temperature = response.current.temperature,
            feelsLike = response.current.feelsLike,
            minimumTemperature = todayMin,
            maximumTemperature = todayMax,
            humidity = response.current.humidity,
            description = weatherDescription(
                response.current.weatherCode
            ),
            icon = response.current.weatherCode.toString(),
            windSpeed = response.current.windSpeed
        )
    }

    override suspend fun getForecast(
        location: UserLocation
    ): List<ForecastItem> {

        val daily = fetchWeather(location).daily

        return daily.time.indices.map { index ->

            val code = daily.weatherCodes.getOrElse(index) { 0 }

            ForecastItem(
                dateTime = daily.time[index],
                minimumTemperature =
                    daily.minimumTemperatures.getOrElse(index) { 0.0 },
                maximumTemperature =
                    daily.maximumTemperatures.getOrElse(index) { 0.0 },
                description = weatherDescription(code),
                icon = code.toString()
            )
        }
    }

    // City search
    override suspend fun searchCities(query: String): List<City> {

        if (query.isBlank()) return emptyList()

        val response = geocodingApiService.searchCities(
            query = query.trim()
        )

        return response.results.orEmpty().map { city ->
            City(
                id = "city:${city.id}",
                name = city.name,
                latitude = city.latitude,
                longitude = city.longitude,
                country = city.country,
                admin1 = city.admin1
            )
        }
    }

    // GPS location

    override suspend fun createGpsLocation(
        latitude: Double,
        longitude: Double
    ): DomainWeatherLocation = withContext(Dispatchers.IO) {

        val address = try {

            val geocoder = Geocoder(context, Locale.getDefault())

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                suspendCancellableCoroutine { continuation ->

                    geocoder.getFromLocation(
                        latitude,
                        longitude,
                        1
                    ) { addresses ->

                        val result = addresses.firstOrNull()

                        if (continuation.isActive) {
                            continuation.resume(
                                result?.let {
                                    Triple(
                                        it.locality
                                            ?: it.subAdminArea
                                            ?: it.adminArea
                                            ?: "Current location",
                                        it.adminArea
                                            ?: it.subAdminArea
                                            ?: "",
                                        it.countryName ?: ""
                                    )
                                }
                            )
                        }
                    }
                }

            } else {

                @Suppress("DEPRECATION")
                geocoder.getFromLocation(
                    latitude,
                    longitude,
                    1
                )?.firstOrNull()?.let {
                    Triple(
                        it.locality
                            ?: it.subAdminArea
                            ?: it.adminArea
                            ?: "Current location",
                        it.adminArea ?: it.subAdminArea ?: "",
                        it.countryName ?: ""
                    )
                }
            }

        } catch (e: Exception) {
            null
        }

        val location = DomainWeatherLocation(
            id = "gps:${"%.5f".format(Locale.US, latitude)}:" +
                    "%.5f".format(Locale.US, longitude),
            name = address?.first ?: "Current location",
            country = address?.third ?: "",
            latitude = latitude,
            longitude = longitude,
            isCurrentLocation = true
        )

        weatherLocationDao.save(location.toEntity())

        location
    }

    // Saved locations

    override fun getSavedLocations(): Flow<List<DomainWeatherLocation>> =
        weatherLocationDao.observeAll().map { locations ->
            locations.map { it.toDomain() }
        }

    override suspend fun saveLocation(location: DomainWeatherLocation) {
        weatherLocationDao.save(location.toEntity())
    }

    override suspend fun getLocationById(
        locationId: String
    ): DomainWeatherLocation? {
        return weatherLocationDao.getLocation(locationId)?.toDomain()
    }

    override suspend fun deleteLocation(locationId: String) {
        weatherLocationDao.deleteById(locationId)
    }

    // Observe current weather

    override fun observeCurrentWeather(
        locationId: String
    ): Flow<CurrentWeather?> {

        return combine(
            currentWeatherDao.observe(locationId),
            weatherLocationDao.observeById(locationId)
        ) { weather, location ->

            if (weather == null || location == null) {
                null
            } else {
                CurrentWeather(
                    cityName = location.name,
                    country = location.country,
                    temperature = weather.temperature,
                    feelsLike = weather.feelsLike,
                    minimumTemperature = weather.minimumTemperature,
                    maximumTemperature = weather.maximumTemperature,
                    humidity = weather.humidity,
                    description = weather.description,
                    icon = weather.icon,
                    windSpeed = weather.windSpeed
                )
            }
        }
    }

    // Observe forecast

    override fun observeForecast(
        locationId: String
    ): Flow<List<ForecastItem>> {

        return forecastDao.observe(locationId).map { forecasts ->
            forecasts.map { forecast ->
                forecast.toDomain()
            }
        }
    }

    // Refresh and save weather


    override suspend fun refreshWeather(
        location: DomainWeatherLocation
    ) {

        val userLocation = UserLocation(
            latitude = location.latitude,
            longitude = location.longitude
        )

        // Fetch current weather and forecast from API
        val current = getCurrentWeather(userLocation)
        val forecast = getForecast(userLocation)

        // Save everything in a single Room transaction
        database.withTransaction {

            // Save location
            weatherLocationDao.save(
                location.toEntity()
            )

            // Save current weather
            currentWeatherDao.save(
                CurrentWeatherEntity(
                    locationId = location.id,
                    temperature = current.temperature,
                    feelsLike = current.feelsLike,
                    minimumTemperature = current.minimumTemperature,
                    maximumTemperature = current.maximumTemperature,
                    humidity = current.humidity,
                    description = current.description,
                    icon = current.icon,
                    windSpeed = current.windSpeed
                )
            )

            // Replace existing forecast
            forecastDao.clear(location.id)

            forecastDao.insert(
                forecast.map { item ->
                    item.toEntity(location.id)
                }
            )
        }
    }


    private fun weatherDescription(code: Int): String =
        when (code) {
            0 -> "Clear sky"
            1 -> "Mainly clear"
            2 -> "Partly cloudy"
            3 -> "Overcast"
            45, 48 -> "Fog"
            51, 53, 55 -> "Drizzle"
            56, 57 -> "Freezing drizzle"
            61, 63, 65 -> "Rain"
            66, 67 -> "Freezing rain"
            71, 73, 75, 77 -> "Snow"
            80, 81, 82 -> "Rain showers"
            85, 86 -> "Snow showers"
            95 -> "Thunderstorm"
            96, 99 -> "Thunderstorm with hail"
            else -> "Unknown conditions"
        }
}
