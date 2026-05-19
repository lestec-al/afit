package com.yurhel.alex.afit.ui.screen_training.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun MaxWidthTextProgressIndicator(
    repsResults: List<String>,
    color: Color,
    textColor: Color,
    numberOfSets: Int,
    widthDp: Int,
    height: Dp = 30.dp
) {
    val itemWidth = (widthDp / numberOfSets).dp
    val progress = repsResults.size / numberOfSets.toFloat()

    Box {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .clip(CircleShape)
                .height(height)
                .width(widthDp.dp),
            color = color.copy(alpha = 0.6f),
            trackColor = color.copy(alpha = 0.1f),
            strokeCap = StrokeCap.Square,
            gapSize = 0.dp,
            drawStopIndicator = {}
        )
        Row {
            repsResults.forEach {
                Box(
                    modifier = Modifier
                        .height(height)
                        .width(itemWidth),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = it,
                        color = textColor,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}