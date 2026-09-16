package com.yurhel.alex.afit.ui.help

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign

@Composable
fun EmptyBox(stringId: Int) {
    Box(
        modifier = Modifier
            .padding(50.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(stringId),
            textAlign = TextAlign.Center
        )
    }
}