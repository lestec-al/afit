package com.yurhel.alex.afit.ui.screen_settings

import android.content.Context
import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult

data class Setting(
    val text: Int,
    val iconId: Int,
    val action: (
        context: Context,
        launcherExport: ManagedActivityResultLauncher<Intent, ActivityResult>?,
        launcherAuth: ManagedActivityResultLauncher<Intent, ActivityResult>?
    ) -> Unit
)

data class Hidden(
    val statsId: Int,
    val isExercise: Boolean
)

val languages = listOf(
    Pair("English", "en"),
    Pair("Беларуская", "be"),
    Pair("Български", "bg"),
    Pair("Русский", "ru"),
    Pair("Українська", "uk")
)