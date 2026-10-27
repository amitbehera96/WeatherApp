package com.ab.weather.domain.model

data class LocationUiState(
    val isLoading : Boolean = false,
    val location : UserLocation ?= null,
    val error : String ?= null,
    val permissionDenied : Boolean =false
)
