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
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.databinding.ActivityFoodSearchBinding
import com.bruhascended.fitapp.ui.addFood.adapters.FoodSearchAdapter
import com.bruhascended.fitapp.ui.addFood.entities.MultiViewType
import com.bruhascended.fitapp.ui.capturefood.PredictionPresenter
import com.bruhascended.fitapp.util.setupToolbar

class FoodSearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFoodSearchBinding
    private lateinit var FoodsAdapter: FoodSearchAdapter
    private lateinit var viewModel: FoodSearchActivityViewModel
    private lateinit var resultContracts: ActivityResultLauncher<Intent>
    private var loadCountList = mutableListOf<MultiViewType>()

    companion object {
        const val KEY_FOOD_DATA = "FOOD_DATA"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_food_search)
        setupToolbar(binding.toolbar, home = true)

        viewModel =
            ViewModelProvider(this).get(FoodSearchActivityViewModel::class.java)
        binding.viewModel = viewModel
        binding.setLifecycleOwner { lifecycle }

        setUpResultContract()
        setUpRecyclerview()
        setUpSearch()

        val intent = intent
        intent.getStringExtra(PredictionPresenter.KEY_FOOD_LABEL)?.let {
            binding.searchBar.setQuery(it, false)
            onSearchCustomise(it)
        }

        viewModel.apply {
            food_hints_list.observe({ lifecycle }) {
                val food_hints_list = mutableListOf<MultiViewType>()
                for (hint in it)
                    food_hints_list.add(MultiViewType(0, hint))
                updateList(food_hints_list)
                binding.searchingLayout.visibility = View.GONE
            }
            loadHistory.observe({ lifecycle }) {
                for (food in it) loadCountList.add(MultiViewType(1, food))
                updateList(loadCountList)
            }
            error.observe({ lifecycle }) {
                handleError()
            }
        }

        binding.searchLayoutPlaceholder.setOnClickListener {
            onSearchCustomise(binding.searchBar.query.toString())
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
            layoutManager = LinearLayoutManager(context)
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

    private fun setUpSearch() {
        binding.searchBar.requestFocus()
        binding.searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String?): Boolean {
                if (!newText.isNullOrEmpty()) {
                    viewModel.searchConsumedFood("%$newText%").observe({ lifecycle }) {
                        val food_history_list = mutableListOf<MultiViewType>()
                        for (food in it) {
                            food_history_list.add(MultiViewType(1, food))
                        }
                        updateList(food_history_list)
                    }
                    binding.searchLayoutPlaceholder.visibility = View.VISIBLE
                } else {
                    updateList(loadCountList)
                    binding.searchLayoutPlaceholder.visibility = View.GONE
                }
                viewModel.searchtext.postValue(newText)
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
        binding.searchLayoutPlaceholder.visibility = View.GONE
        binding.searchingLayout.visibility = View.VISIBLE
    }

    private fun updateList(list: MutableList<MultiViewType>) {
        FoodsAdapter.submitList(list)
    }

    private fun handleError() {
        binding.searchingLayout.visibility = View.GONE
        Toast.makeText(this, viewModel.getError(), Toast.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
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