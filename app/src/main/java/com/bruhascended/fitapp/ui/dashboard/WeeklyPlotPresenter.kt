package com.bruhascended.fitapp.ui.dashboard

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.databinding.ItemCardDashboardBinding
import com.bruhascended.fitapp.util.AnimationDuration
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.components.XAxis.XAxisPosition
import kotlin.collections.ArrayList


class WeeklyPlotPresenter (
    private val mContext: Context,
    mInflater: LayoutInflater,
    val plotType: WeeklyCardType,
) {

    private val binding = ItemCardDashboardBinding.inflate(mInflater)

    val view: View
    get() = binding.root

    init {
        binding.chartName.text = mContext.getString(plotType.title)
        binding.chartName.setTextColor(mContext.getColor(plotType.titleColor))
    }

    private fun setupChart(chart: LineChart, data: LineData, color: Int) {
        (data.getDataSetByIndex(0) as LineDataSet).circleHoleColor = color

        // no description text
        chart.description.isEnabled = false

        //
        // enable / disable grid background
        chart.setDrawGridBackground(false)
        //        chart.getRenderer().getGridPaint().setGridColor(Color.WHITE & 0x70FFFFFF);

        // enable touch gestures
        chart.setTouchEnabled(true)

        // enable scaling and dragging
        chart.isDragEnabled = false
        chart.setScaleEnabled(false)

        // if disabled, scaling can be done on x- and y-axis separately
        chart.setPinchZoom(false)
        chart.setBackgroundColor(color)

        chart.setExtraOffsets(32f, 24f, 32f, 4f)

        // add data
        chart.data = data

        // get the legend (only possible after setting data)
        chart.legend.isEnabled = false
        chart.axisLeft.isEnabled = false
        chart.axisLeft.spaceTop = 40f
        chart.axisLeft.spaceBottom = 40f
        chart.axisRight.isEnabled = false
        chart.xAxis.isEnabled = true


        // setup X axis to show week days
        val xAxisFormatter = DayOfWeekFormatter(mContext)

        val xAxis = chart.xAxis
        xAxis.position = XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(true)
        xAxis.gridColor = Color.argb(32, 0,0,0)
        xAxis.mAxisRange = 6f
        xAxis.textSize = 12f

        xAxis.labelCount = 7
        xAxis.granularity = 1f // only intervals of 1 day
        xAxis.valueFormatter = xAxisFormatter
        xAxis.setAvoidFirstLastClipping(true)

        // animate calls invalidate()
        chart.animateX(AnimationDuration.VERY_LONG.ms.toInt())
    }

    private fun getData(entries: FloatArray): LineData {
        var values: ArrayList<Entry> = ArrayList()
        entries.forEachIndexed { i, day ->
            values.add(Entry(i.toFloat(), day))
        }

        // create a dataset and give it a type
        val set1 = LineDataSet(values, "DataSet 1")
        // set1.setFillAlpha(110);
        // set1.setFillColor(Color.RED);
        set1.lineWidth = 1f
        set1.circleRadius = 7.5f
        set1.circleHoleRadius = 5f
        set1.color = mContext.getColor(plotType.plotColor)
        set1.setCircleColor(mContext.getColor(plotType.titleColor))
        set1.highLightColor = Color.TRANSPARENT // cross-hair color
        set1.setDrawValues(true)
        set1.valueTextSize = 14f


        values = ArrayList()
        val minValue = entries.minOrNull() ?: 0f
        for (i in entries.size..6) {
            values.add(Entry(i.toFloat(), minValue))
        }

        // create a dataset and give it a type
        val set2 = LineDataSet(values, "DataSet 2")
        // set1.setFillAlpha(110);
        // set1.setFillColor(Color.RED);
        set2.lineWidth = 0f
        set2.circleRadius = 0f
        set2.circleHoleRadius = 0f
        set2.color = Color.TRANSPARENT
        set2.setCircleColor(Color.TRANSPARENT)
        set2.highLightColor = Color.TRANSPARENT
        set2.setDrawValues(false)

        // create a data object with the data sets
        return LineData(set1, set2)
    }

    private var previousValues: FloatArray? = null
    fun generateCard(values: FloatArray?) {
        values ?: return
        if (previousValues.contentEquals(values)) return
        setupChart(binding.lineChart, getData(values), mContext.getColor(R.color.white_900))
        previousValues = values
    }
}
