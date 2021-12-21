package com.bruhascended.fitapp.ui.foodjournal

import android.content.Context
import android.graphics.drawable.TransitionDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bruhascended.db.R.string.calorie_count
import com.bruhascended.db.food.entities.DayEntry
import com.bruhascended.db.food.entities.FoodEntry
import com.bruhascended.db.food.types.NutrientType
import com.bruhascended.db.food.types.QuantityType
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.databinding.ItemFoodEntryBinding
import com.bruhascended.fitapp.databinding.ItemFooterBinding
import com.bruhascended.fitapp.databinding.ItemSeparatorFoodentryBinding
import com.bruhascended.fitapp.ui.foodjournal.FoodJournalRecyclerAdapter.FoodEntryItemHolder
import com.bruhascended.fitapp.repository.PreferencesRepository
import com.bruhascended.fitapp.util.*
import java.util.*


class FoodJournalRecyclerAdapter (
    private val mContext: Context,
    private val lastItemLiveSet: MutableLiveData<HashSet<Long>>
): PagingDataAdapter<DateSeparatedItem, FoodEntryItemHolder> (
    DateSeparatedItem.Comparator()
) {

    class FoodEntryItemHolder (
        root: View,
        val itemBinding: ItemFoodEntryBinding? = null,
        val separatorBinding: ItemSeparatorFoodentryBinding? = null,
    ) : RecyclerView.ViewHolder(root) {
        var layoutNutrientsWrapHeight =
            root.context.resources.getDimension(R.dimen.nutrient_details_height).toInt()
        var lastItemObserver: Observer<HashSet<Long>>? = null
        var separatorInfoObserver: Observer<DayEntry?>? = null
        var lastLiveDayEntry: LiveData<DayEntry?>? = null
        var isLastItemBg = false
    }

    private var mOnItemClickListener: ((foodEntry: FoodEntry) -> Unit)? = null

    private val prefRepo = PreferencesRepository(mContext)

    fun setOnItemClickListener (listener: ((foodEntry: FoodEntry) -> Unit)?) {
        mOnItemClickListener = listener
    }

    override fun getItemViewType (position: Int) =
        getItem(position)?.type?.ordinal ?: 0

    override fun onCreateViewHolder (parent: ViewGroup, viewType: Int): FoodEntryItemHolder {
        return when (viewType) {
            DateSeparatedItem.ItemType.Item.ordinal -> {
                val binding = ItemFoodEntryBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                FoodEntryItemHolder(binding.root, itemBinding = binding)
            }
            DateSeparatedItem.ItemType.Separator.ordinal -> {
                val binding = ItemSeparatorFoodentryBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                FoodEntryItemHolder(binding.root, separatorBinding = binding)
            }
            else -> {
                val binding = ItemFooterBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                FoodEntryItemHolder(binding.root)
            }
        }
    }

    private fun ItemSeparatorFoodentryBinding.presentSeparator(
        separator: Date,
        holder: FoodEntryItemHolder,
        liveDayEntry: LiveData<DayEntry?>
    ) {
        holder.separatorInfoObserver?.apply {
            holder.lastLiveDayEntry?.removeObserver(this)
        }

        holder.separatorInfoObserver = Observer<DayEntry?> {
            val separatorInfo = it ?: return@Observer

            textviewDate.text = DateTimePresenter(mContext, separator.time).fullDate

            textviewCalories.text = mContext.getString(
                calorie_count,
                separatorInfo.calories.toString()
            )
            // TODO: Set Using User Preference
            progressbarCalories.apply {
                progress = 0f
                progressMax = 1800f
                setProgressWithAnimation(
                    separatorInfo.calories.toFloat(),
                    AnimationDuration.VERY_LONG.ms
                )
            }

            separatorInfo.nutrientInfo.forEach { (type, value) ->
                if (type == null) return@forEach
                when (type) {
                    NutrientType.Protein -> textviewProteinGram
                    NutrientType.Carbs -> textviewCarbsGram
                    NutrientType.Fat -> textviewFatGram
                }.text = QuantityType.Gram.toString(mContext, value)

                when (type) {
                    NutrientType.Protein -> progressbarProtein
                    NutrientType.Carbs -> progressbarCarbs
                    NutrientType.Fat -> progressbarFat
                }.apply {
                    // TODO: Set Using User Preference
                    progressMax = 100f
                    progress = 0f
                    setProgressWithAnimation(value.toFloat(), AnimationDuration.VERY_LONG.ms)
                }
            }
        }

        holder.lastLiveDayEntry = liveDayEntry
        holder.separatorInfoObserver?.apply {
            holder.lastLiveDayEntry?.observeForever(this)
        }
    }

    private fun ItemFoodEntryBinding.presentItem(
        foodEntry: FoodEntry,
        holder: FoodEntryItemHolder,
    ) {
        val food = foodEntry.food
        val entry = foodEntry.entry

        textviewMeal.text = entry.mealType.getString(mContext)
        textviewCalories.text = mContext.getString(
            calorie_count,
            entry.calories.toString()
        )
        textviewFoodName.text = food.foodName
        textviewQuantity.text = entry.quantityType.toString(mContext, entry.quantity)

        val weight = entry.quantity * (food.weightInfo[entry.quantityType] ?: .0)
        food.nutrientInfo.forEach { (type, value) ->
            if (type == null) return@forEach
            when (type) {
                NutrientType.Protein -> textviewProteinGram
                NutrientType.Carbs -> textviewCarbsGram
                NutrientType.Fat -> textviewFatGram
            }.text = QuantityType.Gram.toString(mContext, value*weight)
        }

        root.setOnClickListener {
            mOnItemClickListener?.invoke(foodEntry)
        }

        var expanded = false
        layoutNutrients.layoutParams = layoutNutrients.layoutParams.apply {
            height = 0
        }
        buttonExpand.scaleY = 1f
        if (food.nutrientInfo.isEmpty()) {
            buttonExpand.visibility = View.INVISIBLE
        } else {
            buttonExpand.setOnClickListener {
                layoutNutrients.animateHeightTo(
                    if (expanded) 0 else holder.layoutNutrientsWrapHeight
                )
                buttonExpand.animateScaleY(if (expanded) 1f else -1f)
                expanded = !expanded
            }
        }

        val bg = layoutRoot.background as TransitionDrawable
        bg.isCrossFadeEnabled = true

        holder.lastItemObserver?.apply {
            lastItemLiveSet.removeObserver(this)
        }
        holder.lastItemObserver = Observer<HashSet<Long>> { set ->
            if (entry.entryId in set) {
                if (!holder.isLastItemBg) {
                    bg.reverseTransition(300)
                    holder.isLastItemBg = true
                }
            } else {
                if (holder.isLastItemBg) {
                    bg.reverseTransition(300)
                    holder.isLastItemBg = false
                }
            }
        }
        holder.lastItemObserver?.apply {
            lastItemLiveSet.observeForever(this)
        }
    }

    override fun onBindViewHolder (holder: FoodEntryItemHolder, position: Int) {
        val item = getItem(position) ?: return
        when (item.type) {
            DateSeparatedItem.ItemType.Separator ->
                holder.separatorBinding?.presentSeparator(
                    item.separator!!,
                    holder,
                    item.liveDayEntry!!
                )
            DateSeparatedItem.ItemType.Item ->
                holder.itemBinding?.presentItem(item.item!!, holder)
        }
    }
}
