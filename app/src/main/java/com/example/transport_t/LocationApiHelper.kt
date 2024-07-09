package com.example.transport_t

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

data class PlaceResponse(val predictions: List<Prediction>)
data class Prediction(val description: String)

interface LocationApiService {
    @GET("autocomplete/json")
    suspend fun getPlaceSuggestions(
        @Query("input") input: String,
        @Query("key") apiKey: String
    ): PlaceResponse
}

class LocationApiHelper(private val apiKey: String, context: Context) {
    private val TAG = "LocationApiHelper"
    private val cacheMap = mutableMapOf<String, List<String>>()

    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://maps.googleapis.com/maps/api/place/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService: LocationApiService = retrofit.create(LocationApiService::class.java)

    suspend fun fetchLocations(query: String): List<String> = withContext(Dispatchers.IO) {
        try {
            // Check cache first
            cacheMap[query]?.let { return@withContext it }

            val startTime = System.currentTimeMillis()
            val response = apiService.getPlaceSuggestions(query, apiKey)
            val endTime = System.currentTimeMillis()

            Log.d(TAG, "API call took ${endTime - startTime} ms")

            val locations = response.predictions.map { it.description }
            // Cache the result
            cacheMap[query] = locations
            locations
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching locations", e)
            emptyList()
        }
    }
}