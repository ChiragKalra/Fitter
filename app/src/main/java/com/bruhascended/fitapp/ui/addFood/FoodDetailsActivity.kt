package com.bruhascended.fitapp.ui.addFood

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.util.Log
import android.view.MenuItem
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
import java.text.SimpleDateFormat
import java.util.*

class FoodDetailsActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener {
    private lateinit var binding: ActivityFoodDetailsBinding
    private lateinit var viewModel: SharedActivityViewModel
    private var foodDetails = FoodNutrientDetails()
    private var freshStart: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_food_details)
        setupToolbar(binding.toolbar, home = true)

        viewModel =
            ViewModelProvider(this).get(SharedActivityViewModel::class.java)
        binding.content.viewModel = viewModel
        binding.setLifecycleOwner { lifecycle }

        val itemIntent = intent
        val item =
            itemIntent.getSerializableExtra(FoodSearchActivity.KEY_FOOD_DATA) as MultiViewType?
        val foodEntry =
            itemIntent.getSerializableExtra(ActionDialogPresenter.KEY_FOOD_ENTRY) as FoodEntry?
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

        viewModel.typeArrayItems.observe({ lifecycle }) { it ->
            val amountArray = it.map { it.toString() }.toTypedArray()
            binding.content.amountDropdown.setAdapter(
                CustomArrayAdapter(
                    this,
                    R.layout.view_dropdown_mealtype,
                    amountArray
                )
            )
        }

        binding.submit.setOnClickListener {
            submitData()
        }


        viewModel.NutrientDetails.value.let {
            if (it != null) foodDetails = it
            else {
                if (item != null) {
                    foodDetails.quantity = 1.0
                    if (item.resId == 0) {
                        val content = item.content as Hint
                        foodDetails.quantityType =
                            QuantityType.valueOf(content.measures[0].label.toString())
                    } else {
                        val content = item.content as Food
                        val quantityTypeArr = content.weightInfo.keys.toList()
                        foodDetails.quantityType = quantityTypeArr[0]
                    }
                }
                if (foodEntry != null) {
                    setCopyToNow(foodEntry)
                }
                viewModel.calculateNutrientData(foodDetails)
            }
        } // TODO customise default values
    }

    override fun onResume() {
        super.onResume()
        freshStart = false
        foodDetails.let {
            binding.content.quantity.setText(it.quantity.toString())
            if (it.mealType != null) binding.content.mealType.setText(it.mealType.toString())
            else binding.content.mealType.setText("")
        }
        binding.content.amountDropdown.setText(foodDetails.quantityType.toString())
        binding.content.datePicker.setText(DateFormat.format("dd/MM/yyyy", viewModel.millis))
    }

    private fun setCopyToNow(foodEntry: FoodEntry) {
        if (viewModel.NutrientDetails.value == null) {
            viewModel.setDataFromDb(foodEntry.food)
            foodDetails.apply {
                quantityType = foodEntry.entry.quantityType
                quantity = foodEntry.entry.quantity
                mealType = foodEntry.entry.mealType
            }
        }
    }

    private fun submitData() {
        if (foodDetails.checkIfNull()) {
            viewModel.insertData(binding.content.foodName.text.toString())
            setResult(Activity.RESULT_OK)
            finish()
        } else Toast.makeText(this, "Fill all the Details", Toast.LENGTH_SHORT).show()
        //TODO show a alert dialog
    }

    private fun setUpTextChangeListener() {
        binding.content.quantity.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(!freshStart){
                    if (!s.isNullOrEmpty() && s.toString() != ".") {
                        foodDetails.quantity = s.toString().toDouble()
                        viewModel.calculateNutrientData(foodDetails)
                    } else {
                        foodDetails.quantity = 1.0 // TODO default quantity
                        viewModel.calculateNutrientData(foodDetails)
                    }
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            onBackPressed()
            true
        }
        else -> false
    }
}