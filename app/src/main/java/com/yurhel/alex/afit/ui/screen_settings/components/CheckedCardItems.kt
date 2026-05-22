package com.yurhel.alex.afit.ui.screen_settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.yurhel.alex.afit.data.Obj
import com.yurhel.alex.afit.ui.screen_settings.Hidden

@Composable
fun CheckedCardItems(
    onClick: (Boolean, Int, Boolean) -> Unit,
    hiddens: List<Hidden>,
    items: List<Obj>
) {
    items.forEachIndexed { idx, obj ->
        if (idx != 0) HorizontalDivider(Modifier.padding(horizontal = 10.dp))
        Row(
            modifier = Modifier
                .padding(start = 20.dp, end = 10.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (obj.name.length > 25) obj.name.substring(0, 26) else obj.name,
                color = Color(obj.color),
                style = MaterialTheme.typography.bodyLarge
            )
            Checkbox(
                checked = hiddens.find { it.statsId == obj.id && it.isExercise == obj.isExercise } == null,
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.onBackground,
                    checkmarkColor = MaterialTheme.colorScheme.background
                ),
                onCheckedChange = {
                    onClick(obj.isExercise, obj.id, it)
                }
            )
        }
    }
    Spacer(Modifier.height(5.dp))
}