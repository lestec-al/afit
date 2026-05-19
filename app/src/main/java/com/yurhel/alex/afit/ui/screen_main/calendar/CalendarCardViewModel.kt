package com.yurhel.alex.afit.ui.screen_main.calendar

import android.content.Context
import android.text.format.DateFormat
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yurhel.alex.afit.R
import com.yurhel.alex.afit.data.LocalRepo
import com.yurhel.alex.afit.data.Obj
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

class CalendarCardViewModel(private val localRepo: LocalRepo): ViewModel() {
    class Factory(private val localRepo: LocalRepo): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = CalendarCardViewModel(localRepo) as T
    }

    val today: Calendar = Calendar.getInstance()
    var currentPage = 1

    val weekNames = updateWeekNames()
    private fun updateWeekNames(): List<String> {
        val list = mutableListOf<String>()
        val c = Calendar.getInstance()
        var d = c.firstDayOfWeek
        c[Calendar.DAY_OF_WEEK] = d
        (1..7).forEach {
            list.add(DateFormat.format("EEE", c).toString())
            d = if ((d + 1 <= 7)) d + 1 else 1
            c[Calendar.DAY_OF_WEEK] = d
        }
        return list
    }

    var isDatePickerON by mutableStateOf(false)
        private set
    fun setIsDatePickerON(value: Boolean) {
        isDatePickerON = value
    }

    var nameOfMonth by mutableStateOf("")
        private set
    var monthsData by mutableStateOf(listOf<MonthObj>())
        private set
    /**
     * Get data for [calendar] & one month before & one month after
     */
    fun get3MonthsData(
        calendar: Calendar,
        pagerState: PagerState?
    ) {
        viewModelScope.launch {
            // Set month name
            nameOfMonth = DateFormat.format(
                if ((calendar[Calendar.YEAR] == today[Calendar.YEAR])) "LLLL" else "LLLL yyyy",
                calendar
            ).toString()
            // Edit calendar
            val calEdit = Calendar.getInstance()
            calEdit.time = calendar.time
            calEdit.set(Calendar.DAY_OF_MONTH, 1)
            calEdit.set(Calendar.HOUR_OF_DAY, 0)
            // Set to -2 to make possible increasing +1 each time in for loop
            calEdit.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 2)
            // Loop for months
            val listOfMonths = mutableListOf<MonthObj>()
            (1..3).forEach {
                calEdit.set(Calendar.MONTH, calEdit.get(Calendar.MONTH) + 1)
                listOfMonths.add(get1MonthData(calEdit))
            }
            monthsData = listOfMonths.toList()
            // Scroll to pos 1 (middle target pos)
            // Without that there are scroll bugs
            if (pagerState != null) {
                while (pagerState.isScrollInProgress) {
                    delay(100)
                }
                pagerState.scrollToPage(1)
            }
        }
    }

    fun addMonthToData(
        lastScrolledBackward: Boolean,
        landedCal: Calendar
    ) {
        // Set month name
        nameOfMonth = DateFormat.format(
            if ((landedCal[Calendar.YEAR] == today[Calendar.YEAR])) "LLLL" else "LLLL yyyy",
            landedCal
        ).toString()

        val landedCalEdit = Calendar.getInstance()
        landedCalEdit.timeInMillis = landedCal.timeInMillis
        landedCalEdit.set(Calendar.DAY_OF_MONTH, 1)
        landedCalEdit.set(Calendar.HOUR_OF_DAY, 0)
        if (lastScrolledBackward) {
            landedCalEdit.set(Calendar.MONTH, landedCalEdit.get(Calendar.MONTH) - 1)
        } else {
            landedCalEdit.set(Calendar.MONTH, landedCalEdit.get(Calendar.MONTH) + 1)
        }

        val calEdit = if (lastScrolledBackward) {
            val calEdit = Calendar.getInstance()
            calEdit.timeInMillis = monthsData[0].calendar.timeInMillis
            calEdit.set(Calendar.DAY_OF_MONTH, 1)
            calEdit.set(Calendar.HOUR_OF_DAY, 0)
            calEdit.set(Calendar.MONTH, calEdit.get(Calendar.MONTH) - 1)
            calEdit
        } else {
            val calEdit = Calendar.getInstance()
            calEdit.timeInMillis = monthsData.last().calendar.timeInMillis
            calEdit.set(Calendar.DAY_OF_MONTH, 1)
            calEdit.set(Calendar.HOUR_OF_DAY, 0)
            calEdit.set(Calendar.MONTH, calEdit.get(Calendar.MONTH) + 1)
            calEdit
        }

        val yearEquals = landedCalEdit.get(Calendar.YEAR) == calEdit.get(Calendar.YEAR)
        val monthEquals = landedCalEdit.get(Calendar.MONTH) == calEdit.get(Calendar.MONTH)
        if (yearEquals && monthEquals) {
            val newM = get1MonthData(calEdit)
            val list = if (lastScrolledBackward) {
                listOf(newM) + monthsData
            } else {
                monthsData + listOf(newM)
            }
            monthsData = list.toList()
        }
    }

    private fun get1MonthData(calEdit: Calendar): MonthObj {
        // Calc the remaining days for the previous month
        var day = 1
        var calendarWeekDay = calEdit.firstDayOfWeek
        val firstMonthWeekDay = calEdit[Calendar.DAY_OF_WEEK]
        if (calendarWeekDay != firstMonthWeekDay) {
            var i = 1
            while (i < 8) {
                calendarWeekDay += 1
                if (calendarWeekDay > 7) calendarWeekDay = 1
                if (calendarWeekDay == firstMonthWeekDay) {
                    day -= i
                    break
                }
                i += 1
            }
        }
        // Get data for 1 month
        val c = Calendar.getInstance()
        c.timeInMillis = calEdit.timeInMillis
        c[Calendar.DAY_OF_MONTH] = day
        c[Calendar.HOUR_OF_DAY] = c.getActualMinimum(Calendar.HOUR_OF_DAY)
        val monthStart = c.timeInMillis
        c.timeInMillis = calEdit.timeInMillis
        c[Calendar.DAY_OF_MONTH] = day + 41
        c[Calendar.HOUR_OF_DAY] = c.getActualMaximum(Calendar.HOUR_OF_DAY)
        val monthEnd = c.timeInMillis
        val dataForMonth = localRepo.getFilteredAllData(monthStart, monthEnd)
        // Setup days
        val listOfDays = mutableListOf<DayObj>()
        (0..41).forEach {
            // This calendar represent date of the specific day
            // And at the start it often be for previous month
            val calEditDay = Calendar.getInstance()
            calEditDay[Calendar.YEAR] = calEdit[Calendar.YEAR]
            calEditDay[Calendar.MONTH] = calEdit[Calendar.MONTH]
            calEditDay[Calendar.DAY_OF_MONTH] = day // Day may be negative
            // Get data for this day
            val dataThisDay = mutableListOf<Obj>()
            for (i in dataForMonth) {
                c.timeInMillis = i.date
                // If day from position == day from data
                if (
                    c[Calendar.DAY_OF_MONTH] == calEditDay[Calendar.DAY_OF_MONTH] &&
                    c[Calendar.MONTH] == calEditDay[Calendar.MONTH]
                ) {
                    dataThisDay.add(i)
                }
            }
            dataThisDay.sortBy { it.date }
            listOfDays.add(
                DayObj(
                    dayNumber = calEditDay[Calendar.DAY_OF_MONTH].toString(),
                    isThisMonth = calEditDay[Calendar.MONTH] == calEdit[Calendar.MONTH],
                    isToday = (
                            today[Calendar.YEAR] == calEditDay[Calendar.YEAR] &&
                                    today[Calendar.MONTH] == calEditDay[Calendar.MONTH] &&
                                    today[Calendar.DAY_OF_MONTH] == calEditDay[Calendar.DAY_OF_MONTH]
                            ),
                    timeMills = calEditDay.timeInMillis,
                    listOfStats = dataThisDay
                )
            )
            day++
        }
        val staticCal = Calendar.getInstance()
        staticCal.timeInMillis = calEdit.timeInMillis
        return MonthObj(days = listOfDays, calendar = staticCal)
    }

    init {
        get3MonthsData(today, null)
    }

    // Show the day dialog
    var isShowDayDialog by mutableStateOf(false)
        private set
    var timeForDay by mutableLongStateOf(0L)
        private set
    var dataForDay by mutableStateOf(listOf<Obj>())
        private set
    fun setIsShowDayDialog(
        context: Context,
        value: Boolean,
        dayObj: DayObj? = null
    ) {
        isShowDayDialog = value
        // Init some data for dialog
        if (dayObj != null) {
            calcTrainingTime(dayObj.listOfStats, context)
            dataForDay = dayObj.listOfStats
            timeForDay = dayObj.timeMills
        }
    }

    // Calc training time for the day
    var trainingTimeText by mutableStateOf("")
        private set
    private fun calcTrainingTime(
        dataForDay: List<Obj>,
        context: Context
    ) {
        var allTimeMin = 0
        var allTimeSec = 0
        // Loop data for the day
        dataForDay.forEach {
            if (it.isExercise) {
                val t = it.time
                    .split(":".toRegex())
                    .dropLastWhile { it1 -> it1.isEmpty() }
                    .toTypedArray()
                allTimeMin += t[0].toInt()
                allTimeSec += t[1].toInt()
            }
        }
        // Finish calc training time
        trainingTimeText = if (allTimeMin > 0 || allTimeSec > 0) {
            if (allTimeSec > 59) {
                allTimeMin += allTimeSec / 60
                if (allTimeSec % 60 > 30) allTimeMin += 1
            } else {
                if (allTimeSec > 30) allTimeMin += 1
            }
            "$allTimeMin ${context.getText(R.string.training_min)}"
        } else {
            ""
        }
    }
}