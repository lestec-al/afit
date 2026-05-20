package com.yurhel.alex.afit.ui.screen_settings

import android.content.Context
import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class Setting(
    @StringRes val text: Int,
    @DrawableRes val iconId: Int,
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