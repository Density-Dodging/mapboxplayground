package com.bignerdranch.android.mapboxplayground

import android.content.Context

class MapRepository private constructor(context: Context) {

    val buildings: MutableList<Building> = ArrayList()

    // used for adding nodes into map
    init{
        // dummy data -> replace
        var foisie: Building = Building("FIS", "Foisie Innovation",
            42.274340, -71.808872,2, listOf(20,30), 2)

        var CC: Building = Building("CC", "Campus Center",
            42.274824,-71.808378, 3, listOf(30,40,10), 3)

        var recCenter: Building = Building("SRC", "WPI Sports and Recreation Center",
            42.274149,-71.810568, 3, listOf(30,40,10), 1)

        buildings.add(foisie)
        buildings.add(CC)
        buildings.add(recCenter)
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