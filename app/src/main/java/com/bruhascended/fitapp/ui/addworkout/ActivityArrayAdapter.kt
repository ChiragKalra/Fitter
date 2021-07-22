package com.bruhascended.fitapp.ui.addworkout

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import com.bruhascended.fitapp.databinding.ItemActivityDropdownBinding

class ActivityArrayAdapter(
    context: Context,
    private val resource: Int,
    objects: MutableList<ActivityViewType>
) :
    ArrayAdapter<ActivityViewType>(context, resource, objects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding: ItemActivityDropdownBinding? = if (convertView == null) {
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                resource,
                parent,
                false
            )
        } else {
            convertView.tag as ItemActivityDropdownBinding?
        }
        val item = getItem(position)
        if (item != null) {
            binding?.apply {
                imageViewActivityType.setImageResource(item.imageResInt)
                textviewActivityType.text = item.activityType
                root.tag = binding
            }
        }
        return binding!!.root
    }
}