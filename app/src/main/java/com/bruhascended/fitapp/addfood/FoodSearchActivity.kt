package com.bruhascended.fitapp.addfood

import android.app.SearchManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil.setContentView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.databinding.ActivityFoodSearchBinding
import com.bruhascended.fitapp.util.setupToolbar
import com.example.api.models.foods.Food
import kotlinx.coroutines.*

class FoodSearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFoodSearchBinding
    private lateinit var viewModel: FoodSearchActivityViewModel
    private lateinit var FoodsAdapter: FoodSearchAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = setContentView(this, R.layout.activity_food_search)
        setupToolbar(binding.foodSearchToolbar, home = true)

        //setUp viewmodel
        viewModel = ViewModelProvider(this).get(FoodSearchActivityViewModel::class.java)

        //search bar customisations
        searchBarCustomize()

        //setUp recyclerview
        setUpRecyclerview()

        //setUp LiveData Observer
        viewModel.foods_list.observe({ lifecycle }) {
            updateList(it)
        }
    }

    private fun updateList(list: List<Food>) {
        FoodsAdapter.submitList(list)
    }

    private fun setUpRecyclerview() {
        binding.recyclerviewFoods.apply {
            layoutManager = LinearLayoutManager(this@FoodSearchActivity)
            FoodsAdapter = FoodSearchAdapter()
            adapter = FoodsAdapter
        }
    }

    private fun searchBarCustomize() {
        val searchManager = getSystemService(SEARCH_SERVICE) as SearchManager
        binding.searchBar.apply {
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
            setIconifiedByDefault(false)
            isFocusable = true
            isIconified = false
            requestFocusFromTouch()
        }
    }
}
