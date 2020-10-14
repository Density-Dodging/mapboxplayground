package com.bignerdranch.android.mapboxplayground

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.squareup.picasso.Picasso
import org.w3c.dom.Text

const val DIRECTIONS_REQUESTED_FOR_ID = "com.bignerdranch.android.densitydodger.directions_requested"

class BuildingDetailsActivity : AppCompatActivity() {
    private val mapRepository: MapRepository = MapRepository.get();
    private var building: Building? = null
    private lateinit var buildingNameText: TextView
    private lateinit var densityLevelText: TextView
    private lateinit var densityLevelPercentage: TextView

    private lateinit var getDirectionsBtn: Button
    private lateinit var buildingImageView: ImageView
    private lateinit var densityProgressBar: ProgressBar
    private var occupancyLevel: Int? = 0


    //////////////////////// LIFECYCLE ////////////////////////

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_building_details)

        getDataFromIntent()
        initializeUiElements()

//        buildingImageView = findViewById(R.id.imageView)
//        buildingNameText = findViewById(R.id.buildingName)
//        densityLevelText = findViewById(R.id.density_level)
//        densityProgressBar = findViewById(R.id.progressBar)
//        densityLevelPercentage = findViewById(R.id.occupancyLevel)
//
//        Picasso.get().load(building?.url).into(buildingImageView)
//
//        buildingNameText.text = "${building?.buildingName}"
//
//        when (building?.densityLevel) {
//            3 -> {
//                densityLevelText.text = "High"
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                    densityProgressBar.progressDrawable.colorFilter =
//                        BlendModeColorFilter(Color.RED,BlendMode.SRC_IN)
//                }
//                occupancyLevel = (60..100).random()
//                densityLevelPercentage.text = "$occupancyLevel" + "%"
//                densityProgressBar.setProgress(occupancyLevel!!,false)
//
//
//            }
//            2 -> {
//                densityLevelText.text = "Moderate"
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                    densityProgressBar.progressDrawable.colorFilter =
//                        BlendModeColorFilter(Color.YELLOW,BlendMode.SRC_IN)
//                }
//                occupancyLevel = (30..60).random()
//                densityLevelPercentage.text = "$occupancyLevel" + "%"
//                densityProgressBar.setProgress(occupancyLevel!!,false)
//
//            }
//            else -> {
//                densityLevelText.text = "Low"
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                    densityProgressBar.progressDrawable.colorFilter =
//                        BlendModeColorFilter(Color.GREEN,BlendMode.SRC_IN)
//                }
//                occupancyLevel = (5..30).random()
//                densityLevelPercentage.text = "$occupancyLevel" + "%"
//                densityProgressBar.setProgress(occupancyLevel!!,false)
//
//            }
//        }
//
//        //actionbar
//        val actionbar = supportActionBar
//        //set actionbar title
//        actionbar!!.title = "${building?.buildingName}"
//        //set back button
//        actionbar.setDisplayHomeAsUpEnabled(true)
//        actionbar.setDisplayHomeAsUpEnabled(true)
    }

    //////////////////////// OTHER FCNS ////////////////////////

    @RequiresApi(Build.VERSION_CODES.N)
    private fun initializeUiElements() {
        getDirectionsBtn = findViewById(R.id.getDirectionsBtn)
        getDirectionsBtn.setOnClickListener {
            building?.let {
                setDirectionsRequested(it.id)
            }
        }

        buildingImageView = findViewById(R.id.imageView)
        buildingNameText = findViewById(R.id.buildingName)
        densityLevelText = findViewById(R.id.density_level)
        densityProgressBar = findViewById(R.id.progressBar)
        densityLevelPercentage = findViewById(R.id.occupancyLevel)

        Picasso.get().load(building?.url).into(buildingImageView)

        buildingNameText.text = "${building?.buildingName}"

        when (building?.densityLevel) {
            3 -> {
                densityLevelText.text = "High"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    densityProgressBar.progressDrawable.colorFilter =
                        BlendModeColorFilter(Color.RED,BlendMode.SRC_IN)
                }
                occupancyLevel = (60..100).random()
                densityLevelPercentage.text = "$occupancyLevel" + "%"
                densityProgressBar.setProgress(occupancyLevel!!,false)


            }
            2 -> {
                densityLevelText.text = "Moderate"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    densityProgressBar.progressDrawable.colorFilter =
                        BlendModeColorFilter(Color.YELLOW,BlendMode.SRC_IN)
                }
                occupancyLevel = (30..60).random()
                densityLevelPercentage.text = "$occupancyLevel" + "%"
                densityProgressBar.setProgress(occupancyLevel!!,false)

            }
            else -> {
                densityLevelText.text = "Low"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    densityProgressBar.progressDrawable.colorFilter =
                        BlendModeColorFilter(Color.GREEN,BlendMode.SRC_IN)
                }
                occupancyLevel = (5..30).random()
                densityLevelPercentage.text = "$occupancyLevel" + "%"
                densityProgressBar.setProgress(occupancyLevel!!,false)

            }
        }

        //actionbar
        val actionbar = supportActionBar
        //set actionbar title
        actionbar!!.title = "${building?.buildingName}"
        //set back button
        actionbar.setDisplayHomeAsUpEnabled(true)
        actionbar.setDisplayHomeAsUpEnabled(true)
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


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}