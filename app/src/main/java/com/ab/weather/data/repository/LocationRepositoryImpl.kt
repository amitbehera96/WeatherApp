package com.ab.weather.data.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import com.ab.weather.domain.model.UserLocation
import com.ab.weather.domain.repository.LocationRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import javax.inject.Inject



class LocationRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient
) : LocationRepository {

    @Suppress("MissingPermission")
    override suspend fun getCurrentLocation(): UserLocation {
        val fineGranted = context.checkSelfPermission(
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = context.checkSelfPermission(
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!fineGranted && !coarseGranted) {
            throw SecurityException("Location permission not granted.")
        }

        val tokenSource = CancellationTokenSource()

        try {
            val priority = if (fineGranted) {
                Priority.PRIORITY_HIGH_ACCURACY
            } else {
                Priority.PRIORITY_BALANCED_POWER_ACCURACY
            }

            val location = fusedLocationClient
                .getCurrentLocation(priority, tokenSource.token)
                .await()
                ?: fusedLocationClient.lastLocation.await()
                ?: throw IllegalStateException("Location unavailable.")

            return UserLocation(
                latitude = location.latitude,
                longitude = location.longitude
            )
        } finally {
            tokenSource.cancel()
        }
    }
}
