package com.ab.weather.ui.presentation.weather

import android.content.pm.PackageManager
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.ab.weather.domain.model.ForecastItem

@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

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
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Weather",
            style = MaterialTheme.typography.headlineMedium
        )

        Button(
            onClick = { requestLocationAndLoadWeather() },
            enabled = !uiState.isLoading && !uiState.isRefreshing,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (uiState.currentWeather == null) {
                    "Allow location & load weather"
                } else {
                    "Refresh weather"
                }
            )
        }

        if (uiState.isLoading || uiState.isRefreshing) {
            CircularProgressIndicator()
        }

        uiState.errorMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error
            )
        }

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
            Text(text = "Feels like: ${weather.feelsLike} °C")
            Text(
                text = "Today's range: " +
                        "${weather.minimumTemperature}° – " +
                        "${weather.maximumTemperature}°"
            )
            Text(text = "Humidity: ${weather.humidity}%")
            Text(text = "Wind: ${weather.windSpeed} m/s")

            Text(
                text = "5-day forecast",
                style = MaterialTheme.typography.titleLarge
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = uiState.forecast,
                    key = { it.dateTime }
                ) { forecast ->
                    ForecastRow(forecast)
                }
            }
        } ?: run {
            if (!uiState.isLoading && !uiState.isRefreshing &&
                uiState.errorMessage == null
            ) {
                Text("Allow location access to see your local weather.")
            }
        }

        Text(
            text = "Weather data by Open-Meteo · open-meteo.com",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun ForecastRow(forecast: ForecastItem) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = forecast.dateTime,
                style = MaterialTheme.typography.titleMedium
            )

            Text(text = forecast.description)

            Text(
                text = "Min ${forecast.minimumTemperature} °C · " +
                        "Max ${forecast.maximumTemperature} °C"
            )
        }
    }
}