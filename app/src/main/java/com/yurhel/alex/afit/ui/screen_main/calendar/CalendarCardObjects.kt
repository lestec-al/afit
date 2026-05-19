package com.yurhel.alex.afit.ui.screen_main.calendar

import com.yurhel.alex.afit.data.Obj
import java.util.Calendar

data class DayObj(
    val dayNumber: String,
    val isThisMonth: Boolean,
    val isToday: Boolean,
    val timeMills: Long,
    val listOfStats: List<Obj>
)

data class MonthObj(
    val days: List<DayObj>,
    val calendar: Calendar
)