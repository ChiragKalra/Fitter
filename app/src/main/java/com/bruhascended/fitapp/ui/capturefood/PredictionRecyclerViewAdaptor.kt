package com.bruhascended.fitapp.ui.capturefood

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.ui.capturefood.PredictionPresenter.Companion.cleanedLabel

typealias ClickListener = (foodLabel: String) -> Unit

class PredictionRecyclerViewAdaptor(
    private val predictions: Array<String>
): ListAdapter<String, PredictionRecyclerViewAdaptor.PredictionViewHolder>(
    object : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(o: String, n: String) = o == n
        override fun areContentsTheSame(o: String, n: String) = o == n
    }
) {

    class PredictionViewHolder(root: View) : RecyclerView.ViewHolder(root) {
        val nameButton: Button = root.findViewById(R.id.predictionName)
    }


    private var listener: ClickListener? = null

    fun setOnClickListener(listener: ClickListener) {
        this.listener = listener
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
        holder.nameButton.text =  predictions[position].cleanedLabel()
        holder.nameButton.setOnClickListener {
            listener?.invoke(predictions[position])
        }
    }
}