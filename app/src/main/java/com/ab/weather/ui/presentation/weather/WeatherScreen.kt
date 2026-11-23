
package com.ab.weather.ui.presentation.weather

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.ab.weather.domain.model.City
import com.ab.weather.domain.model.ForecastItem
import com.ab.weather.domain.model.WeatherLocation

@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var searchText by remember {
        mutableStateOf("")
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            val granted = permissions.values.any { it }

            if (granted) {
                viewModel.onIntent(WeatherIntent.LoadWeather)
            } else {
                viewModel.onIntent(
                    WeatherIntent.LocationPermissionDenied
                )
            }
        }

    fun requestLocationAndLoadWeather() {

        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            viewModel.onIntent(WeatherIntent.LoadWeather)
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = "Weather",
            style = MaterialTheme.typography.headlineMedium
        )

        // City Search

        Text(
            text = "Search City",
            style = MaterialTheme.typography.titleMedium
        )

        OutlinedTextField(
            value = searchText,
            onValueChange = {
                searchText = it
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Enter city name")
            },
            singleLine = true
        )

        Button(
            onClick = {
                viewModel.onIntent(
                    WeatherIntent.SearchCities(searchText.trim())
                )
            },
            enabled = searchText.isNotBlank() && !uiState.isSearching,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Search")
        }

        if (uiState.isSearching) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator()
                Text("Searching cities...")
            }
        }

        if (uiState.searchResults.isNotEmpty()) {

            Text(
                text = "Search Results",
                style = MaterialTheme.typography.titleMedium
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                uiState.searchResults.forEach { city ->

                    CitySearchItem(
                        city = city,
                        onClick = {
                            viewModel.onIntent(
                                WeatherIntent.SelectCity(city)
                            )

                            searchText = ""
                        }
                    )
                }
            }

        } else if (
            searchText.isNotBlank() &&
            !uiState.isSearching &&
            uiState.searchQuery == searchText
        ) {

            Text(
                text = "No cities found. Try another search.",
                style = MaterialTheme.typography.bodySmall
            )
        }

        // Saved Locations

        if (uiState.savedLocations.isNotEmpty()) {

            Text(
                text = "Saved Locations",
                style = MaterialTheme.typography.titleMedium
            )

            LazyRow(
                contentPadding = PaddingValues(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                items(
                    items = uiState.savedLocations,
                    key = { it.id }
                ) { location ->

                    SavedLocationItem(
                        location = location,
                        isSelected = location.id ==
                                uiState.selectedLocation?.id,
                        onClick = {
                            viewModel.onIntent(
                                WeatherIntent.SelectSavedLocation(location)
                            )
                        }
                    )
                }
            }
        }

        // Existing GPS Permission and Weather Loading

        Button(
            onClick = {
                requestLocationAndLoadWeather()
            },
            enabled = !uiState.isLoading && !uiState.isRefreshing,
            modifier = Modifier.fillMaxWidth()
        ) {

            Text(
                if (uiState.currentWeather == null) {
                    "Allow location & load weather"
                } else {
                    "Refresh GPS weather"
                }
            )
        }

        if (uiState.isLoading || uiState.isRefreshing) {
            CircularProgressIndicator()
        }

        // Error Message

        uiState.errorMessage?.let { message ->

            Text(
                text = message,
                color = MaterialTheme.colorScheme.error
            )
        }

        // Current Weather

        uiState.currentWeather?.let { weather ->

            Text(
                text = "${weather.cityName}, ${weather.country}",
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "${weather.temperature} °C",
                style = MaterialTheme.typography.displaySmall
            )

            Text(text = weather.description)

            Text(
                text = "Feels like: ${weather.feelsLike} °C"
            )

            Text(
                text = "Today's range: " +
                        "${weather.minimumTemperature}° – " +
                        "${weather.maximumTemperature}°"
            )

            Text(
                text = "Humidity: ${weather.humidity}%"
            )

            Text(
                text = "Wind: ${weather.windSpeed} m/s"
            )

            Text(
                text = "Forecast",
                style = MaterialTheme.typography.titleLarge
            )

            uiState.forecast.forEach { forecast ->
                ForecastRow(forecast)
            }

        } ?: run {

            if (
                !uiState.isLoading &&
                !uiState.isRefreshing &&
                uiState.errorMessage == null
            ) {
                Text(
                    "Search for a city or allow location access to see weather."
                )
            }
        }

        Text(
            text = "Weather data by Open-Meteo · open-meteo.com",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

// City Search Result

@Composable
private fun CitySearchItem(
    city: City,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {

        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            Text(
                text = city.name,
                style = MaterialTheme.typography.titleMedium
            )

            val locationDetails =
                listOfNotNull(city.admin1, city.country)
                    .filter { it.isNotBlank() }
                    .joinToString(", ")

            Text(
                text = locationDetails,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

// Saved Location Item

@Composable
private fun SavedLocationItem(
    location: WeatherLocation,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {

        Column(
            modifier = Modifier
                .heightIn(min = 64.dp)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            Text(
                text = location.name,
                style = MaterialTheme.typography.titleSmall
            )

            Text(
                text = location.country,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

// Forecast Item

@Composable
private fun ForecastRow(
    forecast: ForecastItem
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            Text(
                text = forecast.dateTime,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = forecast.description
            )

            Text(
                text = "Min ${forecast.minimumTemperature} °C · " +
                        "Max ${forecast.maximumTemperature} °C"
            )
        }
    }
}
