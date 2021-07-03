package com.bruhascended.fitapp.ui.activityjournal

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AlertDialog
import com.bruhascended.db.activity.entities.ActivityEntry
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.ui.addFood.FoodDetailsActivity

class ActionDialogPresenter (
    private val mContext: Context,
    private val viewModel: ActivityJournalViewModel,
    private val entry: ActivityEntry,
) {

    companion object {
        const val KEY_ACTIVITY_ENTRY = "KEY_ACTIVITY_ENTRY"
    }

    private val actions = mContext.resources.getStringArray(R.array.food_entry_actions)

    private val dialog = AlertDialog.Builder(mContext).setItems(actions) { d, c ->
        when (c) {
            0 -> mContext.startActivity(
                Intent (mContext, FoodDetailsActivity::class.java).apply {
                    putExtra(KEY_ACTIVITY_ENTRY, entry)
                }
            )
            1 -> viewModel.deleteEntry(entry)
        }
        d.dismiss()
    }.create()

    fun show() = dialog.show()
}