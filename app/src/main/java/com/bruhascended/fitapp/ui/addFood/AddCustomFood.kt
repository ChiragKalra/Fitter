package com.bruhascended.fitapp.ui.addFood

import android.app.Activity
import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.DatePicker
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.bruhascended.db.food.types.MealType
import com.bruhascended.db.food.types.QuantityType
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.databinding.ActivityAddCustomFoodBinding
import com.bruhascended.fitapp.util.CustomArrayAdapter
import com.bruhascended.fitapp.util.FoodNutrientDetails
import com.bruhascended.fitapp.util.setupToolbar
import java.util.*

class AddCustomFood : AppCompatActivity(), DatePickerDialog.OnDateSetListener {
    private lateinit var binding: ActivityAddCustomFoodBinding
    private lateinit var viewModel: SharedActivityViewModel
    private var foodDetails = FoodNutrientDetails()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_custom_food)
        setupToolbar(binding.toolbar, home = true)

        // viewModel
        val viewModelFactory = FoodDetailsViewModelFactory(application)
        viewModel =
            ViewModelProvider(this, viewModelFactory).get(SharedActivityViewModel::class.java)
        binding.content.viewModel = viewModel
        binding.setLifecycleOwner { lifecycle }

        setUpMealDropDown()
        setUpQuantityTypeDropDown()
        setUpQuantityTypeDropDownItemListener()
        setUpMealTypeDropDownItemListener()
        setUpDatePickerDialog()
        setUpTextChangedListeners()

        // setUp submit click Listener
        binding.submit.setOnClickListener {
            submitData()
        }
    }

    private fun submitData() {
        val foodName = binding.content.foodName.text.toString()
        if (foodDetails.checkIfNull() && foodName.isNotEmpty()) {
            viewModel.insertCustomData(foodName, foodDetails)
            setResult(Activity.RESULT_OK)
            finish()
        } else Toast.makeText(this, "Fill all the Details", Toast.LENGTH_SHORT).show() // TODO
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
        }
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
                MealType.values()[it].toString()
            }
        ))
    }

    private fun setUpTextChangedListeners() {
        binding.content.quantity.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrEmpty()) {
                    foodDetails.quantity = s.toString().toDouble()
                } else foodDetails.quantity = null
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        binding.content.textviewEnergy.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrEmpty()) {
                    foodDetails.Energy = s.toString().toDouble()
                } else foodDetails.Energy = null
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
        binding.content.textviewProtein.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrEmpty()) {
                    foodDetails.Protein = s.toString().toDouble()
                } else foodDetails.Protein = null
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
        binding.content.textviewCarbs.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrEmpty()) {
                    foodDetails.Carbs = s.toString().toDouble()
                } else foodDetails.Carbs = null
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
        binding.content.textviewFat.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrEmpty()) {
                    foodDetails.Fat = s.toString().toDouble()
                } else foodDetails.Fat = null
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
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