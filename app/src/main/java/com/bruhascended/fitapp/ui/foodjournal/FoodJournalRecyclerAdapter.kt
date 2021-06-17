package com.bruhascended.fitapp.ui.foodjournal

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bruhascended.db.food.entities.FoodEntry
import com.bruhascended.db.food.types.NutrientType
import com.bruhascended.db.food.types.QuantityType
import com.bruhascended.db.food.types.QuantityType.Companion.doubleToString
import com.bruhascended.db.R.string.calorie_count
import com.bruhascended.fitapp.databinding.ItemFoodEntryBinding
import com.bruhascended.fitapp.databinding.ItemSeparatorFoodentryBinding
import com.bruhascended.fitapp.ui.foodjournal.FoodJournalRecyclerAdapter.FoodEntryItemHolder
import com.bruhascended.fitapp.util.*
import java.lang.IndexOutOfBoundsException
import java.text.SimpleDateFormat
import java.util.*

class FoodJournalRecyclerAdapter (
    private val mContext: Context
): PagingDataAdapter<DateSeparatedItem, FoodEntryItemHolder> (
    DateSeparatedItemComparator()
) {

    private var mOnItemClickListener: ((foodEntry: FoodEntry) -> Unit)? = null

    class FoodEntryItemHolder (
        root: View,
        val itemBinding: ItemFoodEntryBinding? = null,
        val separatorBinding: ItemSeparatorFoodentryBinding? = null,
    ) : RecyclerView.ViewHolder(root) {
        var layoutNutrientsWrapHeight = root.context.toPx(36).toInt()
    }

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

    override fun onBindViewHolder (holder: FoodEntryItemHolder, position: Int) {
        val item = getItem(position) ?: return
        val foodEntry = item.item
        if (item.isSeparator && item.separator != null) {
            holder.separatorBinding?.apply {
                textviewDate.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    .format(item.separator)

                textviewCalories.text = mContext.getString(
                    calorie_count,
                    doubleToString(item.totalCalories)
                )
                // TODO: Set Using User Preference
                progressbarCalories.apply {
                    progress = 0f
                    progressMax = 1800f
                    setProgressWithAnimation(item.totalCalories.toFloat())
                }

                item.totalNutrients.forEach { (type, value) ->
                    if (type == null) return@forEach
                    when (type) {
                        NutrientType.Protein -> textviewProteinGram
                        NutrientType.Carbs -> textviewCarbsGram
                        NutrientType.Fiber -> textviewFiberGram
                        NutrientType.Fat -> textviewFatGram
                    }.text = QuantityType.Gram.toString(mContext, value)

                    when (type) {
                        NutrientType.Protein -> progressbarProtein
                        NutrientType.Carbs -> progressbarCarbs
                        NutrientType.Fiber -> progressbarFiber
                        NutrientType.Fat -> progressbarFat
                    }.apply {
                        // TODO: Set Using User Preference
                        progressMax = 100f
                        progress = 0f
                        setProgressWithAnimation(value.toFloat(), AnimationDuration.VERY_LONG.ms)
                    }
                }
            }
        } else if (foodEntry != null) {
            val food = foodEntry.food
            val entry = foodEntry.entry
            holder.itemBinding?.apply {
                textviewMeal.text = entry.mealType.getString(mContext)
                textviewCalories.text = mContext.getString(
                    calorie_count,
                    doubleToString(entry.calories)
                )
                textviewFoodName.text = food.foodName
                textviewQuantity.text = entry.quantityType.toString(mContext, entry.quantity)

                val weight = entry.quantity * (food.weightInfo[entry.quantityType] ?: .0) / 100.0
                food.nutrientInfo.forEach { (type, value) ->
                    if (type == null) return@forEach
                    when (type) {
                        NutrientType.Protein -> textviewProteinGram
                        NutrientType.Carbs -> textviewCarbsGram
                        NutrientType.Fiber -> textviewFiberGram
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
                buttonExpand.setOnClickListener {
                    layoutNutrients.animateHeightTo(
                        if (expanded) 0 else holder.layoutNutrientsWrapHeight
                    )
                    buttonExpand.animateScaleY(if (expanded) 1f else -1f)
                    expanded = !expanded
                }


                val shouldDisplayDivider = try {
                    getItem(position + 1)?.isSeparator == false
                } catch (e: IndexOutOfBoundsException) {
                    false
                }
                bottomDivider.visibility = if (shouldDisplayDivider)
                    View.VISIBLE else View.INVISIBLE
            }
        }
    }
}