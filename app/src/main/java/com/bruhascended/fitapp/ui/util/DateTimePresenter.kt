package com.bruhascended.fitapp.ui.util

import android.content.Context
import android.text.format.DateFormat
import android.text.format.DateUtils
import com.bruhascended.fitapp.R
import java.util.*

class DateTimePresenter (
    private val mContext: Context,
    private val mTimeInMillis: Long
) {

    private val yesterdayStr = mContext.getString(R.string.yesterday)
    private val todayStr = mContext.getString(R.string.today)

    val condensedTime: String
    get() {
        val smsTime = Calendar.getInstance().apply { timeInMillis = mTimeInMillis }

        return DateFormat.format(
            if (DateFormat.is24HourFormat(mContext)) "H:mm" else "h:mm aa",
            smsTime
        ).toString()
    }

    val condensedDate: String
    get() {
        val smsTime = Calendar.getInstance().apply {
            timeInMillis = mTimeInMillis
        }

        return DateFormat.format("dd/MM/yyyy", smsTime).toString()
    }

    val fullDate: String
    get() {
        val smsTime = Calendar.getInstance().apply {
            timeInMillis = mTimeInMillis
        }

        val now = Calendar.getInstance()

        return when {
            DateUtils.isToday(mTimeInMillis) ->
                todayStr
            DateUtils.isToday(mTimeInMillis + DateUtils.DAY_IN_MILLIS) ->
                yesterdayStr
            now[Calendar.YEAR] == smsTime[Calendar.YEAR] ->
                DateFormat.format("d MMMM", smsTime).toString()
            else ->
                DateFormat.format("dd/MM/yyyy", smsTime).toString()
        }
    }

    val condensedTimeOrDate: String
    get() {
        val smsTime = Calendar.getInstance().apply { timeInMillis = mTimeInMillis }
        val now = Calendar.getInstance()

        return when {
            DateUtils.isToday(mTimeInMillis) -> DateFormat.format(
                if (DateFormat.is24HourFormat(mContext)) "H:mm" else "h:mm aa",
                smsTime
            ).toString()

            DateUtils.isToday(mTimeInMillis + DateUtils.DAY_IN_MILLIS) -> yesterdayStr

            now[Calendar.WEEK_OF_YEAR] == smsTime[Calendar.WEEK_OF_YEAR] &&
                    now[Calendar.YEAR] == smsTime[Calendar.YEAR] -> DateFormat.format(
                "EEEE", smsTime
            ).toString()

            now[Calendar.YEAR] == smsTime[Calendar.YEAR] -> DateFormat.format(
                "d MMMM", smsTime
            ).toString()

            else -> DateFormat.format("dd/MM/yyyy", smsTime).toString()
        }
    }


    val fullTimeAndDate: String
    get() {
        val smsTime = Calendar.getInstance().apply {
            timeInMillis = mTimeInMillis
        }

        val now = Calendar.getInstance()

        val timeString = DateFormat.format(
            if (DateFormat.is24HourFormat(mContext)) "H:mm" else "h:mm aa",
            smsTime
        ).toString()
        return when {
            DateUtils.isToday(mTimeInMillis) -> timeString
            DateUtils.isToday(mTimeInMillis + DateUtils.DAY_IN_MILLIS) -> "$timeString,\n$yesterdayStr"
            now[Calendar.YEAR] == smsTime[Calendar.YEAR] ->
                timeString + ",\n" + DateFormat.format("d MMMM", smsTime).toString()
            else ->
                timeString + ",\n" + DateFormat.format("dd/MM/yyyy", smsTime).toString()
        }
    }
}