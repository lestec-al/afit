package com.yurhel.alex.afit.ui.screen_training.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import com.yurhel.alex.afit.R
import com.yurhel.alex.afit.ui.screen_training.TrainingStage
import com.yurhel.alex.afit.ui.screen_training.TrainingViewModel
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    vm: TrainingViewModel,
    color: Color
) {
    val context = LocalContext.current
    TopAppBar(
        title = {
            Text(
                text = if (vm.stage == TrainingStage.DoExercise) {
                    "${stringResource(R.string.do_exercise)} ${vm.repsText} ${vm.mainObj.name}"
                } else {
                    stringResource(R.string.rest)
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            // Back button
            IconButton(
                onClick = { vm.exit(false, context) },
                colors = IconButtonDefaults.iconButtonColors(contentColor = color),
                content = { Icon(painterResource(R.drawable.ic_back), "Back") }
            )
        },
        actions = {
            // Save button
            IconButton(
                onClick = { vm.exit(true, context) },
                enabled = vm.repsList.isNotEmpty(),
                colors = IconButtonDefaults.iconButtonColors(contentColor = color),
                content = { Icon(painterResource(R.drawable.ic_save), "Save") }
            )
        }
    )
}