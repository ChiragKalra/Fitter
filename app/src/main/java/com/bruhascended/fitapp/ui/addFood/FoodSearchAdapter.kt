package com.bruhascended.fitapp.ui.addFood


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bruhascended.api.models.foodsv2.Hint
import com.bruhascended.db.food.entities.Food
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.databinding.ItemFoodsHistoryListBinding
import com.bruhascended.fitapp.databinding.ItemFoodsListBinding
import com.bruhascended.fitapp.util.MultiViewType
import com.bumptech.glide.Glide

class FoodSearchAdapter(
    val itemClicked: (item: MultiViewType) -> Unit,
) :
    ListAdapter<MultiViewType, RecyclerView.ViewHolder>(
        object : DiffUtil.ItemCallback<MultiViewType>() {
            override fun areItemsTheSame(oldItem: MultiViewType, newItem: MultiViewType): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: MultiViewType,
                newItem: MultiViewType
            ): Boolean {
                return oldItem == newItem
            }
        }
    ) {


    override fun getItemViewType(position: Int): Int {
        return getItem(position).resId
    }


    inner class FoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener { itemClicked(getItem(absoluteAdapterPosition)) }
        }
    }

    inner class FoodHistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener { itemClicked(getItem(absoluteAdapterPosition)) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            0 -> FoodViewHolder(
                layoutInflater.inflate(
                    R.layout.item_foods_list,
                    parent,
                    false
                )
            )
            else -> FoodHistoryViewHolder(
                layoutInflater.inflate(
                    R.layout.item_foods_history_list,
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is FoodViewHolder -> {
                ItemFoodsListBinding.bind(holder.itemView).apply {
                    val hint = getItem(position).content as Hint
                    foodName.text = hint.food.label
                    brandName.text = hint.food.brand
                    Glide
                        .with(imageView.context)
                        .load(hint.food.image)
                        .circleCrop()
                        .placeholder(R.drawable.ic_restaurant_menu)
                        .into(imageView)
                }
            }
            is FoodHistoryViewHolder -> {
                ItemFoodsHistoryListBinding.bind(holder.itemView).apply {
                    val history = getItem(position).content as Food
                    foodName.text = history.foodName
                }
            }
        }
    }
}