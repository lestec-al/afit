package com.yurhel.alex.afit.ui.screen_main.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.yurhel.alex.afit.R
import com.yurhel.alex.afit.data.Obj

@Composable
fun CardSmallItem(
    onClick: (String, Int) -> Unit,
    obj: Obj,
    modifier: Modifier
) {
    Card(
        onClick = {
            onClick(if ((obj.isExercise)) "ex_id" else "st_id", obj.id)
        },
        modifier = modifier,
        colors = CardColors(
            containerColor = Color(obj.color),
            contentColor = Color.White,
            disabledContainerColor = Color.Gray,
            disabledContentColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = obj.name,
                color = if (obj.color == Color.White.toArgb()) Color.Black else Color.White,
                style = MaterialTheme.typography.titleMedium
            )
            Icon(
                painter = painterResource(
                    if ((obj.isExercise)) R.drawable.ic_rv_exercise else R.drawable.ic_rv_stats
                ),
                contentDescription = null
            )
        }
    }
}