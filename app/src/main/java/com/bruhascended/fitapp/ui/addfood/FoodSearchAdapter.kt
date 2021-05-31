package com.bruhascended.fitapp.ui.addfood

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.databinding.FoodsListItemBinding
import com.bruhascended.api.models.foods.Food
import java.util.*

class FoodSearchAdapter : ListAdapter<Food, FoodSearchAdapter.FoodViewHolder>(
    object : DiffUtil.ItemCallback<Food>() {
        override fun areItemsTheSame(oldItem: Food, newItem: Food): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Food, newItem: Food): Boolean {
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
            val food = getItem(position)

            foodName.text = food.description?.toLowerCase(Locale.ROOT)
            tellBranded.text = food.brandOwner
        }
    }
}