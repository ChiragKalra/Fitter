package com.bruhascended.fitapp.capturefood

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bruhascended.fitapp.R

class PredictionRecyclerViewAdaptor(
    private val predictions: Array<String>
):
    RecyclerView.Adapter<PredictionRecyclerViewAdaptor.PredictionViewHolder>()
{
    class PredictionViewHolder(root: View) : RecyclerView.ViewHolder(root) {
        val nameText: TextView = root.findViewById(R.id.predictionName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PredictionViewHolder {
        return PredictionViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(
                    R.layout.item_food_prediction,
                    parent,
                    false
                )
        )
    }

    override fun getItemCount() = predictions.size

    override fun onBindViewHolder(holder: PredictionViewHolder, position: Int) {
        holder.nameText.text =  predictions[position]
    }
}