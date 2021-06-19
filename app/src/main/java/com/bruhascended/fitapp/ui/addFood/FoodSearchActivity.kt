package com.bruhascended.fitapp.ui.addFood

import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.bruhascended.api.models.foodsv2.Hint
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.databinding.ActivityFoodSearchBinding
import com.bruhascended.fitapp.util.setupToolbar

class FoodSearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFoodSearchBinding
    private lateinit var FoodsAdapter: FoodSearchAdapter
    private val viewModel: FoodSearchActivityViewModel by viewModels()

    companion object {
        const val KEY_FOOD_DATA = "FOOD_DATA"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //binding = DataBindingUtil.setContentView(this, R.layout.activity_food_search)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_food_search)
        setupToolbar(binding.toolbar, home = true)

        //setUp recyclerview
        setUpRecyclerview()

        //setUp search
        setUpSearch()

        //setUp LiveData Observer for recyclerView list items
        viewModel.food_hints_list.observe({ lifecycle }) {
            updateList(it)
        }

        //setUp errorHandler observer
        viewModel.error.observe({ lifecycle }) {
            handleError()
        }
    }

    private fun setUpRecyclerview() {
        binding.recyclerviewFoods.apply {
            layoutManager = LinearLayoutManager(this@FoodSearchActivity)
            FoodsAdapter = FoodSearchAdapter { onFoodItemClicked(it) }
            adapter = FoodsAdapter
        }
    }

    private fun onFoodItemClicked(food_hint: Hint) {
        val intent = Intent(this, FoodDetailsActivity::class.java)
        intent.putExtra(KEY_FOOD_DATA, food_hint)
        startActivity(intent)
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

    private fun onSearchCustomise(query: String) {
        viewModel.getFoodsv2(query)
        binding.searchBar.clearFocus()
        binding.progressBar.isVisible = true
    }

    private fun updateList(list: List<Hint?>) {
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
}