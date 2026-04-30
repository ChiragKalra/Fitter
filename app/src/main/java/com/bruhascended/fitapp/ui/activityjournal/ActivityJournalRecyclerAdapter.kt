package com.bruhascended.fitapp.ui.activityjournal

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
import com.bruhascended.db.R.string.*
import com.bruhascended.db.activity.entities.ActivityEntry
import com.bruhascended.db.activity.entities.DayEntry
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.databinding.ItemActivityEntryBinding
import com.bruhascended.fitapp.databinding.ItemFooterBinding
import com.bruhascended.fitapp.databinding.ItemSeparatorActivityentryBinding
import com.bruhascended.fitapp.util.*
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*


class ActivityJournalRecyclerAdapter (
    private val mContext: Context,
    private val lastItemLiveSet: MutableLiveData<HashSet<Long>>
): PagingDataAdapter<DateSeparatedItem, ActivityJournalRecyclerAdapter.ActivityEntryItemHolder> (
    DateSeparatedItem.Comparator()
) {

    class ActivityEntryItemHolder (
        root: View,
        val itemBinding: ItemActivityEntryBinding? = null,
        val separatorBinding: ItemSeparatorActivityentryBinding? = null,
    ) : RecyclerView.ViewHolder(root) {
        var layoutNutrientsWrapHeight =
            root.context.resources.getDimension(R.dimen.activity_details_height).toInt()
        var lastItemObserver: Observer<HashSet<Long>>? = null
        var separatorInfoObserver: Observer<DayEntry?>? = null
        var liveSeparatorInfo: LiveData<DayEntry?>? = null
        var isLastItemBg = false
    }

    private var mOnItemClickListener: ((foodEntry: ActivityEntry) -> Unit)? = null

    fun setOnItemClickListener (listener: ((foodEntry: ActivityEntry) -> Unit)?) {
        mOnItemClickListener = listener
    }

    override fun getItemViewType (position: Int) =
        getItem(position)?.type?.ordinal ?: 0

    private fun doubleToString(d: Double): String {
        return DecimalFormat(
            "0",
            DecimalFormatSymbols.getInstance(Locale.getDefault())
        ).apply {
            maximumFractionDigits = 2
        }.format(d)
    }

    override fun onCreateViewHolder (parent: ViewGroup, viewType: Int): ActivityEntryItemHolder {
        return when (viewType) {
            DateSeparatedItem.ItemType.Item.ordinal -> {
                val binding = ItemActivityEntryBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                ActivityEntryItemHolder(binding.root, itemBinding = binding)
            }
            DateSeparatedItem.ItemType.Separator.ordinal -> {
                val binding = ItemSeparatorActivityentryBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                ActivityEntryItemHolder(binding.root, separatorBinding = binding)
            }
            else -> {
                val binding = ItemFooterBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                ActivityEntryItemHolder(binding.root)
            }
        }
    }

    private fun ItemSeparatorActivityentryBinding.presentSeparator(
        separator: Date,
        holder: ActivityEntryItemHolder,
        liveDayEntry: LiveData<DayEntry?>,
    ) {
        holder.separatorInfoObserver?.apply {
            holder.liveSeparatorInfo?.removeObserver(this)
        }

        holder.separatorInfoObserver = Observer<DayEntry?> {
            val separatorInfo = it ?: return@Observer

            textviewDate.text = DateTimePresenter(mContext, separator.time).fullDate

            textviewCalories.text = mContext.getString(
                calorie_count,
                separatorInfo.totalCalories.toInt().toString()
            )
            // TODO: Set Using User Preference
            progressbarCalories.apply {
                progress = 0f
                progressMax = 1800f
                setProgressWithAnimation(separatorInfo.totalCalories)
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

        holder.liveSeparatorInfo = liveDayEntry
        holder.separatorInfoObserver?.apply {
            holder.liveSeparatorInfo?.observeForever(this)
        }
    }

    private fun ItemActivityEntryBinding.presentItem(
        entry: ActivityEntry,
        holder: ActivityEntryItemHolder,
    ) {
        textviewTime.text = mContext.getString(
            R.string.to_join,
            DateTimePresenter(mContext, entry.startTime).condensedTime,
            DateTimePresenter(mContext, entry.endTime).condensedTime,
        )
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
            } else {
                textviewMoveMin.visibility = View.INVISIBLE
            }
            if (info.distance != null) {
                textviewDistanceKm.text = mContext.getString(
                    // TODO Mile / KM setting check
                    if (true) distance_km_count else distance_mi_count,
                    doubleToString(info.distance!!)
                )
            } else {
                textviewDistanceKm.visibility = View.INVISIBLE
            }
            if (info.steps != null) {
                textviewSteps.text = mContext.getString(
                    steps_count,
                    info.steps.toString()
                )
            } else {
                textviewDistanceKm.visibility = View.INVISIBLE
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
        val bg = layoutRoot.background as TransitionDrawable
        bg.isCrossFadeEnabled = true

        holder.lastItemObserver?.apply {
            lastItemLiveSet.removeObserver(this)
        }
        holder.lastItemObserver = Observer<HashSet<Long>> { set ->
            if (entry.id in set) {
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

    override fun onBindViewHolder (holder: ActivityEntryItemHolder, position: Int) {
        val item = getItem(position) ?: return
        when (item.type) {
            DateSeparatedItem.ItemType.Separator ->
                holder.separatorBinding?.presentSeparator(
                    item.separator!!, holder, item.liveDayEntry!!
                )
            DateSeparatedItem.ItemType.Item ->
                holder.itemBinding?.presentItem(item.item!!, holder)
        }
    }
}