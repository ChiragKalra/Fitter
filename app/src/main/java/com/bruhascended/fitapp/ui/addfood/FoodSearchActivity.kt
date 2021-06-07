package com.bruhascended.fitapp.ui.addfood

import android.app.SearchManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil.setContentView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.databinding.ActivityFoodSearchBinding
import com.bruhascended.fitapp.util.setupToolbar
import com.bruhascended.api.models.foods.Food
import com.bruhascended.fitapp.ui.capturefood.PredictionPresenter
import kotlinx.coroutines.*

class FoodSearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFoodSearchBinding
    private lateinit var viewModel: FoodSearchActivityViewModel
    private lateinit var FoodsAdapter: FoodSearchAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = setContentView(this, R.layout.activity_food_search)
        setupToolbar(binding.foodSearchToolbar, home = true)

        //setUp viewModel
        viewModel = ViewModelProvider(this).get(FoodSearchActivityViewModel::class.java)

        //search bar customisations
        searchBarCustomize()

        //captureFood intent
        val intent = intent
        val query = intent.getStringExtra(PredictionPresenter.KEY_FOOD_LABEL)
        if (query != null) setUpCapturedFoodSearch(query)

        //setUp recyclerview
        setUpRecyclerview()

        //setUp search
        setUpSearch()

        //setUp LiveData Observer
        viewModel.foods_list.observe({ lifecycle }) {
            updateList(it)
        }

        //setUp errorHandler observer
        viewModel.error.observe({ lifecycle }) {
            handleError()
        }
    }

    private fun setUpCapturedFoodSearch(query: String) {
        binding.searchBar.setQuery(query, false)
        onSearchCustomise(query)
    }

    private fun handleError() {
        binding.progressBar.visibility = View.GONE
        Toast.makeText(this, viewModel.getError(), Toast.LENGTH_SHORT).show()
    }

    private fun setUpSearch() {
        binding.searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) updateList(emptyList())
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                onSearchCustomise(query)
                return false
            }

        })
    }

    private fun updateList(list: List<Food>) {
        FoodsAdapter.submitList(list)
        binding.progressBar.visibility = View.GONE
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
            isIconified = false
            requestFocusFromTouch()
        }
    }

    private fun onSearchCustomise(query: String) {
        viewModel.getFoods(query)
        binding.searchBar.clearFocus()
        binding.progressBar.isVisible = true
    }
}
