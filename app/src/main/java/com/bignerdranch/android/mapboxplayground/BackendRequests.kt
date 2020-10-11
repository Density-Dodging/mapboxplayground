package com.bignerdranch.android.mapboxplayground

import retrofit2.Call
import retrofit2.http.GET

interface BackendRequests {
    @GET("buildings/all")
    fun fetchBuildings(): Call<List<Building>>
}