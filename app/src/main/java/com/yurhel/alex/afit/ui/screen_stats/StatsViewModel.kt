package com.yurhel.alex.afit.ui.screen_stats

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jjoe64.graphview.series.DataPoint
import com.yurhel.alex.afit.data.LocalRepo
import com.yurhel.alex.afit.data.Obj
import java.util.Calendar
import java.util.Date

class StatsViewModel(
    val localRepo: LocalRepo,
    private val objType: String,
    private val objId: Int,
    isAfterWorkout: Boolean
): ViewModel() {
    class Factory(
        private val localRepo: LocalRepo,
        private val objType: String,
        private val objId: Int,
        private val isAfterWorkout: Boolean
    ): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = StatsViewModel(
            localRepo, objType, objId, isAfterWorkout
        ) as T
    }

    var title by mutableStateOf("")
        private set
    var graphData: GraphData? by mutableStateOf(null)
        private set
    fun updateAll() {
        val isExercise = objType == "ex_id"
        val obj = localRepo.getOneMainObj(objId, isExercise)
        title = obj.name
        allNumberOfSets.clear()
        allNumberOfSets.add(0)
        var data = localRepo.getTableEntries(
            objId,
            isExercise,
            if (obj.start.isEmpty()) 0 else obj.start.toLong(),
            if (obj.end.isEmpty()) 0 else obj.end.toLong()
        ).toList()
        if (isExercise) {
            data = data.filter {
                // First filter by: all, with or without weight
                val res = when (weightFilter) {
                    Filter.All -> true
                    Filter.With -> it.allWeights.isNotEmpty()
                    Filter.Without -> it.allWeights.isEmpty()
                }
                // If object must be shown (according the first filter)
                // Then filter by number of sets
                if (!res) false else {
                    val sets = it.longerValue.split("+").size
                    allNumberOfSets.add(sets)
                    if (numberOfSets == 0) true else sets == numberOfSets
                }
            }
        } else {
            data = data.filter {
                // Filter by: all, with or without note
                when (noteFilter) {
                    Filter.All -> true
                    Filter.With -> it.longerValue.isNotEmpty()
                    Filter.Without -> it.longerValue.isEmpty()
                }
            }
        }
        val dataSize = data.size
        if (dataSize > 0) data = data.sortedWith(Comparator.comparing { Date(it.date) })
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
        // Setup graph data & get short info
        var oneSetMax = 0
        var statsMin = 0.0
        var statsMax = 0.0
        val xyData = ArrayList<DataPoint>()
        var statsMin2 = 0.0
        var statsMax2 = 0.0
        val xyData2 = ArrayList<DataPoint>()
        data.forEach { item ->
            xyData.add(DataPoint(Date(item.date), item.mainValue))
            // Second scale
            var allWeightsOneNumber = 0.0
            item.allWeights
                .takeIf { it != null && it.isNotEmpty() }
                ?.split(" + ")
                ?.forEach {
                    if (it.isNotEmpty()) {
                        allWeightsOneNumber += it.toDouble()
                    }
                }
            if (allWeightsOneNumber > 0) {
                xyData2.add(DataPoint(Date(item.date), allWeightsOneNumber))
            }
            // Get short info
            if (isExercise) {
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
            // Second scale
            if (allWeightsOneNumber > statsMax2) statsMax2 = allWeightsOneNumber
            if (statsMin2 == 0.0) statsMin2 = allWeightsOneNumber
            else if (allWeightsOneNumber < statsMin2) statsMin2 = allWeightsOneNumber
        }
        // Update reps (max value when workout starts)
        if (isExercise && oneSetMax > 0) {
            localRepo.updateExercise(
                obj.name, objId, obj.rest, oneSetMax + 1, obj.sets, obj.weight, obj.color
            )
        }
        // Graph update
        graphData = GraphData(
            oneId = objId,
            listData = data.reversed(),
            xyData = xyData,
            xyData2 = xyData2,
            startDate = startDate,
            endDate = endDate,
            statsMin = statsMin,
            statsMax = statsMax,
            statsMin2 = statsMin2,
            statsMax2 = statsMax2,
            objColor = obj.color,
            // Update short info
            isExercise = isExercise,
            size = data.size.toString(),
            heightValue = if ((isExercise)) "${statsMax.toInt()}," else statsMax.toString(),
            smallValue = if ((isExercise)) oneSetMax.toString() else statsMin.toString()
        )
    }

    var isGraphHidden by mutableStateOf(false)
        private set
    fun flipGraphVisibility() {
        isGraphHidden = !isGraphHidden
    }

    var editBottomSheetOpen by mutableStateOf(false)
        private set
    fun updateEditBottomSheetOpen(value: Boolean) {
        editBottomSheetOpen = value
    }

    var addBottomSheetOpen by mutableStateOf(false)
        private set
    fun updateAddBottomSheetOpen(value: Boolean) {
        addBottomSheetOpen = value
    }

    var showExBottomSheetOpen by mutableStateOf(false)
        private set
    fun updateShowExBottomSheetOpen(value: Boolean) {
        showExBottomSheetOpen = value
    }

    var isEditEntry by mutableStateOf(false)
        private set
    var editedEntry by mutableStateOf<Obj?>(null)
        private set
    fun updateIsEditValue(
        isEdit: Boolean,
        entry: Obj?
    ) {
        isEditEntry = isEdit
        editedEntry = entry
    }

    var isDatePickerON by mutableStateOf(false)
        private set
    var dateButtonType by mutableStateOf(DateButtonType.Start)
    fun setIsDatePickerON(
        value: Boolean,
        buttonType: DateButtonType? = null
    ) {
        isDatePickerON = value
        if (buttonType != null) dateButtonType = buttonType
    }

    fun updateMonthsData(
        month: Int? = null,
        year: Int? = null,
        day: Int? = null
    ) {
        val isStartButton = dateButtonType == DateButtonType.Start
        val date = if (month == null || year == null || day == null) "" else {
            val c = Calendar.getInstance()
            c.set(Calendar.MONTH, month)
            c.set(Calendar.YEAR, year)
            c.set(
                Calendar.DAY_OF_MONTH,
                day//if (isStartButton) 1 else c.getActualMaximum(Calendar.DAY_OF_MONTH)
            )
            "${c.timeInMillis}"
        }
        localRepo.setDate(
            date,
            objId,
            objType == "ex_id",
            isStartButton
        )
        updateAll()
    }

    var isAfterWorkoutState by mutableStateOf(isAfterWorkout)
        private set
    fun updateIsAfterWorkoutState(b: Boolean) {
        isAfterWorkoutState = b
    }

    var allNumberOfSets = mutableSetOf(0)
        private set
    var numberOfSets by mutableIntStateOf(0)
        private set
    fun updateNumberOfSets() {
        var setVal = false
        for (it in allNumberOfSets.sorted()) {
            if (setVal) {
                numberOfSets = it
                break
            }
            if (numberOfSets == it) {
                setVal = true
            }
            if (allNumberOfSets.maxOf { it } == it) {
                numberOfSets = 0
            }
        }
        updateAll()
    }

    var weightFilter by mutableStateOf(Filter.All)
        private set
    fun updateWeightFilter() {
        weightFilter = when (weightFilter) {
            Filter.All -> Filter.With
            Filter.With -> Filter.Without
            Filter.Without -> Filter.All
        }
        updateAll()
    }

    var noteFilter by mutableStateOf(Filter.All)
        private set
    fun updateNoteFilter() {
        noteFilter = when (noteFilter) {
            Filter.All -> Filter.With
            Filter.With -> Filter.Without
            Filter.Without -> Filter.All
        }
        updateAll()
    }

    init {
        updateAll()
        // Show congrats, when came to this screen after workout
        updateIsEditValue(isEdit = true, entry = graphData?.listData?.firstOrNull())
        updateShowExBottomSheetOpen(isAfterWorkout)
        // Hide graph if no data
        if (graphData == null || graphData?.size == "0") {
            isGraphHidden = true
        }
    }

    fun getIsShowWeightGraph() = localRepo.getIsWeightShowForMainStat(objId)
}