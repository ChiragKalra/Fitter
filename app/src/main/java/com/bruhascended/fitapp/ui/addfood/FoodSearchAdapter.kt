package com.bruhascended.fitapp.ui.addfood


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bruhascended.api.models.foodsv2.Hint
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.databinding.FoodsListItemBinding

class FoodSearchAdapter(val itemClicked:(food_hint: Hint?)->Unit) : ListAdapter<Hint, FoodSearchAdapter.FoodViewHolder>(
    object : DiffUtil.ItemCallback<Hint>() {
        override fun areItemsTheSame(oldItem: Hint, newItem: Hint): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Hint, newItem: Hint): Boolean {
            return oldItem == newItem
        }

    }
) {
    inner class FoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        return FoodViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.foods_list_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        FoodsListItemBinding.bind(holder.itemView).apply {
            val hint = getItem(position)
            foodName.text = hint.food?.label
            tellBranded.text = hint.food?.brand
            root.setOnClickListener { itemClicked(hint) }
        }
    }
}