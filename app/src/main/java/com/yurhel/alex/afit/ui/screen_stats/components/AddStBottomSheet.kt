package com.yurhel.alex.afit.ui.screen_stats.components

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yurhel.alex.afit.R
import com.yurhel.alex.afit.ui.help.formatDate
import java.util.Date
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AddStBottomSheet(
    onDismiss: () -> Unit,
    onAction: () -> Unit,
    vm: AddStBottomSheetController
) {
    LaunchedEffect(Unit) {
        // Initial data update
        // Here, because when passed to viewModel replacer init - don't work properly
        vm.updateDateValue(
            vm.localeFormatter.format(
                if (vm.isEdit && vm.entryObj != null) Date(vm.entryObj.date) else Date()
            )
        )
    }
    val context = LocalContext.current
    val itemColor = Color(vm.mainObj.color)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        dragHandle = { Spacer(Modifier.height(10.dp)) }
    ) {
        // Title
        Text(
            text = stringResource(if (vm.isEdit) R.string.edit_st else R.string.add_st),
            modifier = Modifier
                .padding(vertical = 5.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge
        )
        // Text settings
        vm.textSettings.forEachIndexed { idx, obj ->
            OutlinedTextField(
                value = obj.value,
                onValueChange = { vm.updateTextSettings(obj.copy(value = it), idx) },
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 5.dp)
                    .fillMaxWidth(),
                label = { Text(stringResource(obj.label)) },
                keyboardOptions = if (obj.label == R.string.note) {
                    KeyboardOptions.Default
                } else {
                    KeyboardOptions(keyboardType = KeyboardType.Decimal)
                },
                shape = CardDefaults.shape,
                colors = OutlinedTextFieldDefaults.colors(
                    cursorColor = itemColor,
                    focusedBorderColor = itemColor,
                    focusedLabelColor = itemColor,
                    selectionColors = TextSelectionColors(
                        handleColor = itemColor,
                        backgroundColor = itemColor.copy(alpha = 0.4f)
                    )
                )
            )
        }
        // Date
        OutlinedTextField(
            value = vm.dateValue,
            onValueChange = vm::updateDateValue,
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 5.dp)
                .fillMaxWidth(),
            enabled = !vm.isEdit,
            label = { Text(text = vm.formattedDateValue?.formatDate(context) ?: stringResource(R.string.date)) },
            placeholder = { Text(text = vm.localPattern) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape = CardDefaults.shape,
            colors = OutlinedTextFieldDefaults.colors(
                cursorColor = itemColor,
                focusedBorderColor = itemColor,
                focusedLabelColor = itemColor,
                selectionColors = TextSelectionColors(
                    handleColor = itemColor,
                    backgroundColor = itemColor.copy(alpha = 0.4f)
                )
            )
        )
        // Buttons (save & delete)
        LazyVerticalGrid(
            modifier = Modifier
                .padding(15.dp)
                .fillMaxWidth(),
            columns = GridCells.Fixed(if (vm.isEdit) 2 else 1)
        ) {
            // Delete button
            if (vm.isEdit) item {
                Card(
                    modifier = Modifier
                        .padding(horizontal = 5.dp)
                        .fillMaxWidth()
                        .height(45.dp)
                        .combinedClickable(
                            onClick = {
                                Toast.makeText(
                                    context,
                                    R.string.delete_entry_info,
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            onLongClick = {
                                vm.deleteObj()
                                onAction()
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
                        onAction()
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