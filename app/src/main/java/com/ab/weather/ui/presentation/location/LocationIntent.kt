package com.ab.weather.ui.presentation.location

sealed interface LocationIntent {

    data object RequestLocation : LocationIntent
    data object PermissionDenied : LocationIntent
}