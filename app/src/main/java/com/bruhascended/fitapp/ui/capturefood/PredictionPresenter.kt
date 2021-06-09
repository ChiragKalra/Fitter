package com.bruhascended.fitapp.ui.capturefood

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.ui.addfood.AddFoodActivity

class PredictionPresenter(
    private val context: Context,
    private val recyclerView: RecyclerView
) {
    companion object {
        const val KEY_FOOD_LABEL = "FOOD_LABEL"
    }

    private val noResArr = arrayOf(context.getString(R.string.no_food_item_detected))

    init {
        recyclerView.layoutManager = LinearLayoutManager(context)
        populate(emptyArray())
    }

    fun populate (predictions: Array<String>) {
        recyclerView.post {
            if (predictions.isEmpty()) {
                recyclerView.adapter = PredictionRecyclerViewAdaptor(noResArr) {}
            } else {
                recyclerView.adapter = PredictionRecyclerViewAdaptor(predictions) {
                    context.startActivity(
                        Intent(context, AddFoodActivity::class.java).apply {
                            putExtra(KEY_FOOD_LABEL, it)
                        }
                    )
                    (context as AppCompatActivity).finish()
                }
            }
        }
    }
}