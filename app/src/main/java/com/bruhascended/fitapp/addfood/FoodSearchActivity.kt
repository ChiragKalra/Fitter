package com.bruhascended.fitapp.addfood

import android.app.SearchManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.databinding.DataBindingUtil.*
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.R.layout.activity_food_search
import com.bruhascended.fitapp.databinding.ActivityFoodSearchBinding
import com.bruhascended.fitapp.util.setupToolbar

class FoodSearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFoodSearchBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = setContentView(this, R.layout.activity_food_search)
        setupToolbar(binding.foodSearchToolbar, home = true)
        //search bar customisations
        searchBarCustomize()
    }

    private fun searchBarCustomize() {
        val searchManager = getSystemService(SEARCH_SERVICE) as SearchManager
        binding.searchBar.apply {
            setSearchableInfo(searchManager.getSearchableInfo(componentName));
            setIconifiedByDefault(false);
            isFocusable = true;
            isIconified = false;
            requestFocusFromTouch();
        }
    }
}