package com.ab.weather.ui.presentation.location

import com.ab.weather.domain.model.LocationUiState
import com.ab.weather.domain.model.UserLocation


sealed interface LocationResult {

    data object Loading : LocationResult

    data class Success(
        val location: UserLocation
    ) : LocationResult

    data class Failure(
        val message: String
    ) : LocationResult

    data object PermissionDenied : LocationResult
}

object LocationReducer {
    fun reduce(result: LocationResult): LocationUiState {

        return when (result) {
            LocationResult.Loading -> LocationUiState(
                isLoading = true
            )

            is LocationResult.Success -> LocationUiState(
                location = result.location
            )

            is LocationResult.Failure -> LocationUiState(
                error = result.message
            )

            LocationResult.PermissionDenied -> LocationUiState(
                permissionDenied = true
            )
        }
    }

}