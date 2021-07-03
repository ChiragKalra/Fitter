package com.bruhascended.db.activity.types

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

import com.bruhascended.db.R.string.*
import com.bruhascended.db.R.drawable.*

enum class ActivityType (
    @StringRes
    val stringRes: Int,
    @DrawableRes
    val iconRes: Int,
) {
    Walking(walking, ic_activity_run),
    FitnessWalking(fitness_walking, ic_activity_run),
    Running(running, ic_activity_run),
    Jogging(jogging, ic_activity_run),
    Cycling(cycling, ic_activity_run),
    CrossFit(crossfit, ic_activity_run),
    Weightlifting(weightlifting, ic_activity_run),
    Swimming(swimming, ic_activity_run),
    Aerobics(aerobics, ic_activity_run),
    Dancing(dancing, ic_activity_run),
    Zumba(zumba, ic_activity_run),
    Boxing(boxing, ic_activity_run),
    Kickboxing(kickboxing, ic_activity_run),
    MixedMartialArts(mixed_martial_arts, ic_activity_run),
    Rugby(rugby, ic_activity_run),
    Football(football, ic_activity_run),
    Hockey(hockey, ic_activity_run),
    Golf(golf, ic_activity_run),
    HighIntensityIntervalTraining(high_intensity_interval_training, ic_activity_run),
    IntervalTraining(interval_training, ic_activity_run),
    Yoga(yoga, ic_activity_run),
    Pilates(pilates, ic_activity_run),
    Meditation(meditation, ic_activity_run),
    Other(other, ic_activity_run);

    fun getString(context: Context) = context.getString(stringRes)
}