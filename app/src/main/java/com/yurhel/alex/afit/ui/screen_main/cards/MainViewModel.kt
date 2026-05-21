package com.yurhel.alex.afit.ui.screen_main.cards

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jjoe64.graphview.series.DataPoint
import com.yurhel.alex.afit.data.LocalRepo
import com.yurhel.alex.afit.data.Obj
import com.yurhel.alex.afit.data.getScores
import com.yurhel.alex.afit.ui.screen_main.upbar.LevelObj
import java.util.Date

class MainViewModel(val localRepo: LocalRepo): ViewModel() {
    class Factory(private val localRepo: LocalRepo): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = MainViewModel(localRepo) as T
    }

    val scores = getScores(localRepo)
    val levelObj = calculateLevel()

    private fun calculateLevel(): LevelObj {
        val x = "${(scores.allPoints / 1000f) + 1}".split('.')
        val currentLevel = x[0]
        return LevelObj(
            currentLevel = currentLevel,
            nextLevel = "${currentLevel.toInt() + 1}",
            progress = "0.${x[1]}".toFloat()
        )
    }

    var data by mutableStateOf(listOf<Obj>())
        private set

    fun updateData() {
        data = localRepo.getMainTableEntries(true, true) + localRepo.getMainTableEntries(false, true)
    }

    var isUpSheetVisible by mutableStateOf(false)
        private set
    fun setUpSheetVisibility(visible: Boolean) {
        isUpSheetVisible = visible
    }

    var editBottomSheetOpen by mutableStateOf(false)
        private set
    fun updateEditBottomSheet(visibility: Boolean, withDataUpdate: Boolean = false) {
        if (withDataUpdate) updateData()
        editBottomSheetOpen = visibility
    }

    var viewType by mutableStateOf(
        when (localRepo.device) {
            "col" -> ViewType.Column
            "grid" -> ViewType.Grid
            else -> ViewType.SmallGrid
        }
    )
        private set
    fun updateViewType(value: ViewType) {
        localRepo.device = when (value) {
            ViewType.Grid -> "grid"
            ViewType.SmallGrid -> "smallGrid"
            ViewType.Column -> "col"
        }
        viewType = value
    }

    init {
        updateData()
    }

    fun getCardData(obj: Obj): Pair<GraphDataLite, Boolean> {
        val data = localRepo.getTableEntries(
            obj.id,
            obj.isExercise,
            if (obj.start.isEmpty()) 0 else obj.start.toLong(),
            if (obj.end.isEmpty()) 0 else obj.end.toLong()
        )
        val dataSize: Int = data.size
        if (dataSize > 0) data.sortWith(Comparator.comparing { Date(it.date) })
        // Setup date boundaries
        val dateToday = System.currentTimeMillis()
        val startDate = if (obj.start.isEmpty()) {
            if ((dataSize > 0)) data[0].date else dateToday
        } else {
            obj.start.toLong()
        }
        val endDate = if (obj.end.isEmpty()) {
            if ((dataSize > 0)) data[dataSize - 1].date else dateToday
        } else {
            obj.end.toLong()
        }
        // Cut data within date boundaries + get short info
        var oneSetMax = 0
        var statsMin = 0.0
        var statsMax = 0.0
        var maxMainValue = 0.0
        val xyData = ArrayList<DataPoint>()
        for (item in data) {
            if (item.mainValue > maxMainValue) {
                maxMainValue = item.mainValue
            }
            xyData.add(DataPoint(Date(item.date), item.mainValue))
            // Get short info
            if (obj.isExercise) {
                for (i in item.longerValue.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()) {
                    if (i != "+") {
                        val iInt = i.toInt()
                        if (iInt > oneSetMax) oneSetMax = iInt
                    }
                }
            }
            if (item.mainValue > statsMax) statsMax = item.mainValue
            if (statsMin == 0.0) statsMin = item.mainValue
            else if (item.mainValue < statsMin) statsMin = item.mainValue
        }
        // Graph update
        return Pair(
            GraphDataLite(
                xyData = xyData,
                startDate = startDate,
                endDate = endDate,
                statsMin = statsMin,
                statsMax = statsMax,
                objColor = obj.color
            ),
            maxMainValue > 0
        )
    }
}