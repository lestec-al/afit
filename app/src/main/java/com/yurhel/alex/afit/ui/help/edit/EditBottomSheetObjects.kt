package com.yurhel.alex.afit.ui.help.edit

import androidx.annotation.StringRes

data class SettingsObj(
    @StringRes val label: Int,
    val value: String
)

enum class CardTypes { Exercise, Stats }