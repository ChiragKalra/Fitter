package com.bruhascended.fitapp.ui.settings

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.content.withStyledAttributes
import androidx.datastore.preferences.core.Preferences
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.google.android.material.switchmaterial.SwitchMaterial
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.repository.PreferencesKeys
import com.bruhascended.fitapp.repository.PreferencesRepository
import com.bruhascended.fitapp.reminders.MealReminderScheduler
import com.bruhascended.fitapp.reminders.MealReminderTimeFormatter

/**
 * One card: subtitle + toggle, divider, tap-to-adjust time strip with chevron.
 */
class MealReminderPreference(
    context: Context,
    attrs: AttributeSet?,
) : Preference(context, attrs) {

    private lateinit var minutesKeyName: String
    private var defaultMinutes: Int = 0
    private var defaultChecked: Boolean = false

    @StringRes private var pickerTitleStrRes: Int = R.string.settings_meal_pick_time_generic

    /** Host fragment attaches time-picker flow here. */
    var onPickTime: (() -> Unit)? = null

    init {
        layoutResource = R.layout.preference_meal_reminder_card
        widgetLayoutResource = 0
        isSingleLineTitle = false
        isSelectable = false
        context.withStyledAttributes(attrs, R.styleable.MealReminderPreference) {
            minutesKeyName = getString(R.styleable.MealReminderPreference_minutesPreferenceKey).orEmpty()
            defaultMinutes = getInt(R.styleable.MealReminderPreference_defaultMinutesValue, 0)
            defaultChecked = getBoolean(R.styleable.MealReminderPreference_switchDefaultChecked, false)
            val titleForPicker = getResourceId(R.styleable.MealReminderPreference_timePickerTitle, 0)
            if (titleForPicker != 0) {
                pickerTitleStrRes = titleForPicker
            }
        }
    }

    fun pickerTitle(): Int = pickerTitleStrRes

    /** Preference.notifyChanged() is protected; host uses this after persisting the time. */
    fun requestRebind() {
        notifyChanged()
    }

    private fun resolveMinutesKey(): Preferences.Key<Long> =
        when (minutesKeyName) {
            "REMINDER_LUNCH_MINUTES" -> PreferencesKeys.REMINDER_LUNCH_MINUTES
            "REMINDER_DINNER_MINUTES" -> PreferencesKeys.REMINDER_DINNER_MINUTES
            "REMINDER_BREAKFAST_MINUTES" -> PreferencesKeys.REMINDER_BREAKFAST_MINUTES
            "REMINDER_SNACK_MINUTES" -> PreferencesKeys.REMINDER_SNACK_MINUTES
            else -> error("Unknown minutesPreferenceKey \"$minutesKeyName\"")
        }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        val root = holder.itemView

        val titleView = root.findViewById<TextView>(R.id.meal_title)
        val subtitleView = root.findViewById<TextView>(R.id.meal_subtitle)
        val sw = root.findViewById<SwitchMaterial>(R.id.meal_switch)
        val timeRow = root.findViewById<View>(R.id.meal_time_row)
        val timeLabel = root.findViewById<TextView>(R.id.meal_time_label)
        val timeValue = root.findViewById<TextView>(R.id.meal_time_value)
        val chevron = root.findViewById<ImageView>(R.id.meal_time_chevron)
        (holder.findViewById(android.R.id.icon) as? ImageView)?.also { iv ->
            if (icon != null) {
                iv.setImageDrawable(icon)
                iv.visibility = View.VISIBLE
            } else {
                iv.setImageDrawable(null)
                iv.visibility = View.GONE
            }
        }

        titleView.text = title

        val checked = preferenceDataStore?.getBoolean(key, defaultChecked) ?: defaultChecked
        subtitleView.setText(
            if (checked) R.string.settings_meal_subtitle_active else R.string.settings_meal_subtitle_muted,
        )

        fun applySubtitle(enabled: Boolean) {
            subtitleView.setText(
                if (enabled) R.string.settings_meal_subtitle_active else R.string.settings_meal_subtitle_muted,
            )
        }

        sw.setOnCheckedChangeListener(null)
        sw.isChecked = checked
        lateinit var switchListener: CompoundButton.OnCheckedChangeListener
        switchListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
            if (callChangeListener(isChecked)) {
                persistBoolean(isChecked)
                applySubtitle(isChecked)
                bindTimeUi(timeRow, timeLabel, timeValue, chevron, isChecked)
            } else {
                sw.setOnCheckedChangeListener(null)
                sw.isChecked = !isChecked
                sw.setOnCheckedChangeListener(switchListener)
            }
        }
        sw.setOnCheckedChangeListener(switchListener)

        val repo = PreferencesRepository(context.applicationContext)
        val stored = repo.getPreference(resolveMinutesKey()) as? Long
        val minutes = MealReminderScheduler.clampMinutes(stored ?: defaultMinutes.toLong())
        timeValue.text = MealReminderTimeFormatter.formatMinutes(context, minutes)

        bindTimeUi(timeRow, timeLabel, timeValue, chevron, checked)

        timeRow.setOnClickListener {
            if (!sw.isChecked) return@setOnClickListener
            onPickTime?.invoke()
        }
    }

    private fun bindTimeUi(
        row: View,
        timeLabel: TextView,
        timeValue: TextView,
        chevron: ImageView,
        enabled: Boolean,
    ) {
        row.isEnabled = enabled
        row.alpha = if (enabled) 1f else 0.45f
        timeLabel.setTypeface(timeLabel.typeface, if (enabled) Typeface.NORMAL else Typeface.ITALIC)
        timeValue.alpha = if (enabled) 1f else 0.8f
        chevron.alpha = if (enabled) 1f else 0.3f
    }
}
