package com.bruhascended.fitapp.ui.addfood


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bruhascended.api.models.foodsv2.Hint
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.databinding.ItemFoodsListBinding
import com.bumptech.glide.Glide

class FoodSearchAdapter(val itemClicked: (food_hint: Hint) -> Unit) :
    ListAdapter<Hint, FoodSearchAdapter.FoodViewHolder>(
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
            LayoutInflater.from(parent.context).inflate(R.layout.item_foods_list, parent, false)
        )
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        ItemFoodsListBinding.bind(holder.itemView).apply {
            val hint = getItem(position)
            foodName.text = hint.food.label
            tellBranded.text = hint.food.brand
            Glide
                .with(imageView.context)
                .load(hint.food.image)
                .circleCrop()
                .placeholder(R.drawable.ic_food_placeholder)
                .into(imageView)


            root.setOnClickListener { itemClicked(hint) }
        }
    }
}