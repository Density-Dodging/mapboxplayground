package com.bignerdranch.android.mapboxplayground

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

class MapRepository private constructor(context: Context) {

    var buildings: MutableLiveData<List<Building>> = MutableLiveData()

    private val retrofit: Retrofit = Retrofit.Builder().baseUrl("https://density-dodger.herokuapp.com/api/")
        .addConverterFactory(GsonConverterFactory.create()).build()
    private val backendApi: BackendRequests = retrofit.create(BackendRequests::class.java)


    fun getBuildingFromId(buildingId: String): Building? {
        // find the building with the given id in our repository
        for (i in 0 until (buildings.value?.size ?: 0)) {
            var buildingFromRepo = buildings.value?.get(i)

            if (buildingFromRepo != null) {
                if (buildingFromRepo.id == buildingId) {
                    // store the obtained building in a variable
                    return buildingFromRepo
                }
            }
        }

        return null
    }

    private fun loadBuildings() {
        val buildingsRequest: Call<List<Building>> = backendApi.fetchBuildings()

        buildingsRequest.enqueue(object : Callback<List<Building>> {
            override fun onFailure(call: Call<List<Building>>, t: Throwable) {
                Log.e("TAG", "Failed to fetch buildings", t)
            }

            override fun onResponse(call: Call<List<Building>>, response: Response<List<Building>>) {
                var buildingItems: List<Building>? = response.body()

                if (buildingItems != null) {
                    buildings.value = buildingItems as MutableList<Building>
                }
            }
        })
    }


    // used for adding nodes into map
    init{
        loadBuildings()
        // dummy data -> replace
//        var foisie: Building = Building("FIS", "Foisie Innovation",
//            42.274340, -71.808872,2, listOf(20,30), 2)
//
//        var CC: Building = Building("CC", "Campus Center",
//            42.274824,-71.808378, 3, listOf(30,40,10), 3)
//
//        var recCenter: Building = Building("SRC", "WPI Sports and Recreation Center",
//            42.274149,-71.810568, 3, listOf(30,40,10), 1)
//
//        buildings.add(foisie)
//        buildings.add(CC)
//        buildings.add(recCenter)
    }

    companion object {
        private var INSTANCE: MapRepository? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = MapRepository(context)
            }
        }

        fun get(): MapRepository {
            return INSTANCE ?: throw IllegalStateException("CrimeRepository must be initialized")
        }

    }

}