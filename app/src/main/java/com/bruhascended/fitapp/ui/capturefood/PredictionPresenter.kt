package com.bruhascended.fitapp.ui.capturefood

import android.content.Intent
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.ui.addFood.FoodSearchActivity

class PredictionPresenter(
    private val cameraActivity: CameraActivity,
    private val recyclerView: RecyclerView
) {
    companion object {
        const val KEY_FOOD_LABEL = "FOOD_LABEL"

        fun String.cleanedLabel(): String {
            var ret = lowercase()
            ret = "" + ret[0].uppercase() + ret.slice(1..lastIndex)
            return ret.replace('-',' ').replace('_', ' ')
        }
    }

    private val noResArr = arrayOf(cameraActivity.getString(R.string.no_food_item_detected))

    init {
        recyclerView.layoutManager = LinearLayoutManager(cameraActivity)
        populate(emptyArray())
    }

    fun populate (predictions: Array<String>) {
        recyclerView.post {
            if (predictions.isEmpty()) {
                recyclerView.adapter = PredictionRecyclerViewAdaptor(noResArr) 
            } else {
                recyclerView.adapter = PredictionRecyclerViewAdaptor(predictions).apply {
                    setOnClickListener {
                        setOnClickListener { }
                        cameraActivity.toggleFlashlight(false)
                        cameraActivity.startActivity(
                            Intent(cameraActivity, FoodSearchActivity::class.java).apply {
                                putExtra(KEY_FOOD_LABEL, it.cleanedLabel())
                            }
                        )
                        cameraActivity.finish()
                    }
                }
            }
        }
    }
}