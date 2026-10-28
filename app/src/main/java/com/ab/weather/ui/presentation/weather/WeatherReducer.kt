package com.ab.weather.ui.presentation.weather

import com.ab.weather.domain.model.CurrentWeather
import com.ab.weather.domain.model.ForecastItem
import com.ab.weather.domain.model.UserLocation

sealed interface WeatherResult{

    data class Loading(
        val isRefresh : Boolean
    ): WeatherResult

    data class Success(
        val location : UserLocation,
        val currentWeather: CurrentWeather,
        val forecast : List<ForecastItem>
    ) : WeatherResult

    data class Failure(
        val message: String
    ) : WeatherResult

    data object PermissionDenied : WeatherResult
}


 object WeatherReducer {

     fun reduce (
         currentState : WeatherUiState,
         result: WeatherResult
     ): WeatherUiState{
         return when(result){
             is WeatherResult.Loading -> currentState.copy(
                 isLoading = !result.isRefresh,
                 isRefreshing = result.isRefresh,
                 errorMessage = null

             )

             is WeatherResult.Success -> WeatherUiState(
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
         }
     }
 }
