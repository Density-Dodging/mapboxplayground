package com.bignerdranch.android.mapboxplayground

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log


class BuildingDetailsActivity : AppCompatActivity() {
    private val mapRepository: MapRepository = MapRepository.get();
    private lateinit var building: Building


    //////////////////////// LIFECYCLE ////////////////////////

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_building_details)

        getDataFromIntent()
    }

    //////////////////////// OTHER FCNS ////////////////////////

    private fun getDataFromIntent() {
        val buildingId = intent.getStringExtra(BUILDING_ID).toString()

        // find the building with the given id in our repository
        for (i in 0 until mapRepository.buildings.size) {
            var buildingFromRepo = mapRepository.buildings[i]

            if (buildingFromRepo.id == buildingId) {
                // store the obtained building in a variable
                building = buildingFromRepo
            }
        }
    }

    companion object {
        fun newIntent(packageContext: Context, buildingId: String): Intent {
            return Intent(packageContext, BuildingDetailsActivity::class.java).apply {
                putExtra(BUILDING_ID, buildingId)
            }
        }
    }
}