package com.yurhel.alex.afit.ui.screen_main.upbar

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ScoreIcon(
    iconId: Int,
    size: Dp = 32.dp,
    paddingEnd: Dp = 5.dp
) {
    Image(
        painter = painterResource(iconId),
        contentDescription = null,
        modifier = Modifier
            .padding(end = paddingEnd)
            .size(size)
    )
}