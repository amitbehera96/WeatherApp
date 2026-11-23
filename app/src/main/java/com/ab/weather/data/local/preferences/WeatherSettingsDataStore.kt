
package com.ab.weather.data.local.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ab.weather.domain.model.TemperatureUnit
import com.ab.weather.domain.model.ThemeMode
import com.ab.weather.domain.model.WeatherSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.weatherSettingsDataStore by preferencesDataStore(
    name = "weather_settings"
)

class WeatherSettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private object Keys {
        val TEMPERATURE_UNIT = stringPreferencesKey("temperature_unit")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val SELECTED_LOCATION_ID = stringPreferencesKey("selected_location_id")
        val BACKGROUND_REFRESH = booleanPreferencesKey("background_refresh")
    }

    val settings: Flow<WeatherSettings> =
        context.weatherSettingsDataStore.data.map { preferences ->
            WeatherSettings(
                temperatureUnit = runCatching {
                    TemperatureUnit.valueOf(
                        preferences[Keys.TEMPERATURE_UNIT]
                            ?: TemperatureUnit.CELSIUS.name
                    )
                }.getOrDefault(TemperatureUnit.CELSIUS),

                themeMode = runCatching {
                    ThemeMode.valueOf(
                        preferences[Keys.THEME_MODE]
                            ?: ThemeMode.SYSTEM.name
                    )
                }.getOrDefault(ThemeMode.SYSTEM),

                selectedLocationId =
                    preferences[Keys.SELECTED_LOCATION_ID],

                backgroundRefreshEnabled =
                    preferences[Keys.BACKGROUND_REFRESH] ?: true
            )
        }

    suspend fun setTemperatureUnit(unit: TemperatureUnit) {
        context.weatherSettingsDataStore.edit {
            it[Keys.TEMPERATURE_UNIT] = unit.name
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.weatherSettingsDataStore.edit {
            it[Keys.THEME_MODE] = mode.name
        }
    }

    suspend fun setSelectedLocation(locationId: String?) {
        context.weatherSettingsDataStore.edit {
            if (locationId == null) {
                it.remove(Keys.SELECTED_LOCATION_ID)
            } else {
                it[Keys.SELECTED_LOCATION_ID] = locationId
            }
        }
    }

    suspend fun setBackgroundRefreshEnabled(enabled: Boolean) {
        context.weatherSettingsDataStore.edit {
            it[Keys.BACKGROUND_REFRESH] = enabled
        }
    }
}
