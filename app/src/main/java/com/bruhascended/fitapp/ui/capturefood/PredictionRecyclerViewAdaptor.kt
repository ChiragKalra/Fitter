package com.bruhascended.fitapp.ui.capturefood

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.bruhascended.fitapp.R

typealias ClickListener = (foodLabel: String) -> Unit

class PredictionRecyclerViewAdaptor(
    private val predictions: Array<String>,
    private val listener: ClickListener? = null
): RecyclerView.Adapter<PredictionRecyclerViewAdaptor.PredictionViewHolder>() {

    class PredictionViewHolder(root: View) : RecyclerView.ViewHolder(root) {
        val nameButton: Button = root.findViewById(R.id.predictionName)
    }

    private fun String.formatText(): String {
        var ret = lowercase()
        ret = "" + ret[0].uppercase() + ret.slice(1..lastIndex)
        return ret.replace('-',' ').replace('_', ' ')
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PredictionViewHolder {
        return PredictionViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_food_prediction,
                parent,
                false
            )
        )
    }

    override fun getItemCount() = predictions.size

    override fun onBindViewHolder(holder: PredictionViewHolder, position: Int) {
        holder.nameButton.text =  predictions[position].formatText()
        holder.nameButton.setOnClickListener {
            listener?.invoke(predictions[position])
        }
    }
}