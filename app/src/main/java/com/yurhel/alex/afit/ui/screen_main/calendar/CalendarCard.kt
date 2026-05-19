package com.yurhel.alex.afit.ui.screen_main.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.yurhel.alex.afit.R
import com.yurhel.alex.afit.ui.help.MonthPickerBottomSheet
import com.yurhel.alex.afit.ui.help.upperFirstChar
import java.util.Calendar
import androidx.compose.ui.res.stringResource

@Composable
fun CalendarCard(
    gridHeightDp: Int,
    modifier: Modifier,
    vm: CalendarCardViewModel
) {
    val isDark = isSystemInDarkTheme()
    val context = LocalContext.current
    val itemHeight = gridHeightDp / 6
    val weekDaysNamesHeight = 25.dp
    val pagerState = rememberPagerState(
        initialPage = 1,
        pageCount = { vm.monthsData.size }
    )
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            vm.currentPage = page
            vm.addMonthToData(
                pagerState.lastScrolledBackward,
                vm.monthsData[page].calendar
            )
        }
    }

    MonthPickerBottomSheet(
        visible = vm.isDatePickerON,
        currentMonth = vm.monthsData[vm.currentPage].calendar.get(Calendar.MONTH),
        currentYear = vm.monthsData[vm.currentPage].calendar.get(Calendar.YEAR),
        currentDay = null,
        confirmButtonCLicked = { month, year, day ->
            val c = Calendar.getInstance()
            c.set(Calendar.MONTH, month)
            c.set(Calendar.YEAR, year)
            c.set(Calendar.DAY_OF_MONTH, day)
            vm.get3MonthsData(c, pagerState)
            vm.setIsDatePickerON(false)
        },
        cancelClicked = {
            vm.setIsDatePickerON(false)
        },
        resetClicked = {
            vm.get3MonthsData(Calendar.getInstance(), pagerState)
            vm.setIsDatePickerON(false)
        }
    )
    DayBottomSheet(
        onDismissRequest = {
            vm.setIsShowDayDialog(context, false)
        },
        visible = vm.isShowDayDialog,
        vm = vm
    )

    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        // Upper row with text & actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Info text
            Text(
                text = vm.nameOfMonth.upperFirstChar(),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.weight(1f))
            // Set date button
            IconButton(onClick = {
                vm.setIsDatePickerON(true)
            }) {
                Icon(painterResource(R.drawable.ic_calendar_edit), stringResource(R.string.set_date))
            }
            // Set date now button
            IconButton(onClick = {
                vm.get3MonthsData(vm.today, pagerState)
            }) {
                Icon(painterResource(R.drawable.ic_calendar_now), stringResource(R.string.set_date))
            }
        }
        HorizontalDivider()
        // Show months to all cards
        HorizontalPager(
            state = pagerState,
            key = { vm.monthsData[it].calendar.timeInMillis }
        ) { page ->
            val month = vm.monthsData[page]
            // Setup days
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                userScrollEnabled = false,
                verticalArrangement = Arrangement.Bottom,
                // Added for compensation of the row with day names !
                modifier = Modifier.height(gridHeightDp.dp + weekDaysNamesHeight)
            ) {
                // Row with day names
                vm.weekNames.forEach { item {
                    Box(
                        modifier = Modifier.height(weekDaysNamesHeight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = it)
                    }
                } }
                // Days
                month.days.forEach { item {
                    Box(
                        modifier = Modifier
                            .height(itemHeight.dp)
                            .border(width = 0.3.dp, color = DividerDefaults.color)
                            .clickable {
                                vm.setIsShowDayDialog(context, true, it)
                            },
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            // Day number
                            Text(
                                text = it.dayNumber,
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .fillMaxWidth()
                                    .background(
                                        color = if (it.isToday) {
                                            MaterialTheme.colorScheme.onBackground
                                        } else {
                                            Color.Transparent
                                        },
                                        shape = CardDefaults.shape
                                    ),
                                textAlign = TextAlign.Center,
                                color = if (it.isToday) {
                                    if (isDark) Color.Black else Color.White
                                } else {
                                    if (it.isThisMonth) {
                                        Color.Unspecified
                                    } else {
                                        if (isDark) {
                                            Color.White.copy(alpha = 0.4f)
                                        } else {
                                            Color.Black.copy(alpha = 0.4f)
                                        }
                                    }
                                }
                            )
                            // Stats for the specific day
                            it.listOfStats.forEach { it1 ->
                                val text = if ((it1.isExercise)) {
                                    it1.mainValue.toInt().toString()
                                } else {
                                    it1.mainValue.toString()
                                }
                                val height = TextUnit(10f, TextUnitType.Sp)
                                Text(
                                    text = when {
                                        text.length > 4 -> text.substring(0, 4)
                                        (text.isEmpty() || text == "0" || text == "0.0") -> ""
                                        else -> text
                                    },
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp, vertical = 0.5.dp)
                                        .fillMaxWidth()
                                        .background(
                                            color = Color(it1.color),
                                            shape = RoundedCornerShape(4.dp)
                                        ),
                                    textAlign = TextAlign.Center,
                                    color = if (it1.color == Color.White.toArgb()) Color.Black else Color.White,
                                    fontSize = height,
                                    lineHeight = height,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                } }
            }
        }
    }
}