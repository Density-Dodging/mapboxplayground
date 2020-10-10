package com.bignerdranch.android.mapboxplayground

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle



class BuildingDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_building_details)
    }

    companion object {
        fun newIntent(packageContext: Context, buildingName: String, buildingPeople: String, numFloors: Number): Intent {
            return Intent(packageContext, BuildingDetailsActivity::class.java).apply {
                putExtra(BUILDING_NAME, buildingName)
                putExtra(BUILDING_PEOPLE, buildingPeople)
                putExtra(BUILDING_FLOORS, numFloors)
            }
        }
    }
}