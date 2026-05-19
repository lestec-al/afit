package com.yurhel.alex.afit.ui.screen_stats.components

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.yurhel.alex.afit.ui.help.MonthPickerBottomSheet
import com.yurhel.alex.afit.ui.help.edit.EditBottomSheet
import com.yurhel.alex.afit.ui.help.edit.EditBottomSheetViewModel
import com.yurhel.alex.afit.ui.screen_stats.DateButtonType
import com.yurhel.alex.afit.ui.screen_stats.StatsViewModel
import java.util.Calendar

@Composable
fun SetupBottomSheets(
    onBack: () -> Unit,
    vm: StatsViewModel,
    context: Context,
    color: Color
) {
    if (vm.editBottomSheetOpen) {
        EditBottomSheet(
            onDismiss = { vm.updateEditBottomSheetOpen(false) },
            onSave = {
                vm.updateAll()
                vm.updateEditBottomSheetOpen(false)
            },
            onDelete = {
                vm.updateEditBottomSheetOpen(false)
                onBack()
            },
            vm = EditBottomSheetViewModel(vm.localRepo, vm.graphData?.isExercise, vm.graphData?.oneId)
        )
    }
    if (vm.addBottomSheetOpen) {
        AddStBottomSheet(
            onDismiss = { vm.updateAddBottomSheetOpen(false) },
            onAction = {
                vm.updateAddBottomSheetOpen(false)
                vm.updateAll()
            },
            vm = AddStBottomSheetViewModel(
                localRepo = vm.localRepo,
                isEdit = vm.isEditEntry,
                isExercise = vm.graphData!!.isExercise,
                context = context,
                mainObjId = vm.graphData!!.oneId,
                entryObj = vm.editedEntry
            )
        )
    }
    if (vm.showExBottomSheetOpen) {
        ShowExBottomSheet(
            onDismiss = {
                vm.updateShowExBottomSheetOpen(false)
                vm.updateIsAfterWorkoutState(false)
            },
            onAction = {
                vm.updateShowExBottomSheetOpen(false)
                vm.updateAll()
            },
            isShowCongratsText = vm.isAfterWorkoutState,
            localRepo = vm.localRepo,
            mainObjId = vm.graphData!!.oneId,
            mainObjColor = color,
            entryObj = vm.editedEntry!!
        )
    }
    val c = Calendar.getInstance()
    MonthPickerBottomSheet(
        visible = vm.isDatePickerON,
        currentMonth = when(vm.dateButtonType) {
            // Repeated because "vm.dateButtonType" changing depending on user action
            DateButtonType.Start -> {
                c.timeInMillis = vm.graphData!!.startDate
                c.get(Calendar.MONTH)
            }
            DateButtonType.End -> {
                c.timeInMillis = vm.graphData!!.endDate
                c.get(Calendar.MONTH)
            }
        },
        currentYear = when(vm.dateButtonType) {
            DateButtonType.Start -> {
                c.timeInMillis = vm.graphData!!.startDate
                c.get(Calendar.YEAR)
            }
            DateButtonType.End -> {
                c.timeInMillis = vm.graphData!!.endDate
                c.get(Calendar.YEAR)
            }
        },
        currentDay = when(vm.dateButtonType) {
            DateButtonType.Start -> {
                c.timeInMillis = vm.graphData!!.startDate
                c.get(Calendar.DAY_OF_MONTH)
            }
            DateButtonType.End -> {
                c.timeInMillis = vm.graphData!!.endDate
                c.get(Calendar.DAY_OF_MONTH)
            }
        },
        confirmButtonCLicked = { month, year, day ->
            vm.updateMonthsData(month, year, day)
            vm.setIsDatePickerON(false)
        },
        cancelClicked = {
            vm.setIsDatePickerON(false)
        },
        resetClicked = {
            vm.updateMonthsData()
            vm.setIsDatePickerON(false)
        },
        primaryColor = color
    )
}