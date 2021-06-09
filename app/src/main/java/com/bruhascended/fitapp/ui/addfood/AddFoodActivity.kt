package com.bruhascended.fitapp.ui.addfood

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.bruhascended.api.models.foods.Food
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.databinding.ActivityAddFoodBinding
import com.bruhascended.fitapp.ui.capturefood.PredictionPresenter
import com.bruhascended.fitapp.util.CustomArrayAdapter
import com.bruhascended.fitapp.util.setupToolbar
import java.util.*


class AddFoodActivity : AppCompatActivity(), TimePickerDialog.OnTimeSetListener,
    DatePickerDialog.OnDateSetListener {

    private val viewModel: AddFoodActivityViewModel by viewModels()
    private lateinit var resultContract: ActivityResultLauncher<Intent>
    private lateinit var binding: ActivityAddFoodBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_food)
        binding.content.viewModel = viewModel
        binding.lifecycleOwner = this
        setupToolbar(binding.toolbar, home = true)

        //setUp resultContract
        setUpResultContract()

        //captureFood intent
        val intent = intent
        val query = intent.getStringExtra(PredictionPresenter.KEY_FOOD_LABEL)
        if (query != null) setUpCapturedFoodSearch(query)

        //setUp Amount drop down
        val amounts = resources.getStringArray(R.array.Amount)
        val amounts_adapter = CustomArrayAdapter(this, R.layout.amount_drop_down, amounts)
        binding.content.amountDropdown.setAdapter(amounts_adapter)

        //setUp Calorie drop down
        val calories_amounts = resources.getStringArray(R.array.Calorie_Amount)
        val calories_adapter = CustomArrayAdapter(this, R.layout.amount_drop_down, calories_amounts)
        binding.content.calorieDropdown.setAdapter(calories_adapter)

        //setUp date and time picker
        setUpTimePickerDialog()
        setUpDatePickerDialog()


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
                    val food =
                        result.data?.getSerializableExtra(FoodSearchActivity.KEY_FOOD_DATA) as Food
                    viewModel.setData(food)
                }
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
                resultContract.launch(intent)
            }
            android.R.id.home -> onBackPressed()
        }
        return true
    }

    fun setUpTimePickerDialog() {
        val cal = Calendar.getInstance()
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val min = cal.get(Calendar.MINUTE)
        binding.content.timePicker.setOnClickListener {
            TimePickerDialog(this, this, hour, min, false).show()
        }
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        binding.content.timePicker.setText(String.format("%02d:%02d", hourOfDay, minute))
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
        binding.content.datePicker.setText(String.format("%02d/%02d/%d", dayOfMonth, month, year))
    }

}
