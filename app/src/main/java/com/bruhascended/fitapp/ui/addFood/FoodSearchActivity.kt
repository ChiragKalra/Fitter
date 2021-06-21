package com.bruhascended.fitapp.ui.addFood

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bruhascended.api.models.foodsv2.Hint
import com.bruhascended.db.food.entities.Food
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.databinding.ActivityFoodSearchBinding
import com.bruhascended.fitapp.util.MultiViewType
import com.bruhascended.fitapp.util.setupToolbar

class FoodSearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFoodSearchBinding
    private lateinit var FoodsAdapter: FoodSearchAdapter
    private lateinit var viewModel: FoodSearchActivityViewModel
    private lateinit var resultContracts: ActivityResultLauncher<Intent>

    companion object {
        const val KEY_FOOD_DATA = "FOOD_DATA"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_food_search)
        setupToolbar(binding.toolbar, home = true)

        // viewModel
        val viewModelFactory = FoodSearchViewModelFactory(application)
        viewModel =
            ViewModelProvider(this, viewModelFactory).get(FoodSearchActivityViewModel::class.java)
        binding.setLifecycleOwner { lifecycle }

        setUpResultContract()
        setUpRecyclerview()
        setUpSearch()

        //setUp LiveData Observer for recyclerView list items
        viewModel.food_hints_list.observe({ lifecycle }) {
            val food_hints_list = mutableListOf<MultiViewType>()
            for (hint in it)
                food_hints_list.add(MultiViewType(0, hint))
            updateList(food_hints_list)
        }
        viewModel.food_history_list.observe({ lifecycle }) {
            val food_history_list = mutableListOf<MultiViewType>()
            for (food in it) {
                food_history_list.add(MultiViewType(1, food))
            }
            updateList(food_history_list)
        }

        //setUp errorHandler observer
        viewModel.error.observe({ lifecycle }) {
            handleError()
        }
    }

    private fun setUpResultContract() {
        resultContracts =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == Activity.RESULT_OK) finish()
            }
    }

    private fun setUpRecyclerview() {
        binding.recyclerviewFoods.apply {
            layoutManager = LinearLayoutManager(this@FoodSearchActivity)
            FoodsAdapter = FoodSearchAdapter {
                onFoodItemClicked(it)
            }
            adapter = FoodsAdapter
        }
    }

    private fun onFoodItemClicked(item: MultiViewType) {
        val intent = Intent(this, FoodDetailsActivity::class.java)
        intent.putExtra(KEY_FOOD_DATA, item)
        resultContracts.launch(intent)
    }

    private fun onHistoryFoodItemClicked(food: Food) {

    }

    private fun setUpSearch() {
        binding.searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String?): Boolean {
                if (!newText.isNullOrEmpty()) viewModel.searchConsumedFood("%$newText%")
                else viewModel.searchConsumedFood("%%")
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                onSearchCustomise(query)
                return false
            }

        })
    }

    private fun onSearchCustomise(query: String) {
        viewModel.getFoodsv2(query)
        binding.searchBar.clearFocus()
        binding.progressBar.isVisible = true
    }

    private fun updateList(list: MutableList<MultiViewType>) {
        FoodsAdapter.submitList(list)
        binding.progressBar.visibility = View.GONE
    }

    private fun handleError() {
        binding.progressBar.visibility = View.GONE
        Toast.makeText(this, viewModel.getError(), Toast.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.add_newfoodv2_items, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.manually_add -> {
                val intent = Intent(this, AddCustomFood::class.java)
                resultContracts.launch(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}