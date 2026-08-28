package com.yurhel.alex.afit.ui.help.edit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import com.yurhel.alex.afit.R
import com.yurhel.alex.afit.data.LocalRepo
import com.yurhel.alex.afit.data.Obj
import kotlin.math.max
import kotlin.random.Random

class EditBottomSheetController(
    private val localRepo: LocalRepo,
    val isExercise: Boolean?,
    private val objId: Int?
) {
    var editedObj by mutableStateOf<Obj?>(null)
        private set

    var withWeight by mutableStateOf(false)
        private set

    var selectedCard by mutableStateOf(CardTypes.Exercise)
        private set
    fun updateSelectedCard(value: CardTypes) {
        selectedCard = value
    }

    init {
        if (objId != null && isExercise != null) {
            editedObj = localRepo.getOneMainObj(objId, isExercise)
            updateSelectedCard(if (isExercise) CardTypes.Exercise else CardTypes.Stats)
            withWeight = localRepo.getIsWeightShowForMainStat(objId)
        }
    }

    private fun check(value: Any?, setValue: () -> String) = when (value) {
        null -> setValue()
        else -> value.toString()
    }
    var textSettings = mutableStateListOf(
        SettingsObj(R.string.name, check(editedObj?.name) { "Example" }),
        SettingsObj(R.string.rest, check(editedObj?.rest) { "120" }),
        SettingsObj(R.string.sets, check(editedObj?.sets) { "5" }),
        SettingsObj(R.string.weight, check(editedObj?.weight?.toInt()) { "0" })
    )
        private set
    fun updateTextSettings(newObj: SettingsObj, pos: Int) {
        textSettings[pos] = newObj
    }

    private var initialColor = editedObj?.color ?: Color(
        Random.nextInt(256), Random.nextInt(256), Random.nextInt(256)
    ).toArgb()
    var redColor by mutableIntStateOf(initialColor.red)
        private set
    var greenColor by mutableIntStateOf(initialColor.green)
        private set
    var blueColor by mutableIntStateOf(initialColor.blue)
        private set
    fun updateColor(colorType: Char, color: Float) {
        when (colorType) {
            'r' -> redColor = color.toInt()
            'g' -> greenColor = color.toInt()
            'b' -> blueColor = color.toInt()
        }
    }
    fun getWholeColor() = Color(redColor, greenColor, blueColor)
    fun createRandomColor() {
        redColor = Random.nextInt(256)
        greenColor = Random.nextInt(256)
        blueColor = Random.nextInt(256)
    }

    private fun tryParseIntDouble(text: String, isInteger: Boolean): Any {
        // Don't remember why this is here, but decided do not delete
        if (text.contains("-") || text.contains("+")) {
            return if (isInteger) 1 else 0.0
        }
        // Here is known & useful part
        return try {
            if (isInteger) text.toInt() else text.toDouble()
        } catch (_: Exception) {
            if (isInteger) 1 else 0.0
        }
    }

    fun saveAllSettingsToDB() {
        val nameValue = textSettings.find { it.label == R.string.name }!!.value
        val name = if (nameValue == "") "Example" else nameValue
        val color = getWholeColor().toArgb()

        if (editedObj != null) {
            localRepo.setWeights(objId!!, withWeight)
        }

        if (selectedCard == CardTypes.Stats) {
            if (editedObj != null) {
                localRepo.updateStats(name, objId!!, color)
            } else {
                localRepo.addStats(name, color)
            }
        } else {
            val restValue = textSettings.find { it.label == R.string.rest }!!.value
            val setsValue = textSettings.find { it.label == R.string.sets }!!.value
            val weightValue = textSettings.find { it.label == R.string.weight }!!.value
            val rest = tryParseIntDouble(restValue, true) as Int
            val sets = tryParseIntDouble(setsValue, true) as Int
            val weight = tryParseIntDouble(weightValue, false) as Double

            if (editedObj != null) {
                localRepo.updateExercise(
                    name,
                    objId!!,
                    max(rest.toDouble(), 1.0).toInt(),
                    editedObj!!.reps,
                    max(sets.toDouble(), 1.0).toInt(),
                    weight,
                    color
                )
            } else {
                localRepo.addExercise(
                    name,
                    max(rest.toDouble(), 1.0).toInt(),
                    max(sets.toDouble(), 1.0).toInt(),
                    weight,
                    color
                )
            }
        }
    }

    fun deleteObj() {
        if (editedObj != null && isExercise != null) {
            localRepo.deleteObj(objId!!, isExercise)
        }
    }

    fun weightSwitch(newValue: Boolean) {
        withWeight = newValue
    }
}