package com.ab.weather.ui.presentation.weather

object WeatherReducer {

    fun reduce(
        currentState: WeatherUiState,
        result: WeatherResult
    ): WeatherUiState {

        return when (result) {

            is WeatherResult.Loading -> currentState.copy(
                isLoading = !result.isRefresh,
                isRefreshing = result.isRefresh,
                errorMessage = null
            )

            is WeatherResult.Success -> currentState.copy(
                isLoading = false,
                isRefreshing = false,
                location = result.location,
                currentWeather = result.currentWeather,
                forecast = result.forecast,
                errorMessage = null
            )

            is WeatherResult.Failure -> currentState.copy(
                isLoading = false,
                isRefreshing = false,
                errorMessage = result.message
            )

            WeatherResult.PermissionDenied -> currentState.copy(
                isLoading = false,
                isRefreshing = false,
                errorMessage = "Location permission is required to load weather."
            )

            is WeatherResult.SearchStarted -> currentState.copy(
                searchQuery = result.query,
                isSearching = true,
                searchResults = emptyList(),
                errorMessage = null
            )

            is WeatherResult.SearchSuccess -> currentState.copy(
                isSearching = false,
                searchResults = result.cities,
                errorMessage = null
            )

            is WeatherResult.SearchFailure -> currentState.copy(
                isSearching = false,
                errorMessage = result.message
            )

            is WeatherResult.SavedLocationsUpdated -> currentState.copy(
                savedLocations = result.locations
            )

            is WeatherResult.SelectedLocationChanged -> currentState.copy(
                selectedLocation = result.location,
                isLoading = false,
                isRefreshing = false,
                errorMessage = null
            )

            is WeatherResult.CachedWeatherUpdated -> currentState.copy(
                currentWeather = result.currentWeather,
                forecast = result.forecast,
                isLoading = false,
                isRefreshing = false,
                errorMessage = null
            )
        }
    }
}