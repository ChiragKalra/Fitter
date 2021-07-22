package com.bruhascended.fitapp.ui.addworkout

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.DatePicker
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.databinding.ActivityAddWorkoutBinding
import com.bruhascended.fitapp.util.DateTimePresenter
import com.bruhascended.fitapp.util.setupToolbar
import java.util.*

class AddWorkoutActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener {

    private lateinit var activityAdapter: ActivityArrayAdapter
    private lateinit var binding: ActivityAddWorkoutBinding
    private var millis: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_workout)
        setupToolbar(binding.toolbar, home = true)

        setUpActivityDropDownListener()
        setUpDatePickerDialog()
    }

    private fun setUpActivityDropDownListener() {
        val builder = AlertDialog.Builder(this)
        activityAdapter = ActivityArrayAdapter(
            this,
            R.layout.item_activity_dropdown,
            ActivitiesMap.getActivityTypesList(this).toMutableList()
        )
        builder.apply {
            setAdapter(activityAdapter, onActivityItemCLicked())
            setTitle(R.string.choose_activity)
        }

        val dialog = builder.create()
        binding.workoutContent.textviewActivity.setOnClickListener { dialog.show() }
    }

    private fun onActivityItemCLicked(): DialogInterface.OnClickListener {
        return DialogInterface.OnClickListener { dialog, which ->
            binding.workoutContent.textviewActivity.setText(activityAdapter.getItem(which)?.activityType)
            dialog.dismiss()
        }
    }


    private fun setUpDatePickerDialog() {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)
        binding.workoutContent.datePicker.setOnClickListener {
            DatePickerDialog(this, this, year, month, day).show()
        }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.MILLISECOND, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.HOUR_OF_DAY, 0)
            set(year, month, dayOfMonth)
        }
        millis = cal.timeInMillis
        binding.workoutContent.datePicker.setText(
            DateTimePresenter(this, millis).condensedDate
        )
    }
}
