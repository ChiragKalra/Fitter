package com.bruhascended.fitapp.ui.addFood

import android.app.Activity
import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.bruhascended.api.models.foodsv2.Hint
import com.bruhascended.db.food.types.MealType
import com.bruhascended.db.food.types.QuantityType
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.databinding.ActivityFoodDetailsBinding
import com.bruhascended.fitapp.util.CustomArrayAdapter
import com.bruhascended.fitapp.util.FoodNutrientDetails
import com.bruhascended.fitapp.util.setupToolbar
import java.util.*

class FoodDetailsActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener {
    private lateinit var binding: ActivityFoodDetailsBinding
    private lateinit var viewModel: FoodDetailsActivityViewModel
    private val foodDetails = FoodNutrientDetails()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_food_details)
        setupToolbar(binding.toolbar, home = true)

        // viewModel
        val viewModelFactory = FoodDetailsViewModelFactory(application)
        viewModel =
            ViewModelProvider(this, viewModelFactory).get(FoodDetailsActivityViewModel::class.java)
        binding.content.viewModel = viewModel
        binding.setLifecycleOwner { lifecycle }

        // setUp intent
        val intent = intent
        val hint = intent.getSerializableExtra(FoodSearchActivity.KEY_FOOD_DATA) as Hint
        viewModel.setData(hint)

        setUpMealDropDown()
        setUpDatePickerDialog()
        setUpQuantityTypeDropDownItemListener()
        setUpMealTypeDropDownItemListener()
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

        // setUp submit FAB click listener
        binding.submit.setOnClickListener {
            submitData()
        }
    }

    private fun submitData() {
        if (foodDetails.checkIfNull()) {
            viewModel.insertData(binding.content.foodName.text.toString())
            setResult(Activity.RESULT_OK)
            finish()
        } else Toast.makeText(this, "Fill all the Details", Toast.LENGTH_SHORT).show() //TODO
    }

    private fun setUpTextChangeListener() {
        binding.content.quantity.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                foodDetails.quantity = s.toString().toDouble()
                if (foodDetails.quantityType != null && foodDetails.quantity != null) viewModel.calculateNutrientData(
                    foodDetails
                )
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })
    }

    private fun setUpMealTypeDropDownItemListener() {
        binding.content.mealType.setOnItemClickListener { parent, view, position, id ->
            foodDetails.mealType = MealType.valueOf(parent.getItemAtPosition(position).toString())
        }
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