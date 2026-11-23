package com.ab.weather.data.worker

import android.content.Context
import com.ab.weather.data.local.preferences.WeatherSettingsDataStore

suspend fun updateBackgroundRefresh(
    context: Context,
    enabled: Boolean,
    settingsDataStore: WeatherSettingsDataStore
) {
    settingsDataStore.setBackgroundRefreshEnabled(enabled)

    if (enabled) {
        WeatherWorkScheduler.schedule(context)
    } else {
        WeatherWorkScheduler.cancel(context)
    }
}