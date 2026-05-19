package com.yurhel.alex.afit.ui.screen_main.upbar

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun ScoreTextItem(
    title: String,
    descriptions: List<Pair<String, Color?>>,
    modifier: Modifier
) {
    OutlinedCard(modifier = modifier.fillMaxWidth()) {
        Spacer(Modifier.height(15.dp))
        Text(
            text = title,
            modifier = Modifier
                .padding(horizontal = 15.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium
        )
        descriptions.forEach {
            Spacer(Modifier.height(10.dp))
            Text(
                text = it.first,
                modifier = Modifier
                    .padding(horizontal = 15.dp)
                    .fillMaxWidth(),
                color = it.second ?: Color.Unspecified,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Spacer(Modifier.height(15.dp))
    }
}