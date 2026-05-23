package com.yurhel.alex.afit.ui.screen_main.upbar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun ScoreItem(
    title: String,
    iconId: Int,
    descriptions: List<String>,
    currentLevel: String? = null,
    nextLevel: String? = null,
    progress: Float? = null,
    modifier: Modifier
) {
    OutlinedCard(modifier = modifier.fillMaxWidth()) {
        Spacer(Modifier.height(15.dp))
        if (currentLevel != null && nextLevel != null && progress != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currentLevel,
                    modifier = Modifier.padding(horizontal = 15.dp),
                    style = MaterialTheme.typography.titleMedium
                )
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .height(15.dp)
                        .weight(1f),
                    strokeCap = StrokeCap.Square,
                    gapSize = 0.dp,
                    drawStopIndicator = {}
                )
                Text(
                    text = nextLevel,
                    modifier = Modifier.padding(horizontal = 15.dp),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 15.dp)
                .fillMaxWidth()
        ) {
            ScoreIcon(iconId = iconId, size = 24.dp)
            Text(
                text = title,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium
            )
        }
        descriptions.forEach {
            Spacer(Modifier.height(10.dp))
            Text(
                text = it,
                modifier = Modifier
                    .padding(horizontal = 15.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Spacer(Modifier.height(15.dp))
    }
}