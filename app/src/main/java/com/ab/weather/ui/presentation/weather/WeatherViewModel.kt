
package com.ab.weather.ui.presentation.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ab.weather.domain.model.City
import com.ab.weather.domain.model.WeatherLocation
import com.ab.weather.domain.usecase.GetCurrentUserLocationUseCase
import com.ab.weather.domain.usecase.GetCurrentWeatherUseCase
import com.ab.weather.domain.usecase.GetForecastUseCase
import com.ab.weather.domain.usecase.GetSavedLocationsUseCase
import com.ab.weather.domain.usecase.ObserveCurrentWeatherUseCase
import com.ab.weather.domain.usecase.ObserveForecastUseCase
import com.ab.weather.domain.usecase.RefreshWeatherUseCase
import com.ab.weather.domain.usecase.SaveLocationUseCase
import com.ab.weather.domain.usecase.SearchCitiesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    // Existing GPS use cases
    private val getCurrentLocationUseCase: GetCurrentUserLocationUseCase,
    private val getCurrentWeatherUseCase: GetCurrentWeatherUseCase,
    private val getForecastUseCase: GetForecastUseCase,

    // New use cases
    private val searchCitiesUseCase: SearchCitiesUseCase,
    private val getSavedLocationsUseCase: GetSavedLocationsUseCase,
    private val observeCurrentWeatherUseCase: ObserveCurrentWeatherUseCase,
    private val observeForecastUseCase: ObserveForecastUseCase,
    private val saveLocationUseCase: SaveLocationUseCase,
    private val refreshWeatherUseCase: RefreshWeatherUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var weatherObservationJob: Job? = null

    init {
        observeSavedLocations()
    }

    fun onIntent(intent: WeatherIntent) {
        when (intent) {
            WeatherIntent.LoadWeather -> loadWeather(isRefresh = false)

            WeatherIntent.RefreshWeather -> {
                val selectedLocation = _uiState.value.selectedLocation

                if (selectedLocation != null) {
                    refreshSelectedLocation(selectedLocation)
                } else {
                    loadWeather(isRefresh = true)
                }
            }

            WeatherIntent.LocationPermissionDenied -> {
                _uiState.value = WeatherReducer.reduce(
                    _uiState.value,
                    WeatherResult.PermissionDenied
                )
            }

            is WeatherIntent.SearchCities -> {
                searchCities(intent.query)
            }

            is WeatherIntent.SelectCity -> {
                selectCity(intent.city)
            }

            is WeatherIntent.SelectSavedLocation -> {
                selectSavedLocation(intent.location)
            }
        }
    }

    // Existing GPS weather functionality

    fun loadWeather(isRefresh: Boolean) {
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
                showFailure(getHttpErrorMessage(exception))
            } catch (exception: IOException) {
                showFailure(
                    "Network error. Check your internet connection."
                )
            } catch (exception: Exception) {
                showFailure(
                    exception.message
                        ?: "Something went wrong while loading weather."
                )
            }
        }
    }

    // City search

    private fun searchCities(query: String) {
        searchJob?.cancel()

        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(
                searchQuery = query,
                searchResults = emptyList(),
                isSearching = false
            )
            return
        }

        searchJob = viewModelScope.launch {
            _uiState.value = WeatherReducer.reduce(
                _uiState.value,
                WeatherResult.SearchStarted(query)
            )

            try {
                val cities = searchCitiesUseCase(query)

                _uiState.value = WeatherReducer.reduce(
                    _uiState.value,
                    WeatherResult.SearchSuccess(cities)
                )

            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.value = WeatherReducer.reduce(
                    _uiState.value,
                    WeatherResult.SearchFailure(
                        exception.message ?: "Unable to search cities."
                    )
                )
            }
        }
    }

    // Select a city from search results

    private fun selectCity(city: City) {
        viewModelScope.launch {
            val location = WeatherLocation(
                id = city.id,
                name = city.name,
                country = city.country,
                latitude = city.latitude,
                longitude = city.longitude,
                isCurrentLocation = false
            )

            try {
                saveLocationUseCase(location)

                _uiState.value = WeatherReducer.reduce(
                    _uiState.value,
                    WeatherResult.SelectedLocationChanged(location)
                )

                observeLocationWeather(location.id)
                refreshSelectedLocation(location)

            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                showFailure(
                    exception.message ?: "Unable to select this city."
                )
            }
        }
    }

    // Select an already saved location

    private fun selectSavedLocation(location: WeatherLocation) {
        _uiState.value = WeatherReducer.reduce(
            _uiState.value,
            WeatherResult.SelectedLocationChanged(location)
        )

        observeLocationWeather(location.id)
        refreshSelectedLocation(location)
    }

    // Observe weather stored in Room

    private fun observeLocationWeather(locationId: String) {
        weatherObservationJob?.cancel()

        weatherObservationJob = viewModelScope.launch {
            combine(
                observeCurrentWeatherUseCase(locationId),
                observeForecastUseCase(locationId)
            ) { currentWeather, forecast ->
                currentWeather to forecast
            }.collect { (currentWeather, forecast) ->

                _uiState.value = WeatherReducer.reduce(
                    _uiState.value,
                    WeatherResult.CachedWeatherUpdated(
                        currentWeather = currentWeather,
                        forecast = forecast
                    )
                )
            }
        }
    }

    // Refresh selected location from Open-Meteo and save to Room

    private fun refreshSelectedLocation(location: WeatherLocation) {
        viewModelScope.launch {
            _uiState.value = WeatherReducer.reduce(
                _uiState.value,
                WeatherResult.Loading(isRefresh = true)
            )

            try {
                refreshWeatherUseCase(location)

            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: HttpException) {
                showFailure(getHttpErrorMessage(exception))
            } catch (exception: IOException) {
                showFailure(
                    "Network error. Showing cached weather if available."
                )
            } catch (exception: Exception) {
                showFailure(
                    exception.message ?: "Unable to refresh weather."
                )
            }
        }
    }

    // Observe saved locations from Room

    private fun observeSavedLocations() {
        viewModelScope.launch {
            getSavedLocationsUseCase().collect { locations ->

                _uiState.value = WeatherReducer.reduce(
                    _uiState.value,
                    WeatherResult.SavedLocationsUpdated(locations)
                )
            }
        }
    }

    private fun getHttpErrorMessage(exception: HttpException): String {
        return when (exception.code()) {
            429 -> "Weather API limit reached. Please try again later."
            in 500..599 -> "Weather service is temporarily unavailable."
            else -> "Unable to retrieve weather. HTTP ${exception.code()}."
        }
    }

    private fun showFailure(message: String) {
        _uiState.value = WeatherReducer.reduce(
            _uiState.value,
            WeatherResult.Failure(message)
        )
    }
}
