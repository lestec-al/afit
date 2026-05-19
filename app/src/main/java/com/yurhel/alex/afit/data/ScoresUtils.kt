package com.yurhel.alex.afit.data

import java.util.Calendar

data class ScoresObj(
    val allPoints: Int,
    val weekPoints: Int,
    val weekDate: Long
)

private fun countScores(localRepo: LocalRepo): ScoresObj {
    // Init scores
    var allTimeMin = 0
    var allTimeSec = 0
    var lastWeekMin = 0
    var lastWeekSec = 0
    val objDate = Calendar.getInstance()
    val nowWeek = objDate.get(Calendar.WEEK_OF_YEAR)
    val nowYear = objDate.get(Calendar.YEAR)
    // Get & loop data
    localRepo.getAllExercises()
        .sortedBy { it.date }
        .forEach {
            objDate.timeInMillis = it.date
            // Get score
            val t = it.time.split(":".toRegex())
            allTimeMin += t[0].toInt()
            allTimeSec += t[1].toInt()
            objDate.timeInMillis = it.date
            if (nowWeek == objDate[Calendar.WEEK_OF_YEAR] && nowYear == objDate[Calendar.YEAR]) {
                lastWeekMin += t[0].toInt()
                lastWeekSec += t[1].toInt()
            }
        }
    return ScoresObj(
        allPoints = allTimeMin + (allTimeSec / 60) + (if (allTimeSec % 60 > 0) 1 else 0),
        weekPoints = lastWeekMin + (lastWeekSec / 60) + (if (lastWeekSec % 60 > 0) 1 else 0),
        weekDate = System.currentTimeMillis()
    )
}

fun getScores(localRepo: LocalRepo): ScoresObj {
    val savedScores = localRepo.getScores()
    // Check saved score
    val isScoreExistAndWeekNotChanged = if (savedScores == null) false else {
        val now = Calendar.getInstance()
        val c = Calendar.getInstance()
        c.timeInMillis = savedScores.weekDate
        c.get(Calendar.WEEK_OF_YEAR) == now.get(Calendar.WEEK_OF_YEAR) &&
                c.get(Calendar.YEAR) == now.get(Calendar.YEAR)
    }
    // Get existed or recount scores
    return if (isScoreExistAndWeekNotChanged) {
        savedScores
    } else {
        val newScores = countScores(localRepo)
        localRepo.setScores(newScores)
        newScores
    }
}

fun getAllScoreEmoji() = "\uD83C\uDFC6"
fun getWeekScoreEmoji() = "\uD83D\uDDD3"