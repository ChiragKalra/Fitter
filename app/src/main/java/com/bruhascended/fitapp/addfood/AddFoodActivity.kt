package com.bruhascended.fitapp.addfood

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.databinding.ActivityAddFoodBinding
import com.bruhascended.fitapp.util.setupToolbar

class AddFoodActivity : AppCompatActivity() {


    private lateinit var binding: ActivityAddFoodBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_food)
        setupToolbar(binding.toolbar, home = true)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.add_newfood_items, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val intent:Intent = Intent(this,FoodSearchActivity::class.java)
        when(item.itemId){
            R.id.search -> startActivity(intent)
            android.R.id.home -> onBackPressed()
        }
        return true
    }

}
