package com.yurhel.alex.afit.ui.help

import android.text.format.DateFormat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.yurhel.alex.afit.R
import com.yurhel.alex.afit.data.Obj
import androidx.compose.ui.res.stringResource

@Composable
fun RowStats(
    onClick: (() -> Unit)? = null,
    it: Obj,
    isForCalendar: Boolean,
    modifier: Modifier
) {
    val context = LocalContext.current
    val isExercise = it.isExercise
    val textColor = if (isForCalendar) Color(it.color) else Color.Unspecified
    Row(
        modifier = modifier
            .clickable(
                enabled = onClick != null,
                onClick = {
                    if (onClick != null) {
                        onClick()
                    }
                }
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (isExercise) {
                it.mainValue.toInt().toString()
            } else {
                it.mainValue.toString()
            },
            color = textColor,
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(Modifier.width(8.dp))
        Column {
            val time = if (isExercise) {
                val timeSplit = it.time.split(':')
                val timeEdited = "${timeSplit[0]}${stringResource(R.string.min)} ${timeSplit[1]}${stringResource(R.string.sec)}"
                "$timeEdited (${it.date.formatMillsTime(context)})"
            } else {
                it.date.formatMillsTime(context)
            }
            Text(
                text = time,
                color = textColor,
                style = MaterialTheme.typography.labelSmall
            )
            if (it.longerValue != "") {
                Text(
                    text = if (it.longerValue.length > 25) {
                        it.longerValue.substring(0, 25) + "..."
                    } else {
                        it.longerValue
                    },
                    color = textColor,
                    style = MaterialTheme.typography.labelSmall
                )
            }
            if (isExercise && it.allWeights.isNotEmpty()) {
                Text(
                    text = if (it.allWeights.length > 25) {
                        it.allWeights.substring(0, 25) + "..."
                    } else {
                        it.allWeights
                    },
                    color = textColor,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
        Spacer(Modifier.weight(1f))
        Text(
            text = if (it.name != null) {
                if (it.name.length > 25) it.name.substring(0, 25) + "..." else it.name
            } else {
                "${DateFormat.format("EEE", it.date)}, ${it.date.formatMillsDate(context)}"
            },
            color = textColor
        )
    }
    HorizontalDivider()
}