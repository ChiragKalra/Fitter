package com.bruhascended.fitapp.ui.addFood

import android.app.Activity
import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.DatePicker
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.bruhascended.api.models.foodsv2.Hint
import com.bruhascended.db.food.entities.Entry
import com.bruhascended.db.food.entities.Food
import com.bruhascended.db.food.entities.FoodEntry
import com.bruhascended.db.food.types.MealType
import com.bruhascended.db.food.types.QuantityType
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.databinding.ActivityFoodDetailsBinding
import com.bruhascended.fitapp.ui.foodjournal.ActionDialogPresenter
import com.bruhascended.fitapp.util.*
import java.util.*

const val DEFAULT_QUANTITY = 1.0

class FoodDetailsActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener {
    private lateinit var binding: ActivityFoodDetailsBinding
    private lateinit var viewModel: SharedActivityViewModel
    private val viewsLIst = mutableListOf<TextView>()
    private var millis: Long = 0
    private var foodQuantity = DEFAULT_QUANTITY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_food_details)
        setupToolbar(binding.toolbar, home = true)

        viewModel =
            ViewModelProvider(this).get(SharedActivityViewModel::class.java)
        binding.content.viewModel = viewModel
        binding.setLifecycleOwner { lifecycle }

        // all the intents are setUp here
        val itemIntent = intent
        val item =
            itemIntent.getSerializableExtra(FoodSearchActivity.KEY_FOOD_DATA) as MultiViewType?
        val foodEntry =
            itemIntent.getSerializableExtra(ActionDialogPresenter.KEY_FOOD_ENTRY) as FoodEntry?
        if (item != null) {
            if (item.resId == 0)
                viewModel.setData(item.content as Hint)
            else viewModel.setDataFromDb(item.content as Food)
        } else viewModel.setDataFromDb(foodEntry!!.food)

        populateViewsList()
        setUpMealDropDown()
        setUpDatePickerDialog()
        setUpQuantityTextChangeListeners()
        setUpAmountDropDownListener()

        /* Nutritional values is only calculated after quantityTypeDropDown gets populated,
        thus the first point where viewModel.CalculateNutrientData is called */
        viewModel.QuantityTYpeItems.observe({ lifecycle }) { it ->
            val amountArray = it.map { it.toString() }.toTypedArray()
            binding.content.amountDropdown.setAdapter(
                CustomArrayAdapter(this, R.layout.item_dropdown, amountArray)
            )
            /* we are only saving millis as all other views data is retained by default,
            although the date text too retains but we cannot convert it to timeInMillis
            so millis is synced so that it matches the date picker text */
            if (savedInstanceState?.get(TIME) == null) {
                if (foodEntry == null)
                    setUpDefaultValues(amountArray)
                else setCopyToNow(foodEntry)
            } else {
                millis = savedInstanceState.getLong(TIME)
                // sync foodQuantity with quantity text view
                viewsLIst[1].text.toString().let {
                    if (it.isNotEmpty()) foodQuantity = it.toDouble()
                }
                /* here we are reducing unnecessary calls to viewModel.calculateNutrientData
                by checking if our viewModel got destroyed or not */
                if (viewModel.NutrientDetails.value == null) {
                    viewModel.calculateNutrientData(this,
                        foodQuantity,
                        QuantityType.valueOf(viewsLIst[2].text.toString())
                    )
                }
            }
        }
    }

    private fun setCopyToNow(foodEntry: FoodEntry) {
        foodEntry.let {
            viewsLIst[0].text = it.food.foodName
            viewsLIst[1].text = it.entry.quantity.toString()
            viewsLIst[2].text = it.entry.quantityType.toString()
            foodQuantity = it.entry.quantity
        }
        millis = Calendar.getInstance().apply {
            set(Calendar.MILLISECOND, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.HOUR_OF_DAY, 0)
        }.timeInMillis
        binding.content.apply {
            datePicker.setText(DateTimePresenter(this@FoodDetailsActivity, millis).condensedDate)
            mealType.setText(MealType.getCurrentMealType().getString(this@FoodDetailsActivity))
        }
        viewModel.calculateNutrientData(this,
            foodQuantity, QuantityType.valueOf(viewsLIst[2].text.toString())
        )
    }

    private fun populateViewsList() {
        viewsLIst.apply {
            add(binding.content.foodName)       // 0
            add(binding.content.quantity)       // 1
            add(binding.content.amountDropdown) // 2
            add(binding.content.mealType)       // 3
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(TIME, millis)
    }

    private fun setUpDefaultValues(amountArray: Array<String>) {
        millis = Calendar.getInstance().apply {
            set(Calendar.MILLISECOND, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.HOUR_OF_DAY, 0)
        }.timeInMillis
        binding.content.apply {
            amountDropdown.setText(amountArray[0])
            datePicker.setText(DateTimePresenter(this@FoodDetailsActivity, millis).condensedDate)
            mealType.setText(MealType.getCurrentMealType().getString(this@FoodDetailsActivity))
        }
        viewModel.calculateNutrientData(this,
            foodQuantity,
            QuantityType.valueOf(viewsLIst[2].text.toString())
        )
    }

    /* This function helps insert data to db and values cannot be null
    as we always fill it up with default values */
    fun submitData(view: View) {
        val food = Food(
            viewModel.foodName.value!!,
            viewModel.perEnergy!!,
            viewModel.weightInfo_map,
            viewModel.nutrientInfo_map
        )
        val calories =
            viewModel.weightInfo_map[QuantityType.valueOf(viewsLIst[2].text.toString())]!! * foodQuantity * viewModel.perEnergy!!
        val entry = Entry(
            calories.toInt(),
            foodQuantity,
            QuantityType.valueOf(viewsLIst[2].text.toString()),
            MealType.getEnum(viewsLIst[3].text.toString(), this),
            millis
        )
        viewModel.insertData(food, entry)
        setResult(Activity.RESULT_OK)
        finish()
    }

    /* This function is not called if the amountDropDown has not yet been
    inflated */
    private fun setUpQuantityTextChangeListeners() {
        binding.content.quantity.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (viewsLIst[2].text.toString().isNotEmpty()) {
                    if (!s.isNullOrEmpty() && s.toString() != "."
                    ) {
                        foodQuantity = viewsLIst[1].text.toString().toDouble()
                        viewModel.calculateNutrientData(this@FoodDetailsActivity,
                            foodQuantity,
                            QuantityType.valueOf(viewsLIst[2].text.toString())
                        )
                    } else {
                        foodQuantity = DEFAULT_QUANTITY
                        viewModel.calculateNutrientData(this@FoodDetailsActivity,
                            foodQuantity,
                            QuantityType.valueOf(viewsLIst[2].text.toString())
                        )
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })
    }

    private fun setUpAmountDropDownListener() {
        binding.content.amountDropdown.setOnItemClickListener { parent, view, position, id ->
            viewsLIst[1].text.toString().let {
                if (it.isNotEmpty()) {
                    foodQuantity = it.toDouble()
                    viewModel.calculateNutrientData(this,
                        foodQuantity,
                        QuantityType.valueOf(viewsLIst[2].text.toString())
                    )
                }
            }
        }
    }

    private fun setUpMealDropDown() {
        binding.content.mealType.setAdapter(CustomArrayAdapter(this,
            R.layout.item_dropdown,
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