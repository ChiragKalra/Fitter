package com.bruhascended.fitapp.ui.foodjournal

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bruhascended.db.food.entities.FoodEntry
import com.bruhascended.db.food.types.NutrientType
import com.bruhascended.db.food.types.QuantityType
import com.bruhascended.db.R.string.calorie_count
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.databinding.ItemFoodEntryBinding
import com.bruhascended.fitapp.databinding.ItemSeparatorFoodentryBinding
import com.bruhascended.fitapp.ui.foodjournal.FoodJournalRecyclerAdapter.FoodEntryItemHolder
import com.bruhascended.fitapp.util.*
import java.util.Date
import kotlin.collections.HashMap
import kotlin.collections.HashSet


class FoodJournalRecyclerAdapter (
    private val mContext: Context,
    private val lastItemLiveSet: MutableLiveData<HashSet<Long>>,
    private val separatorInfoLiveMap:
        MutableLiveData<HashMap<Date, FoodJournalViewModel.SeparatorInfo>>
): PagingDataAdapter<DateSeparatedItem, FoodEntryItemHolder> (
    DateSeparatedItemComparator()
) {

    class FoodEntryItemHolder (
        root: View,
        val itemBinding: ItemFoodEntryBinding? = null,
        val separatorBinding: ItemSeparatorFoodentryBinding? = null,
    ) : RecyclerView.ViewHolder(root) {
        var layoutNutrientsWrapHeight = root.context.toPx(36).toInt()
        var lastItemObserver: Observer<HashSet<Long>>? = null
        var separatorInfoObserver:
                Observer<HashMap<Date, FoodJournalViewModel.SeparatorInfo>>? = null
    }

    private var mOnItemClickListener: ((foodEntry: FoodEntry) -> Unit)? = null

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
            val binding = ItemSeparatorFoodentryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            FoodEntryItemHolder(binding.root, separatorBinding = binding)
        }
    }

    private fun ItemSeparatorFoodentryBinding.presentSeparator(
        separator: Date,
        holder: FoodEntryItemHolder,
    ) {
        holder.separatorInfoObserver?.apply {
            separatorInfoLiveMap.removeObserver(this)
        }

        holder.separatorInfoObserver = Observer<HashMap<Date, FoodJournalViewModel.SeparatorInfo>> {
            val separatorInfo = it[separator] ?: return@Observer

            textviewDate.text = DateTimePresenter(mContext, separator.time).fullDate

            textviewCalories.text = mContext.getString(
                calorie_count,
                separatorInfo.totalCalories.toString()
            )
            // TODO: Set Using User Preference
            progressbarCalories.apply {
                progress = 0f
                progressMax = 1800f
                setProgressWithAnimation(separatorInfo.totalCalories.toFloat())
            }

            separatorInfo.totalNutrients.forEach { (type, value) ->
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

        holder.separatorInfoObserver?.apply {
            separatorInfoLiveMap.observeForever(this)
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

        holder.lastItemObserver?.apply {
            lastItemLiveSet.removeObserver(this)
        }
        holder.lastItemObserver = Observer<HashSet<Long>> { set ->
            val isLastItem = entry.entryId in set
            layoutRoot.setBackgroundResource(
                if (isLastItem) R.drawable.bg_foodjournal_item_end
                else R.drawable.bg_foodjournal_item
            )
            layoutRoot.layoutParams = (layoutRoot.layoutParams as ViewGroup.MarginLayoutParams).also {
                it.bottomMargin = if (isLastItem) mContext.toPx(12).toInt() else 0
            }
        }
        holder.lastItemObserver?.apply {
            lastItemLiveSet.observeForever(this)
        }
    }

    override fun onBindViewHolder (holder: FoodEntryItemHolder, position: Int) {
        val item = getItem(position) ?: return
        val foodEntry = item.item
        if (item.isSeparator) {
            holder.separatorBinding?.presentSeparator(item.separator!!, holder)
        } else if (foodEntry != null) {
            holder.itemBinding?.presentItem(foodEntry, holder)
        }
    }
}