package com.ab.weather.ui.presentation.location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ab.weather.domain.model.LocationUiState
import com.ab.weather.domain.usecase.GetCurrentUserLocationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException


@HiltViewModel
class LocationViewModel @Inject constructor(
    private val getCurrentUserLocationUseCase: GetCurrentUserLocationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LocationUiState())

    val uiState = _uiState.asStateFlow()


    fun onIntent(intent: LocationIntent) {
        when (intent) {
            LocationIntent.RequestLocation -> requestLocation()

            LocationIntent.PermissionDenied -> _uiState.value = LocationReducer.reduce(
                LocationResult.PermissionDenied
            )
        }
    }


    private fun requestLocation() {
        viewModelScope.launch {
            _uiState.value = LocationReducer.reduce(
                LocationResult.Loading
            )

            try {
                // This suspend function is called inside a coroutine.
                val location = getCurrentUserLocationUseCase()

                _uiState.value = LocationReducer.reduce(
                    LocationResult.Success(location)
                )
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: SecurityException) {
                _uiState.value = LocationReducer.reduce(
                    LocationResult.PermissionDenied
                )
            } catch (exception: Exception) {
                _uiState.value = LocationReducer.reduce(
                    LocationResult.Failure(
                        exception.message
                            ?: "Unable to retrieve your location."
                    )
                )
            }
        }
    }


}