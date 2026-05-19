package com.yurhel.alex.afit.ui.screen_training.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.yurhel.alex.afit.R
import com.yurhel.alex.afit.ui.screen_training.TrainingStage
import com.yurhel.alex.afit.ui.screen_training.TrainingViewModel
import androidx.compose.ui.res.stringResource

@Composable
fun MainButton(
    vm: TrainingViewModel,
    color: Color
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .fillMaxWidth()
    ) {
        if (vm.stage == TrainingStage.Rest) {
            CircularProgressIndicator(
                progress = { (vm.timeAtStartOfRest - vm.time) / vm.timeAtStartOfRest.toFloat() },
                modifier = Modifier.fillMaxSize(),
                color = color,
                strokeWidth = 6.dp,
                trackColor = color.copy(alpha = 0.2f),
                strokeCap = StrokeCap.Square,
                gapSize = 0.dp
            )
        }
        Button(
            onClick = {
                vm.mainButtonClick(context)
            },
            modifier = Modifier.fillMaxSize(),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (vm.stage == TrainingStage.Rest) Color.Transparent else color,
                contentColor = if (vm.stage == TrainingStage.Rest) color else Color.White
            )
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = if (vm.stage == TrainingStage.DoExercise) vm.repsText else "${vm.time}",
                    fontSize = TextUnit(80f, TextUnitType.Sp)
                )
                Box(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxSize(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Text(
                        text = stringResource(
                            if (vm.stage == TrainingStage.DoExercise) R.string.done else R.string.stop
                        ).uppercase(),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        }
    }
}