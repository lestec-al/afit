package com.yurhel.alex.afit.ui.screen_stats.components

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.yurhel.alex.afit.R
import com.yurhel.alex.afit.data.LocalRepo
import com.yurhel.alex.afit.data.Obj
import com.yurhel.alex.afit.ui.help.edit.SettingsObj
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.math.roundToInt

class AddStBottomSheetViewModel(
    val localRepo: LocalRepo,
    val isEdit: Boolean,
    val isExercise: Boolean,
    context: Context,
    private val mainObjId: Int,
    val entryObj: Obj?
) {
    // Date
    val localeFormatter: DateFormat = SimpleDateFormat.getDateInstance(
        SimpleDateFormat.SHORT,
        context.resources.configuration.locales[0]
    )
    val localPattern = (localeFormatter as SimpleDateFormat).toLocalizedPattern().uppercase()

    var dateValue by mutableStateOf("")
        private set
    var formattedDateValue by mutableStateOf<Date?>(null)
        private set
    fun updateDateValue(value: String) {
        dateValue = value
        formattedDateValue = try {
            localeFormatter.parse(dateValue)
        } catch (_: Exception) {
            null
        }
    }

    //
    var mainObj by mutableStateOf<Obj>(localRepo.getOneMainObj(mainObjId, isExercise))
        private set

    private fun check(value: Any?, setValue: () -> String) = when (value) {
        null -> setValue()
        else -> value.toString()
    }
    var textSettings = mutableStateListOf(
        SettingsObj(R.string.value, check(entryObj?.mainValue) { "1.0" }),
        SettingsObj(R.string.note, check(entryObj?.longerValue) { "" }),
    )
        private set
    fun updateTextSettings(newObj: SettingsObj, pos: Int) {
        textSettings[pos] = newObj
    }

    fun saveAllSettingsToDB() {
        val valueV = textSettings.find { it.label == R.string.value }!!.value
        val noteV = textSettings.find { it.label == R.string.note }!!.value
        val valueChecked = try {
            (valueV.toDouble() * 10.0).roundToInt() / 10.0
        } catch (_: Exception) { 0.0 }
        if (isEdit) {
            localRepo.updateStatsEntry(
                entryObj!!.id,
                mainObjId,
                valueChecked,
                noteV
            )
        } else {
            localRepo.addStatsEntry(
                mainObjId,
                valueChecked,
                if (formattedDateValue == null) {
                    "${Date().time}"
                } else {
                    val now = Date()
                    formattedDateValue!!.hours = now.hours
                    formattedDateValue!!.minutes = now.minutes
                    formattedDateValue!!.seconds = now.seconds
                    "${formattedDateValue!!.time}"
                },
                noteV
            )
        }
    }

    fun deleteObj() {
        if (entryObj != null && isEdit) {
            mainObj
            localRepo.deleteSmallObj(mainObjId, entryObj.id, isExercise)
        }
    }
}