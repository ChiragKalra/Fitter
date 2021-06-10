package com.bruhascended.fitapp.ui.foodjournal

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bruhascended.db.food.entities.FoodEntry
import com.bruhascended.fitapp.databinding.ItemFoodEntryBinding
import com.bruhascended.fitapp.databinding.ItemSeparatorDateBinding
import com.bruhascended.fitapp.ui.foodjournal.FoodJournalRecyclerAdapter.FoodEntryItemHolder
import com.bruhascended.fitapp.util.*
import com.bruhascended.fitapp.util.datetime.DateSeparatedItem
import com.bruhascended.fitapp.util.datetime.DateSeparatedItemComparator
import com.bruhascended.fitapp.util.datetime.DateTimePresenter
import java.text.SimpleDateFormat
import java.util.*

class FoodJournalRecyclerAdapter (
    private val mContext: Context
): PagingDataAdapter<DateSeparatedItem<FoodEntry>, FoodEntryItemHolder> (
    DateSeparatedItemComparator()
) {

    private var mOnItemClickListener: ((foodEntry: FoodEntry) -> Unit)? = null

    class FoodEntryItemHolder (
        val root: View,
        val itemBinding: ItemFoodEntryBinding? = null,
        val separatorBinding: ItemSeparatorDateBinding? = null,
    ) : RecyclerView.ViewHolder(root)

    fun setOnItemClickListener (listener: ((foodEntry: FoodEntry) -> Unit)?) {
        mOnItemClickListener = listener
    }

    override fun getItemViewType (position: Int): Int {
        return if (getItem(position)?.item != null) 0 else 1
    }

    override fun onCreateViewHolder (parent: ViewGroup, viewType: Int): FoodEntryItemHolder {
        return if (viewType == 0) {
            val binding = ItemFoodEntryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            FoodEntryItemHolder(binding.root, itemBinding = binding)
        } else {
            val binding = ItemSeparatorDateBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            FoodEntryItemHolder(binding.root, separatorBinding = binding)
        }
    }

    override fun onBindViewHolder (holder: FoodEntryItemHolder, position: Int) {
        val item = getItem(position) ?: return
        val foodEntry = item.item
        if (foodEntry != null) {
            val food = foodEntry.food
            val entry = foodEntry.entry
            holder.itemBinding?.apply {
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
        } else if (item.separator != null) {
            holder.separatorBinding?.apply {
                textviewDate.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    .format(item.separator)
            }
        }
    }
}