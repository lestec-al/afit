package com.yurhel.alex.afit.ui.screen_training.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.yurhel.alex.afit.R
import com.yurhel.alex.afit.ui.screen_training.TrainingStage
import com.yurhel.alex.afit.ui.screen_training.TrainingViewModel

@Composable
fun PlusMinusButtons(
    vm: TrainingViewModel,
    color: Color
) {
    // Row with - & + buttons
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = vm::minusButtonClick,
            enabled = vm.stage == TrainingStage.DoExercise,
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = color
            )
        ) {
            Icon(painterResource(R.drawable.ic_t_minus), "Minus")
        }
        IconButton(
            onClick = vm::plusButtonClick,
            enabled = vm.stage == TrainingStage.DoExercise,
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = color
            )
        ) {
            Icon(painterResource(R.drawable.ic_t_plus), "Plus")
        }
    }
}