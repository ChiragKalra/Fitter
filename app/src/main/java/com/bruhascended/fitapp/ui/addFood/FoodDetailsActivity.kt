package com.bruhascended.fitapp.ui.addFood

import android.app.Activity
import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.bruhascended.api.models.foodsv2.Hint
import com.bruhascended.db.food.entities.Food
import com.bruhascended.db.food.entities.FoodEntry
import com.bruhascended.db.food.types.MealType
import com.bruhascended.db.food.types.QuantityType
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.databinding.ActivityFoodDetailsBinding
import com.bruhascended.fitapp.ui.foodjournal.ActionDialogPresenter
import com.bruhascended.fitapp.util.CustomArrayAdapter
import com.bruhascended.fitapp.util.FoodNutrientDetails
import com.bruhascended.fitapp.util.MultiViewType
import com.bruhascended.fitapp.util.setupToolbar
import java.util.*

class FoodDetailsActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener {
    private lateinit var binding: ActivityFoodDetailsBinding
    private lateinit var viewModel: SharedActivityViewModel
    private var foodDetails = FoodNutrientDetails()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_food_details)
        setupToolbar(binding.toolbar, home = true)

        // viewModel
        viewModel =
            ViewModelProvider(this).get(SharedActivityViewModel::class.java)
        binding.content.viewModel = viewModel
        binding.setLifecycleOwner { lifecycle }

        // setUp intent
        val itemIntent = intent
        val item =
            itemIntent.getSerializableExtra(FoodSearchActivity.KEY_FOOD_DATA) as MultiViewType?
        val foodEntry =
            itemIntent.getSerializableExtra(ActionDialogPresenter.KEY_FOOD_ENTRY) as FoodEntry?
        if (foodEntry != null) setCopyToNow(foodEntry)
        if (item != null) {
            if (item.resId == 0)
                viewModel.setData(item.content as Hint)
            else viewModel.setDataFromDb(item.content as Food)
        }

        setUpMealDropDown()
        setUpDatePickerDialog()
        setUpQuantityTypeDropDownItemListener()
        setUpMealTypeDropDownItemListener()
        setUpTextChangeListener()

        // setUp live data observer for quantity type drop down
        viewModel.typeArrayItems.observe({ lifecycle }) { it ->
            val amountArray = it.map { it.toString() }.toTypedArray()
            binding.content.amountDropdown.setAdapter(
                CustomArrayAdapter(
                    this,
                    R.layout.view_dropdown_mealtype,
                    amountArray
                )
            )
            // TODO setUp default quantity type
            if (foodEntry == null) {
                foodDetails.quantityType = QuantityType.valueOf(amountArray[0])
                viewModel.calculateNutrientData(foodDetails)
                binding.content.amountDropdown.setText(amountArray[0])
            }
        }

        // setUp submit FAB click listener
        binding.submit.setOnClickListener {
            submitData()
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.NutrientDetails.let {
            if (it.value != null) {
                foodDetails = it.value!!
            }
        }
        if (foodDetails.quantity == null) {
            foodDetails.quantity = 1.0 // TODO default quantity value
            viewModel.calculateNutrientData(foodDetails)
        }
    }

    private fun setCopyToNow(foodEntry: FoodEntry) {
        if(viewModel.NutrientDetails.value == null){
            viewModel.setDataFromDb(foodEntry.food)
            foodDetails.apply {
                quantityType = foodEntry.entry.quantityType
                quantity = foodEntry.entry.quantity
                mealType = foodEntry.entry.mealType
            }
            viewModel.calculateNutrientData(foodDetails)
            binding.content.apply {
                quantity.setText(foodEntry.entry.quantity.toString())
                amountDropdown.setText(foodEntry.entry.quantityType.toString())
                mealType.setText(foodEntry.entry.mealType.toString())
            }
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
                if (!s.isNullOrEmpty() && s.toString() != ".") {
                    foodDetails.quantity = s.toString().toDouble()
                    viewModel.calculateNutrientData(foodDetails)
                } else {
                    foodDetails.quantity = 1.0 // TODO default value
                    viewModel.calculateNutrientData(foodDetails)
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })
    }

    private fun setUpMealTypeDropDownItemListener() {
        binding.content.mealType.setOnItemClickListener { parent, view, position, id ->
            foodDetails.mealType = MealType.valueOf(parent.getItemAtPosition(position).toString())
            viewModel.calculateNutrientData(foodDetails)
        }
    }

    private fun setUpQuantityTypeDropDownItemListener() {
        binding.content.amountDropdown.setOnItemClickListener { parent, view, position, id ->
            foodDetails.quantityType =
                QuantityType.valueOf(parent.getItemAtPosition(position).toString())
            viewModel.calculateNutrientData(foodDetails)
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
        val cal = Calendar.getInstance()
        cal.set(year, month, dayOfMonth)
        viewModel.millis = cal.timeInMillis
    }
}