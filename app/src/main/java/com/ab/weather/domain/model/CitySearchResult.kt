package com.ab.weather.domain.model

data class CitySearchResult(
    val id: String,
    val name: String,
    val region: String,
    val country: String,
    val latitude: Double,
    val longitude: Double
){
    val  displayString : String
        get() = listOf(name,region,country)
            .filter { it.isNotBlank() }
            .distinct()
            .joinToString(", ")
}
