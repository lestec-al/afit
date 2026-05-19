package com.yurhel.alex.afit.data

data class SavedWorkout(
    val stage: String,
    val msg: String,
    val restTime: Int,
    val exId: Int,
    val repsList: String,
    val weightsList: String,
    val startTime: Long
)