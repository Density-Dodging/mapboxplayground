package com.bignerdranch.android.mapboxplayground

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button

const val DIRECTIONS_REQUESTED_FOR_ID = "com.bignerdranch.android.densitydodger.directions_requested"

class BuildingDetailsActivity : AppCompatActivity() {
    private val mapRepository: MapRepository = MapRepository.get();
    private var building: Building? = null

    private lateinit var getDirectionsBtn: Button


    //////////////////////// LIFECYCLE ////////////////////////

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_building_details)

        getDataFromIntent()
        initializeUiElements()
    }

    //////////////////////// OTHER FCNS ////////////////////////

    private fun initializeUiElements() {
        getDirectionsBtn = findViewById(R.id.getDirectionsBtn)
        getDirectionsBtn.setOnClickListener {
            building?.let {
                setDirectionsRequested(it.id)
            }
        }
    }

    private fun setDirectionsRequested(buildingId: String) {
        val data = Intent().apply {
            putExtra(DIRECTIONS_REQUESTED_FOR_ID, buildingId)
        }
        setResult(Activity.RESULT_OK, data)
        this.finish()
    }

    private fun getDataFromIntent() {
        val buildingId = intent.getStringExtra(BUILDING_ID).toString()

        building = mapRepository.getBuildingFromId(buildingId)
    }

    companion object {
        fun newIntent(packageContext: Context, buildingId: String): Intent {
            return Intent(packageContext, BuildingDetailsActivity::class.java).apply {
                putExtra(BUILDING_ID, buildingId)
            }
        }
    }
}