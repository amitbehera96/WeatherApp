package com.ab.weather.di

import com.ab.weather.data.remote.GeocodingApiService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GeocodingNetworkModule {

    @Provides
    @Singleton
    @GeocodingRetrofit
    fun provideGeocodingRetrofit(
        okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://geocoding-api.open-meteo.com/")
            .client(okHttpClient)
            .addConverterFactory(
                json.asConverterFactory(
                    "application/json".toMediaType()
                )
            )
            .build()
    }

    @Provides
    @Singleton
    fun provideGeocodingApiService(
        @GeocodingRetrofit retrofit: Retrofit
    ): GeocodingApiService {
        return retrofit.create(GeocodingApiService::class.java)
    }
}