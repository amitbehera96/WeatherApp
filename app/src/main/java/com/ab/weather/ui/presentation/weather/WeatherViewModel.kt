package com.ab.weather.ui.presentation.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ab.weather.domain.usecase.GetCurrentUserLocationUseCase
import com.ab.weather.domain.usecase.GetCurrentWeatherUseCase
import com.ab.weather.domain.usecase.GetForecastUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject


@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val getCurrentLocationUseCase: GetCurrentUserLocationUseCase,
    private val getCurrentWeatherUseCase: GetCurrentWeatherUseCase,
    private val getForecastUseCase: GetForecastUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState = _uiState.asStateFlow()


    fun onIntent(intent: WeatherIntent){
        when(intent){
            WeatherIntent.LoadWeather -> loadWeather(isRefresh = false)
            WeatherIntent.RefreshWeather -> loadWeather(isRefresh = true)
            WeatherIntent.LocationPermissionDenied ->
                _uiState.value = WeatherReducer.reduce(
                    _uiState.value,
                    WeatherResult.PermissionDenied
                )
        }
    }


    fun loadWeather(isRefresh : Boolean){
        viewModelScope.launch {
            _uiState.value = WeatherReducer.reduce(
                _uiState.value,
                WeatherResult.Loading(isRefresh)
            )

            try {
                val location = getCurrentLocationUseCase()

                val currentWeatherDeferred = async {
                    getCurrentWeatherUseCase(location)
                }

                val forecastDeferred = async {
                    getForecastUseCase(location)
                }

                val currentWeather = currentWeatherDeferred.await()
                val forecast = forecastDeferred.await()

                _uiState.value = WeatherReducer.reduce(
                    _uiState.value,
                    WeatherResult.Success(
                        location = location,
                        currentWeather = currentWeather,
                        forecast = forecast
                    )
                )
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: SecurityException) {
                _uiState.value = WeatherReducer.reduce(
                    _uiState.value,
                    WeatherResult.PermissionDenied
                )
            } catch (exception: HttpException) {
                val message = when (exception.code()) {
                    429 -> "Weather API limit reached. Please try again later."
                    in 500..599 -> "Weather service is temporarily unavailable."
                    else -> "Unable to retrieve weather. HTTP ${exception.code()}."
                }
                showFailure(message)
            } catch (exception: IOException) {
                showFailure("Network error. Check your internet connection.")
            } catch (exception: Exception) {
                showFailure(
                    exception.message ?: "Something went wrong while loading weather."
                )
            }
        }
    }

    private fun showFailure(message: String) {
        _uiState.value = WeatherReducer.reduce(
            _uiState.value,
            WeatherResult.Failure(message)
        )
    }
}