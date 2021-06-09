package com.bruhascended.fitapp.ui.foodjournal

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bruhascended.db.food.FoodEntryComparator
import com.bruhascended.db.food.entities.FoodEntry
import com.bruhascended.fitapp.databinding.ItemFoodEntryBinding
import com.bruhascended.fitapp.ui.foodjournal.FoodJournalRecyclerAdapter.FoodEntryItemHolder
import com.bruhascended.fitapp.util.DateTimePresenter
import com.bruhascended.fitapp.util.QuantityPresenter
import com.bruhascended.fitapp.util.AnimationDuration
import com.bruhascended.fitapp.util.animateProgressTo

class FoodJournalRecyclerAdapter (
    private val mContext: Context
): PagingDataAdapter<FoodEntry, FoodEntryItemHolder> (FoodEntryComparator) {

    private var mOnItemClickListener: ((foodEntry: FoodEntry) -> Unit)? = null

    class FoodEntryItemHolder(
        val binding: ItemFoodEntryBinding
    ) : RecyclerView.ViewHolder(binding.root)

    fun setOnItemClickListener (listener: ((foodEntry: FoodEntry) -> Unit)?) {
        mOnItemClickListener = listener
    }

    override fun onCreateViewHolder (parent: ViewGroup, viewType: Int): FoodEntryItemHolder {
        return FoodEntryItemHolder(
            ItemFoodEntryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder (holder: FoodEntryItemHolder, position: Int) {
        val foodEntry = getItem(position) ?: return
        val food = foodEntry.food
        val entry = foodEntry.entry
        holder.binding.apply {
            textviewTime.text = DateTimePresenter(mContext, entry.timeInMillis).condensedTime
            textviewCalories.text = entry.calories.toString()
            textviewFoodName.text = food.foodName
            textviewQuantity.text = QuantityPresenter(mContext, entry).quantityDescription

            progressindicatorHealth.animateProgressTo(
                food.healthRating,
                duration = AnimationDuration.VERY_LONG,
            )

            root.setOnClickListener {
                mOnItemClickListener?.invoke(foodEntry)
            }
        }
    }
}