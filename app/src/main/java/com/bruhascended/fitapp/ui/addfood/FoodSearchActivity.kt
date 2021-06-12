package com.bruhascended.fitapp.ui.addfood


import android.app.Activity
import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil.setContentView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bruhascended.api.models.foodsv2.Hint
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.databinding.ActivityFoodSearchBinding
import com.bruhascended.fitapp.ui.capturefood.PredictionPresenter
import com.bruhascended.fitapp.util.setupToolbar
import kotlinx.coroutines.*

class FoodSearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFoodSearchBinding
    private lateinit var viewModel: FoodSearchActivityViewModel
    private lateinit var FoodsAdapter: FoodSearchAdapter

    companion object {
        const val KEY_FOOD_DATA = "FOOD_DATA"
    }

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

        //setUp LiveData Observer v2
        viewModel.food_hints_list.observe({ lifecycle }) {
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
                FoodsAdapter.submitList(emptyList())
                return false
            }

        })
    }

    private fun updateList(list: List<Hint?>) {
        FoodsAdapter.submitList(list)
        binding.progressBar.visibility = View.GONE
    }

    private fun setUpRecyclerview() {
        binding.recyclerviewFoods.apply {
            layoutManager = LinearLayoutManager(this@FoodSearchActivity)
            FoodsAdapter = FoodSearchAdapter { onFoodItemClicked(it) }
            adapter = FoodsAdapter
        }
    }

    private fun onFoodItemClicked(food_hint: Hint?) {
        val intent = Intent(this, AddFoodActivity::class.java)
        intent.putExtra(KEY_FOOD_DATA, food_hint)
        setResult(Activity.RESULT_OK, intent)
        finish()
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
        viewModel.getFoodsv2(query)
        binding.searchBar.clearFocus()
        binding.progressBar.isVisible = true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        }
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        setResult(Activity.RESULT_CANCELED)
        finish()
    }
}
