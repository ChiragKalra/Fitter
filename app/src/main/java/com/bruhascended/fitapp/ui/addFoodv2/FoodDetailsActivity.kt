package com.bruhascended.fitapp.ui.addFoodv2

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.DatePicker
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.bruhascended.api.models.foodsv2.Hint
import com.bruhascended.db.food.types.MealType
import com.bruhascended.db.food.types.QuantityType
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.databinding.ActivityFoodDetailsBinding
import com.bruhascended.fitapp.util.CustomArrayAdapter
import com.bruhascended.fitapp.util.FoodNutrientDetails
import com.bruhascended.fitapp.util.setupToolbar
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import java.util.*
import kotlin.collections.ArrayList

class FoodDetailsActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener {
    private lateinit var binding: ActivityFoodDetailsBinding
    private val viewModel: FoodDetailsActivityViewModel by viewModels()
    private val foodDetails = FoodNutrientDetails()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_food_details)
        setupToolbar(binding.toolbar, home = true)
        binding.content.viewModel = viewModel
        binding.setLifecycleOwner { lifecycle }

        // setUp intent
        val intent = intent
        val hint = intent.getSerializableExtra(FoodSearchActivityv2.KEY_FOOD_DATA) as Hint
        viewModel.setData(hint)

        setUpMealDropDown()
        setUpDatePickerDialog()
        setUpQuantityTypeDropDownItemListener()
        setUpTextChangeListener()

        // setUp live data observer for quantity type drop down
        viewModel.typeArrayItems.observe({ lifecycle }) { it ->
            binding.content.amountDropdown.setAdapter(
                CustomArrayAdapter(
                    this,
                    R.layout.view_dropdown_mealtype,
                    it.map { it.toString() }.toTypedArray()
                )
            )
        }
    }


    private fun setUpTextChangeListener() {
        binding.content.quantity.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                foodDetails.quantity = s.toString().toDouble()
                if (foodDetails.quantityType != null) viewModel.calculateNutrientData(foodDetails)
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })
    }

    private fun setUpQuantityTypeDropDownItemListener() {
        binding.content.amountDropdown.setOnItemClickListener { parent, view, position, id ->
            foodDetails.quantityType =
                QuantityType.valueOf(parent.getItemAtPosition(position).toString())
            if (foodDetails.quantity != null) viewModel.calculateNutrientData(foodDetails)
        }
    }

    private fun setUpMealDropDown() {
        binding.content.mealType.setAdapter(CustomArrayAdapter(this,
            R.layout.view_dropdown_mealtype,
            Array(MealType.values().size) {
                MealType.values()[it].toString()
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
        binding.content.datePicker.setText(
            String.format(
                "%02d/%02d/%d",
                dayOfMonth,
                month + 1,
                year
            )
        )
    }
}