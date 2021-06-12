package com.bruhascended.fitapp.util

import android.content.Context
import android.content.res.Resources
import android.database.DataSetObserver
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import java.util.*

class CustomArrayAdapter(context: Context, id: Int, objects: Array<String>) :
    ArrayAdapter<String>(context, id, objects) {

    private val filter: Filter = NoFilter(objects)

    class NoFilter(objects: Array<String>) : Filter() {
        val items = objects
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val results = FilterResults()
            results.count = items.size
            results.values = items
            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {

        }
    }

    override fun equals(other: Any?): Boolean {
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    override fun toString(): String {
        return super.toString()
    }

    override fun registerDataSetObserver(observer: DataSetObserver?) {
        super.registerDataSetObserver(observer)
    }

    override fun unregisterDataSetObserver(observer: DataSetObserver?) {
        super.unregisterDataSetObserver(observer)
    }

    override fun getCount(): Int {
        return super.getCount()
    }

    override fun getItem(position: Int): String? {
        return super.getItem(position)
    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }

    override fun hasStableIds(): Boolean {
        return super.hasStableIds()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return super.getView(position, convertView, parent)
    }

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }

    override fun getViewTypeCount(): Int {
        return super.getViewTypeCount()
    }

    override fun isEmpty(): Boolean {
        return super.isEmpty()
    }

    override fun getAutofillOptions(): Array<CharSequence>? {
        return super.getAutofillOptions()
    }

    override fun areAllItemsEnabled(): Boolean {
        return super.areAllItemsEnabled()
    }

    override fun isEnabled(position: Int): Boolean {
        return super.isEnabled(position)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return super.getDropDownView(position, convertView, parent)
    }

    override fun notifyDataSetChanged() {
        super.notifyDataSetChanged()
    }

    override fun notifyDataSetInvalidated() {
        super.notifyDataSetInvalidated()
    }

    override fun setAutofillOptions(vararg options: CharSequence?) {
        super.setAutofillOptions(*options)
    }

    override fun getFilter(): Filter {
        return filter
    }

    override fun setDropDownViewTheme(theme: Resources.Theme?) {
        super.setDropDownViewTheme(theme)
    }

    override fun getDropDownViewTheme(): Resources.Theme? {
        return super.getDropDownViewTheme()
    }

    override fun add(`object`: String?) {
        super.add(`object`)
    }

    override fun addAll(collection: MutableCollection<out String>) {
        super.addAll(collection)
    }

    override fun addAll(vararg items: String?) {
        super.addAll(*items)
    }

    override fun insert(`object`: String?, index: Int) {
        super.insert(`object`, index)
    }

    override fun remove(`object`: String?) {
        super.remove(`object`)
    }

    override fun clear() {
        super.clear()
    }

    override fun sort(comparator: Comparator<in String>) {
        super.sort(comparator)
    }

    override fun setNotifyOnChange(notifyOnChange: Boolean) {
        super.setNotifyOnChange(notifyOnChange)
    }

    override fun getContext(): Context {
        return super.getContext()
    }

    override fun getPosition(item: String?): Int {
        return super.getPosition(item)
    }

    override fun setDropDownViewResource(resource: Int) {
        super.setDropDownViewResource(resource)
    }
}