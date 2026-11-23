package com.ab.weather.domain.model

 enum class TemperatureUnit {
     CELSIUS,
     FAHRENHEIT
 }

enum class ThemeMode{
    SYSTEM,
    LIGHT,
    DARK
}

data class WeatherSettings(
    val selectedLocationId : String ?=null,
    val temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val backgroundRefreshEnabled : Boolean = true
)