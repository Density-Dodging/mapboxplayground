package com.bignerdranch.android.mapboxplayground

import android.content.Context
import com.mapbox.geojson.Feature

class MapRepository private constructor(context: Context) {

    val buildingCoordinates: MutableList<Building> = ArrayList()

    // used for adding nodes into map
    init{
        // dummy data -> replace
        var foisie: Building = Building("FIS", "Foisie Innovation",
            42.274340, -71.808872,2, listOf(20,30))

        buildingCoordinates.add(foisie)

        var CC: Building = Building("CC", "Campus Center",
            42.274824,-71.808378, 3, listOf(30,40,10))

        buildingCoordinates.add(CC)

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