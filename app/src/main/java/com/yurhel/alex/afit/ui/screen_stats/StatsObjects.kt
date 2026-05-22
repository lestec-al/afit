package com.yurhel.alex.afit.ui.screen_stats

import com.jjoe64.graphview.series.DataPoint
import com.yurhel.alex.afit.data.Obj

data class GraphData(
    val oneId: Int,
    val listData: List<Obj>,
    val xyData: List<DataPoint>,
    val startDate: Long,
    val endDate: Long,
    val statsMin: Double,
    val statsMax: Double,
    val objColor: Int,
    // Short info
    val isExercise: Boolean,
    val size: String,
    val heightValue: String,
    val smallValue: String
)

enum class DateButtonType { Start, End }

enum class Filter { All, With, Without }