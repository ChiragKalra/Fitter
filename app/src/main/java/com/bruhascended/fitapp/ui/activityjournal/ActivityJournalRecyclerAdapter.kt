package com.bruhascended.fitapp.ui.activityjournal

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bruhascended.db.R.string.*
import com.bruhascended.db.activity.entities.ActivityEntry
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.databinding.ItemActivityEntryBinding
import com.bruhascended.fitapp.databinding.ItemSeparatorActivityentryBinding
import com.bruhascended.fitapp.repository.ActivityEntryRepository
import com.bruhascended.fitapp.util.*
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.HashSet


class ActivityJournalRecyclerAdapter (
    private val mContext: Context,
    private val lastItemLiveSet: MutableLiveData<HashSet<Long>>,
    private val separatorInfoLiveMap:
        MutableLiveData<HashMap<Date, ActivityEntryRepository.SeparatorInfo>>
): PagingDataAdapter<DateSeparatedItem, ActivityJournalRecyclerAdapter.ActivityEntryItemHolder> (
    DateSeparatedItem.Comparator()
) {

    class ActivityEntryItemHolder (
        root: View,
        val itemBinding: ItemActivityEntryBinding? = null,
        val separatorBinding: ItemSeparatorActivityentryBinding? = null,
    ) : RecyclerView.ViewHolder(root) {
        var layoutNutrientsWrapHeight = root.context.toPx(36)
        var lastItemObserver: Observer<HashSet<Long>>? = null
        var separatorInfoObserver:
                Observer<HashMap<Date, ActivityEntryRepository.SeparatorInfo>>? = null
    }

    private var mOnItemClickListener: ((foodEntry: ActivityEntry) -> Unit)? = null

    fun setOnItemClickListener (listener: ((foodEntry: ActivityEntry) -> Unit)?) {
        mOnItemClickListener = listener
    }

    override fun getItemViewType (position: Int): Int {
        return if (getItem(position)?.item != null) 0 else 1
    }

    private fun doubleToString(d: Double): String {
        return DecimalFormat(
            "0",
            DecimalFormatSymbols.getInstance(Locale.getDefault())
        ).apply {
            maximumFractionDigits = 2
        }.format(d)
    }

    override fun onCreateViewHolder (parent: ViewGroup, viewType: Int): ActivityEntryItemHolder {
        return if (viewType == 0) {
            val binding = ItemActivityEntryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            ActivityEntryItemHolder(binding.root, itemBinding = binding)
        } else {
            val binding = ItemSeparatorActivityentryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            ActivityEntryItemHolder(binding.root, separatorBinding = binding)
        }
    }

    private fun ItemSeparatorActivityentryBinding.presentSeparator(
        separator: Date,
        holder: ActivityEntryItemHolder,
    ) {
        holder.separatorInfoObserver?.apply {
            separatorInfoLiveMap.removeObserver(this)
        }

        holder.separatorInfoObserver = Observer<HashMap<Date, ActivityEntryRepository.SeparatorInfo>> {
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

            separatorInfo.also { info ->
                val timeInMins = info.totalDuration / (1000*60)
                textviewMoveMin.text = mContext.getString(
                    move_min_count,
                    timeInMins.toString()
                )
                progressbarMoveMin.apply {
                    // TODO: Set Using User Preference
                    progressMax = 60f
                    progress = 0f
                    setProgressWithAnimation(timeInMins.toFloat(), AnimationDuration.VERY_LONG.ms)
                }

                textviewDistance.text = mContext.getString(
                    // TODO Mile / KM setting check
                    if (true) distance_km_count else distance_mi_count,
                    doubleToString(info.totalDistance)
                )
                progressbarDistance.apply {
                    // TODO: Set Using User Preference
                    progressMax = 1f
                    progress = 0f
                    setProgressWithAnimation(
                        info.totalDistance.toFloat(),
                        AnimationDuration.VERY_LONG.ms
                    )
                }

                textviewSteps.text = mContext.getString(
                    steps_count,
                    info.totalSteps.toString()
                )
                progressbarSteps.apply {
                    // TODO: Set Using User Preference
                    progressMax = 5000f
                    progress = 0f
                    setProgressWithAnimation(
                        info.totalSteps.toFloat(),
                        AnimationDuration.VERY_LONG.ms
                    )
                }
            }
        }

        holder.separatorInfoObserver?.apply {
            separatorInfoLiveMap.observeForever(this)
        }
    }

    private fun ItemActivityEntryBinding.presentItem(
        entry: ActivityEntry,
        holder: ActivityEntryItemHolder,
    ) {

        textviewTime.text = DateTimePresenter(mContext, entry.startTime).condensedTime
        textviewCalories.text = mContext.getString(
            calorie_count,
            entry.calories.toString()
        )
        textviewActivityName.text = entry.activity.getString(mContext)
        iconActivity.setImageResource(entry.activity.iconRes)

        entry.also { info ->
            if (info.duration != null) {
                textviewMoveMin.text = mContext.getString(
                    move_min_count,
                    (info.duration!! / (1000 * 60)).toString()
                )
            }
            if (info.distance != null) {
                textviewDistanceKm.text = mContext.getString(
                    // TODO Mile / KM setting check
                    if (true) distance_km_count else distance_mi_count,
                    doubleToString(info.distance!!)
                )
            }
            if (info.steps != null) {
                textviewSteps.text = mContext.getString(
                    steps_count,
                    info.steps.toString()
                )
            }
        }

        root.setOnClickListener {
            mOnItemClickListener?.invoke(entry)
        }

        var expanded = false
        layoutNutrients.layoutParams = layoutNutrients.layoutParams.apply {
            height = 0
        }
        buttonExpand.scaleY = 1f
        if (!entry.hasExtraInfo) {
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
            val isLastItem = entry.id in set
            layoutRoot.setBackgroundResource(
                if (isLastItem) R.drawable.bg_foodjournal_item_end
                else R.drawable.bg_foodjournal_item
            )
            layoutRoot.layoutParams = (layoutRoot.layoutParams as ViewGroup.MarginLayoutParams).also {
                it.bottomMargin = if (isLastItem) mContext.toPx(12) else 0
            }
        }
        holder.lastItemObserver?.apply {
            lastItemLiveSet.observeForever(this)
        }
    }

    override fun onBindViewHolder (holder: ActivityEntryItemHolder, position: Int) {
        val item = getItem(position) ?: return
        val foodEntry = item.item
        if (item.isSeparator) {
            holder.separatorBinding?.presentSeparator(item.separator!!, holder)
        } else if (foodEntry != null) {
            holder.itemBinding?.presentItem(foodEntry, holder)
        }
    }
}