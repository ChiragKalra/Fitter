package com.bruhascended.fitapp.ui.addFood

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.MenuItem
import android.widget.DatePicker
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.bruhascended.db.food.entities.Entry
import com.bruhascended.db.food.entities.Food
import com.bruhascended.db.food.types.MealType
import com.bruhascended.db.food.types.NutrientType
import com.bruhascended.db.food.types.QuantityType
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.databinding.ActivityAddCustomFoodBinding
import com.bruhascended.fitapp.repository.FoodEntryRepository
import com.bruhascended.fitapp.util.CustomArrayAdapter
import com.bruhascended.fitapp.util.DateTimePresenter
import com.bruhascended.fitapp.util.setupToolbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.util.*

const val TIME = "time"

class AddCustomFood : AppCompatActivity(), DatePickerDialog.OnDateSetListener {
    private lateinit var binding: ActivityAddCustomFoodBinding
    private val viewsLIst = mutableListOf<TextView>()
    private val nutritionViewsList = mutableListOf<TextView>()
    private var millis: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_custom_food)
        setupToolbar(binding.toolbar, home = true)

        if (savedInstanceState?.get(TIME) == null)
            setUpDefaultValues()
        else millis = savedInstanceState.get(TIME) as Long

        populateViewsList()
        setUpMealDropDown()
        setUpQuantityTypeDropDown()
        setUpDatePickerDialog()

        binding.submit.setOnClickListener {
            checkData()
        }
    }

    /* we are only saving millis(DatePicker time) as millis
    will be lost, although the date text retains but we won't
    be converting the DatePicker text to millis, instead we will
    be syncing millis with DatePicker text*/
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(TIME, millis)
    }

    private fun setUpDefaultValues() {
        millis = Calendar.getInstance().apply {
            set(Calendar.MILLISECOND, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.HOUR_OF_DAY, 0)
        }.timeInMillis
        binding.content.apply {
            datePicker.setText(
                DateTimePresenter(this@AddCustomFood, millis).condensedDate
            )
            mealType.setText(MealType.getCurrentMealType().getString(this@AddCustomFood))
        }
    }

    private fun populateViewsList() {
        viewsLIst.apply {
            add(binding.content.foodName) // 0
            add(binding.content.textviewEnergy)  // 1
            add(binding.content.quantity) // 2
            add(binding.content.amountDropdown)  // 3
            add(binding.content.mealType) // 4
        }
        nutritionViewsList.apply {
            add(binding.content.textviewCarbs) // 0
            add(binding.content.textviewFat)  // 1
            add(binding.content.textviewProtein) // 2
        }
    }

    /* This function is the entry point for checking views and ensures
    views values are not null before calling submitData() */
    private fun checkData() {
        if (checkViews()) {
            if (checkNutrientViews())
                submitData()
            else showNutritionAlertDialog()
        } else showFillDataNutritionDialog()
    }

    private fun checkNutrientViews(): Boolean {
        for (view in nutritionViewsList) {
            if (view.text.isNullOrEmpty()) return false
        }
        return true
    }

    private fun checkViews(): Boolean {
        for (view in viewsLIst) {
            if (view.text.isNullOrEmpty()) return false
        }
        return true
    }

    /* This function helps insert data to db and this function is safe
    and is only called after checking all the views, that whether they
    are null or not */
    private fun submitData() {
        val db by FoodEntryRepository.Delegate(application)
        val weightInfoMap = EnumMap<QuantityType, Double>(QuantityType::class.java)
        val nutrientInfoMap = EnumMap<NutrientType, Double>(NutrientType::class.java)

        weightInfoMap[QuantityType.valueOf(viewsLIst[3].text.toString())] = 1.0
        for (value in NutrientType.values()) {
            nutritionViewsList.let {
                if (!it[value.ordinal].text.isNullOrEmpty())
                    nutrientInfoMap[value] = it[value.ordinal].text.toString().toDouble()
            }
        }
        val food = Food(
            viewsLIst[0].text.toString(),
            viewsLIst[1].text.toString().toDouble() /
                    viewsLIst[2].text.toString().toDouble(),
            weightInfoMap,
            nutrientInfoMap
        )
        val entry = Entry(
            viewsLIst[1].text.toString().toInt(),
            viewsLIst[2].text.toString().toDouble(),
            QuantityType.valueOf(viewsLIst[3].text.toString()),
            MealType.getEnum(viewsLIst[4].text.toString(), this),
            millis
        )
        CoroutineScope(IO).launch { db.writeEntry(food, entry) }
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun showFillDataNutritionDialog() {
        val builder = AlertDialog.Builder(this)
        builder.apply {
            setMessage(getString(R.string.fill_details_dialog_text))
            setPositiveButton(getString(R.string.ok)) { dialog, which ->
                dialog.dismiss()
            }
        }

        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun showNutritionAlertDialog() {
        val builder = AlertDialog.Builder(this)
        builder.apply {
            setMessage(getString(R.string.alert_dialog_text))
            setPositiveButton(getString(R.string.continue_text)) { dialog, which ->
                submitData()
            }
            setNegativeButton(getString(R.string.back)) { dialog, which ->
                dialog.dismiss()
            }
        }

        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun setUpQuantityTypeDropDown() {
        binding.content.amountDropdown.setAdapter(CustomArrayAdapter(this,
            R.layout.view_dropdown_mealtype,
            Array(QuantityType.values().size) {
                QuantityType.values()[it].toString()
            }
        ))
    }

    private fun setUpMealDropDown() {
        binding.content.mealType.setAdapter(CustomArrayAdapter(this,
            R.layout.view_dropdown_mealtype,
            Array(MealType.values().size) {
                MealType.values()[it].getString(this)
            }
        ))
    }

    private fun setUpDatePickerDialog() {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)
        binding.content.datePicker.setOnClickListener {
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
        binding.content.datePicker.setText(
            DateTimePresenter(this, millis).condensedDate
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            onBackPressed()
            true
        }
        else -> false
    }
}