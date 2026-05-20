package com.yurhel.alex.afit.ui.screen_main.cards

import com.jjoe64.graphview.series.DataPoint

enum class ViewType { Grid, SmallGrid, Column }

data class GraphDataLite(
    val xyData: List<DataPoint>,
    val startDate: Long,
    val endDate: Long,
    val statsMin: Double,
    val statsMax: Double,
    val objColor: Int
)