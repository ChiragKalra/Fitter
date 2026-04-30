package com.bruhascended.fitapp.ui.dashboard

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.bruhascended.db.R
import com.bruhascended.db.activity.entities.DayEntry
import com.bruhascended.fitapp.databinding.ItemCardActivityBinding
import com.bruhascended.fitapp.util.AnimationDuration
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*


class ActivityCardPresenter (
    private val mContext: Context,
    mInflater: LayoutInflater,
) {

    private val binding = ItemCardActivityBinding.inflate(mInflater)

    val view: View
    get() = binding.root
    
    private fun doubleToString(d: Double): String {
        return DecimalFormat(
            "0",
            DecimalFormatSymbols.getInstance(Locale.getDefault())
        ).apply {
            maximumFractionDigits = 2
        }.format(d)
    }

    private fun ItemCardActivityBinding.presentSeparator(
        separatorInfo: DayEntry
    ) { 

        textviewCalories.text = mContext.getString(
            R.string.calorie_count,
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
                R.string.move_min_count,
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
                if (true) R.string.distance_km_count else R.string.distance_mi_count,
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
                R.string.steps_count,
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


    private var previousValues: DayEntry? = null
    fun generateCard(values: DayEntry) {
        if (values == previousValues) return
        binding.presentSeparator(values)
        previousValues = values
    }
}
