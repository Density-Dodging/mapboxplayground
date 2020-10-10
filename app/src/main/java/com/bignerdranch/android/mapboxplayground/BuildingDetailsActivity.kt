package com.bignerdranch.android.mapboxplayground

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log


class BuildingDetailsActivity : AppCompatActivity() {
    private lateinit var buildingName: String
    private var buildingPeople: MutableList<Double> = mutableListOf()
    private var numFloors: Int = 0
    private var buildingDensity: Int = 0


    //////////////////////// LIFECYCLE ////////////////////////

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_building_details)

        getDataFromIntent()

        Log.d("test", "$buildingName : ${buildingPeople.size} : $numFloors : $buildingDensity")
    }

    //////////////////////// OTHER FCNS ////////////////////////

    private fun getDataFromIntent() {
        buildingName = intent.getStringExtra(BUILDING_NAME).toString()

        // init buildingPeople
        var peopleBuildingString = intent.getStringExtra(BUILDING_PEOPLE).toString()
        var subStr = peopleBuildingString.substring(1, peopleBuildingString.length - 1)
        subStr.split(",").forEach { floorPeopleString ->
            buildingPeople.add(floorPeopleString.toDouble())
        }

        numFloors = intent.getIntExtra(BUILDING_FLOORS, 0)
        buildingDensity = intent.getIntExtra(BUILDING_DENSITY, 1)
    }

    companion object {
        fun newIntent(packageContext: Context, buildingName: String, buildingPeople: String, numFloors: Number, buildingDensity: Int): Intent {
            return Intent(packageContext, BuildingDetailsActivity::class.java).apply {
                putExtra(BUILDING_NAME, buildingName)
                putExtra(BUILDING_PEOPLE, buildingPeople)
                putExtra(BUILDING_FLOORS, numFloors)
                putExtra(BUILDING_DENSITY, buildingDensity)
            }
        }
    }
}