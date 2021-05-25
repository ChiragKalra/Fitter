package com.bruhascended.fitapp.capturefood

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class PredictionPresenter(
    private val context: Context,
    private val recyclerView: RecyclerView
) {

    init {
        recyclerView.layoutManager = LinearLayoutManager(context)
    }

    fun populate (predictions: Array<String>) {
        recyclerView.post {
            recyclerView.adapter = PredictionRecyclerViewAdaptor(predictions)
        }
    }

}