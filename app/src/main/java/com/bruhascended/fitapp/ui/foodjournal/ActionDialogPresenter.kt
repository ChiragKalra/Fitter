package com.bruhascended.fitapp.ui.foodjournal

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AlertDialog
import com.bruhascended.db.food.entities.FoodEntry
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.ui.addFood.FoodDetailsActivity

class ActionDialogPresenter (
    private val mContext: Context,
    private val viewModel: FoodJournalViewModel,
    private val foodEntry: FoodEntry,
) {

    companion object {
        const val KEY_FOOD_ENTRY = "KEY_FOOD_ENTRY"
        const val ACTION_COPY_FOOD_ENTRY = "ACTION_COPY_FOOD_ENTRY"
        const val ACTION_EDIT_FOOD_ENTRY = "ACTION_EDIT_FOOD_ENTRY"
    }

    private val actions = mContext.resources.getStringArray(R.array.food_entry_actions)

    private val dialog = AlertDialog.Builder(mContext).setItems(actions) { d, c ->
        when (c) {
            0, 1 -> mContext.startActivity(
                Intent (mContext, FoodDetailsActivity::class.java).apply {
                    action = if (c == 0) ACTION_EDIT_FOOD_ENTRY else ACTION_COPY_FOOD_ENTRY
                    putExtra(KEY_FOOD_ENTRY, foodEntry)
                }
            )
            2 -> viewModel.deleteEntry(foodEntry)
        }
        d.dismiss()
    }.create()

    fun show() = dialog.show()
}