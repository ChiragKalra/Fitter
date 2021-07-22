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

        val binding = ItemFoodsListBinding.bind(itemView)

        fun bindItem(item: Hint) {
            binding.apply {
                foodName.text = item.food.label
                brandName.text = item.food.brand
                Glide
                    .with(imageView.context)
                    .load(item.food.image)
                    .circleCrop()
                    .placeholder(R.drawable.ic_restaurant_menu)
                    .into(imageView)
            }
        }
    }

    inner class FoodHistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener { itemClicked(getItem(absoluteAdapterPosition)) }
        }

        val binding = ItemFoodsHistoryListBinding.bind(itemView)
        fun bindItem(item: Food) {
            binding.foodName.text = item.foodName
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
            is FoodViewHolder -> holder.bindItem(getItem(position).content as Hint)
            is FoodHistoryViewHolder -> holder.bindItem(getItem(position).content as Food)
        }
    }
}