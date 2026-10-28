package com.ab.weather.data.repository

import com.ab.weather.data.remote.WeatherApiService
import com.ab.weather.domain.model.CurrentWeather
import com.ab.weather.domain.model.ForecastItem
import com.ab.weather.domain.model.UserLocation
import com.ab.weather.domain.repository.WeatherRepository
import javax.inject.Inject

class WeatherRepositoryImpl @Inject constructor(
    private val weatherApiService: WeatherApiService
) : WeatherRepository{

    private suspend fun fetchWeather(location: UserLocation) =
        weatherApiService.getWeather(
            latitude = location.latitude,
            longitude = location.longitude
        )
    override suspend fun getCurrentWeather(location: UserLocation): CurrentWeather {
        val response = fetchWeather(location)
        val todayMin = response.daily.minimumTemperatures.firstOrNull()
            ?: response.current.temperature
        val todayMax = response.daily.maximumTemperatures.firstOrNull()
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

    override suspend fun getForecast(location: UserLocation): List<ForecastItem> {
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


    private fun weatherDescription(code: Int): String {
        return when (code) {
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
}