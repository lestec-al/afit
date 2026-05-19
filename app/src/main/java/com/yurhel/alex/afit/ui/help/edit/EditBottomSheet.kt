package com.yurhel.alex.afit.ui.help.edit

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yurhel.alex.afit.R
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun EditBottomSheet(
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    vm: EditBottomSheetViewModel
) {
    val context = LocalContext.current
    val itemColor = if (vm.editedObj != null) {
        Color(vm.editedObj!!.color)
    } else {
        MaterialTheme.colorScheme.onBackground
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        dragHandle = { Spacer(Modifier.height(10.dp)) }
    ) {
        // Title
        Text(
            text = stringResource(
                if (vm.editedObj != null) R.string.card_settings else R.string.add_card
            ),
            modifier = Modifier
                .padding(vertical = 5.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge
        )
        // Exercise or Stats card choose
        if (vm.editedObj == null) {
            LazyVerticalGrid(
                modifier = Modifier
                    .padding(horizontal = 15.dp, vertical = 5.dp)
                    .fillMaxWidth(),
                columns = GridCells.Fixed(2)
            ) {
                items(items = listOf(CardTypes.Exercise, CardTypes.Stats)) {
                    Card(
                        onClick = { vm.updateSelectedCard(it) },
                        modifier = Modifier
                            .padding(horizontal = 5.dp)
                            .fillMaxWidth(),
                        border = if (vm.selectedCard != it) null else {
                            BorderStroke(
                                width = 3.dp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(5.dp)
                                .fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                painter = painterResource(
                                    when (it) {
                                        CardTypes.Exercise -> R.drawable.ic_rv_exercise
                                        CardTypes.Stats -> R.drawable.ic_rv_stats
                                    }
                                ),
                                contentDescription = null
                            )
                            Text(
                                text = stringResource(
                                    when (it) {
                                        CardTypes.Exercise -> R.string.exercise_card
                                        CardTypes.Stats -> R.string.statistic_card
                                    }
                                ).uppercase(),
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
        // Name field ('0' idx for settings !)
        EditBottomSheetTextField(
            idx = 0,
            obj = vm.textSettings[0],
            vm = vm,
            itemColor = itemColor,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 5.dp)
        )
        // Other settings
        if (vm.selectedCard == CardTypes.Exercise) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.padding(horizontal = 15.dp)
            ) {
                itemsIndexed(items = vm.textSettings.subList(1, 4)) { idx, obj ->
                    EditBottomSheetTextField(idx+1, obj, vm, itemColor, Modifier.padding(5.dp))
                }
            }
        }
        // Color picker
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 5.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.color),
                    modifier = Modifier.padding(10.dp),
                    style = MaterialTheme.typography.titleMedium
                )
                OutlinedButton(
                    onClick = vm::createRandomColor,
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = BottomSheetDefaults.ContainerColor,
                        contentColor = itemColor
                    ),
                    border = BorderStroke(width = 1.dp, color = itemColor)
                ) {
                    Icon(painterResource(R.drawable.ic_random_color), null)
                }
            }
            Slider(
                value = vm.redColor.toFloat(),
                onValueChange = { vm.updateColor('r', it) },
                valueRange = 0f..255f,
                colors = SliderDefaults.colors(
                    thumbColor = vm.getWholeColor(),
                    activeTrackColor = vm.getWholeColor()
                )
            )
            Slider(
                value = vm.greenColor.toFloat(),
                onValueChange = { vm.updateColor('g', it) },
                valueRange = 0f..255f,
                colors = SliderDefaults.colors(
                    thumbColor = vm.getWholeColor(),
                    activeTrackColor = vm.getWholeColor()
                )
            )
            Slider(
                value = vm.blueColor.toFloat(),
                onValueChange = { vm.updateColor('b', it) },
                valueRange = 0f..255f,
                colors = SliderDefaults.colors(
                    thumbColor = vm.getWholeColor(),
                    activeTrackColor = vm.getWholeColor()
                )
            )
        }
        // Weight switcher
        if (vm.editedObj != null && vm.isExercise == true) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 5.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.show_weight),
                    modifier = Modifier.padding(vertical = 10.dp),
                    style = MaterialTheme.typography.titleMedium
                )
                Switch(
                    checked = vm.withWeight,
                    onCheckedChange = vm::weightSwitch,
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = itemColor,
                        checkedThumbColor = BottomSheetDefaults.ContainerColor
                    )
                )
            }
        }
        // Buttons (save & delete)
        LazyVerticalGrid(
            modifier = Modifier
                .padding(start = 15.dp, end = 15.dp, top = 10.dp, bottom = 15.dp)
                .fillMaxWidth(),
            columns = GridCells.Fixed(if (vm.editedObj != null) 2 else 1)
        ) {
            // Delete button
            if (vm.editedObj != null) item {
                Card(
                    modifier = Modifier
                        .padding(horizontal = 5.dp)
                        .fillMaxWidth()
                        .height(45.dp)
                        .combinedClickable(
                            onClick = {
                                Toast.makeText(
                                    context,
                                    R.string.delete_card_info,
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            onLongClick = {
                                vm.deleteObj()
                                onDelete()
                            }
                        ),
                    shape = ButtonDefaults.shape,
                    colors = CardDefaults.cardColors(
                        containerColor = itemColor,
                        contentColor = BottomSheetDefaults.ContainerColor
                    )
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(painterResource(R.drawable.ic_delete), "delete")
                    }
                }
            }
            // Save button
            item {
                Button(
                    onClick = {
                        vm.saveAllSettingsToDB()
                        onSave()
                    },
                    modifier = Modifier
                        .padding(horizontal = 5.dp)
                        .fillMaxWidth()
                        .height(45.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = itemColor,
                        contentColor = BottomSheetDefaults.ContainerColor
                    )
                ) {
                    Icon(painterResource(R.drawable.ic_save), "save")
                }
            }
        }
    }
}