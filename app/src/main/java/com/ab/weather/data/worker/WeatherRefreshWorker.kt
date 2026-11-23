package com.ab.weather.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ab.weather.data.local.preferences.WeatherSettingsDataStore
import com.ab.weather.domain.repository.WeatherRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.io.IOException


@HiltWorker
class WeatherRefreshWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val settingsDataStore: WeatherSettingsDataStore,
    private val weatherRepository: WeatherRepository
) : CoroutineWorker(context,workerParameters){

    override suspend fun doWork(): Result {
        return try {

            val settings = settingsDataStore.settings.first()

            if (!settings.backgroundRefreshEnabled) {
                return Result.success()
            }

            val locationId = settings.selectedLocationId
                ?: return Result.success()

            val location = weatherRepository.getLocationById(locationId)
                ?: return Result.success()

            weatherRepository.refreshWeather(location)

            Result.success()

        } catch (exception: IOException) {
            Result.retry()
        } catch (exception: Exception) {
            Result.failure()
        }
    }

}