package com.yurhel.alex.afit.ui.screen_training

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.yurhel.alex.afit.ui.screen_training.components.EditTextSheet
import com.yurhel.alex.afit.ui.screen_training.components.MainButton
import com.yurhel.alex.afit.ui.screen_training.components.MaxWidthTextProgressIndicator
import com.yurhel.alex.afit.ui.screen_training.components.PlusMinusButtons
import com.yurhel.alex.afit.ui.screen_training.components.TopBar

@Composable
fun TrainingScreen(
    onBack: (isPastExerciseSaved:Boolean) -> Unit,
    vm: TrainingViewModel
) {
    if (vm.exerciseEndAndIsSaveResult != null) onBack(vm.exerciseEndAndIsSaveResult!!)
    val context = LocalContext.current
    BackHandler { vm.exit(false, context) }
    val color = Color(vm.mainObj.color)
    // Instead of 22 + 22 from padding (see on column)
    val widthDp = LocalConfiguration.current.screenWidthDp - 44

    if (vm.isEditSheetOpens) {
        EditTextSheet(vm, color)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { TopBar(vm, color) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 22.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (vm.withWeight) {
                // Weights reps row
                MaxWidthTextProgressIndicator(
                    repsResults = vm.weightsList,
                    color = color,
                    textColor = Color.White,
                    numberOfSets = vm.mainObj.sets,
                    widthDp = widthDp
                )
                // Weight button
                Button(
                    onClick = { vm.updateIsEditSheetOpens(true) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = color,
                        contentColor = Color.White
                    ),
                    content = { Text(vm.weight) }
                )
            } else {
                Spacer(Modifier.height(10.dp))
                Spacer(Modifier.height(10.dp))
            }
            MainButton(vm, color)
            PlusMinusButtons(vm, color)
            // Reps progress indicator
            MaxWidthTextProgressIndicator(
                repsResults = vm.repsList,
                color = color,
                textColor = Color.White,
                numberOfSets = vm.mainObj.sets,
                widthDp = widthDp
            )
        }
    }
}