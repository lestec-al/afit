package com.yurhel.alex.afit.ui.help

import android.content.Context
import android.text.format.DateFormat
import androidx.navigation.NavHostController
import java.util.Date

fun String.upperFirstChar() = this.replaceFirstChar { i ->
    if (i.isLowerCase()) i.uppercase() else i.toString()
}

fun Date.formatDate(context: Context): String = DateFormat
    .getMediumDateFormat(context)
    .format(this)

fun Long.formatMillsDate(context: Context): String = DateFormat
    .getMediumDateFormat(context)
    .format(this)

fun Long.formatMillsTime(context: Context): String = DateFormat
    .getTimeFormat(context)
    .format(this)

fun NavHostController.nav(screen: String) {
    this.navigate(screen) { popUpTo(0) { inclusive = true } }
}