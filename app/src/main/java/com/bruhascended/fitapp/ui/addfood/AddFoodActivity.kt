package com.bruhascended.fitapp.ui.addfood

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.widget.DatePicker
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.bruhascended.api.models.foodsv2.Hint
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.databinding.ActivityAddFoodBinding
import com.bruhascended.fitapp.ui.capturefood.PredictionPresenter
import com.bruhascended.fitapp.util.CustomArrayAdapter
import com.bruhascended.fitapp.util.setupToolbar
import java.util.*


class AddFoodActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener {

    private val viewModel: AddFoodActivityViewModel by viewModels()
    private var quantity_type: String = ""
    private var drop_down_item_selected = false
    private var searchSuccess = false
    private lateinit var resultContract: ActivityResultLauncher<Intent>
    private lateinit var binding: ActivityAddFoodBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_food)
        binding.content.viewModel = viewModel
        binding.lifecycleOwner = this
        setupToolbar(binding.toolbar, home = true)

        // setUp dropDown click listener
        binding.content.amountDropdown.setOnItemClickListener { parent, view, position, id ->
            val view_text = parent.getItemAtPosition(position).toString()
            drop_down_item_selected = true
            if (searchSuccess) {
                quantity_type = view_text
                setEnergyData(binding.content.quantity.text)
            } else {
                binding.content.perEnergyTextview.hint = "kcal/$view_text"
            }
        }

        //setUp EditText text change listener
        binding.content.quantity.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (searchSuccess) setEnergyData(s)
                else setEnergyDataOffline()
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
        //only for offline mode
        binding.content.perEnergyEdittextview.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                setEnergyDataOffline()
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })

        //setUp resultContract
        setUpResultContract()

        //captureFood intent
        val intent = intent
        val query = intent.getStringExtra(PredictionPresenter.KEY_FOOD_LABEL)
        if (query != null) setUpCapturedFoodSearch(query)

        //setUp Amount drop down
        val amounts_adapter =
            CustomArrayAdapter(this, R.layout.meal_type_drop_down, viewModel.default_types)
        binding.content.amountDropdown.setAdapter(amounts_adapter)

        //setUp mealType drop down
        val meal_dropdown = resources.getStringArray(R.array.Meal_type)
        val meal_dropdown_adapter =
            CustomArrayAdapter(this, R.layout.meal_type_drop_down, meal_dropdown)
        binding.content.mealType.setAdapter(meal_dropdown_adapter)

        // live data observer for drop down
        viewModel.type_arr.observe({ lifecycle }) {
            val amounts_adapter =
                CustomArrayAdapter(this, R.layout.meal_type_drop_down, it.toTypedArray())
            binding.content.amountDropdown.setAdapter(amounts_adapter)
        }

        //setUp date picker
        setUpDatePickerDialog()
    }

    private fun setEnergyData(s: CharSequence?) {
        if (s?.isNotEmpty() == true && drop_down_item_selected) {
            viewModel.calculateEnergy(s.toString(), quantity_type)
        } else binding.content.energy.setText("")
    }

    private fun setEnergyDataOffline() {
        val quantity = binding.content.quantity.text.toString()
        val kcal_per_type = binding.content.perEnergyEdittextview.text.toString()
        if (quantity != "." && kcal_per_type != ".") {
            if (quantity.isNotEmpty() && kcal_per_type.isNotEmpty()) {
                viewModel.calculateEnergyOffline(quantity, kcal_per_type)
            }
        } else binding.content.energy.setText("")
    }

    private fun setUpCapturedFoodSearch(query: String) {
        val intent: Intent = Intent(this, FoodSearchActivity::class.java)
        intent.putExtra(PredictionPresenter.KEY_FOOD_LABEL, query)
        resultContract.launch(intent)
    }

    private fun setUpResultContract() {
        resultContract =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult? ->
                if (result?.resultCode == RESULT_OK) {
                    binding.content.apply {
                        quantity.setText("")
                        perEnergyEdittextview.setText("")
                        perEnergyTextview.hint = "kcal/g"
                        quantity.requestFocus()
                        amountDropdown.showDropDown()
                    }
                    searchSuccess = true
                    val hint =
                        result.data?.getSerializableExtra(FoodSearchActivity.KEY_FOOD_DATA) as Hint?
                    viewModel.setData(hint)
                    drop_down_item_selected = true
                    quantity_type = "Gram(1g)"
                    binding.content.perEnergyEdittextview.apply {
                        isFocusable = false
                        clearFocus()
                    }
                } else searchSuccess = false
            }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.add_newfood_items, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val intent: Intent = Intent(this, FoodSearchActivity::class.java)
        when (item.itemId) {
            R.id.search -> {
                drop_down_item_selected = false
                resultContract.launch(intent)
            }
            android.R.id.home -> onBackPressed()
        }
        return true
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
