package com.yurhel.alex.afit.ui.screen_training.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.yurhel.alex.afit.R
import com.yurhel.alex.afit.ui.screen_training.TrainingViewModel
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTextSheet(
    vm: TrainingViewModel,
    color: Color
) {
    var textValue by remember { mutableStateOf(vm.weight) }

    ModalBottomSheet(
        onDismissRequest = { vm.updateIsEditSheetOpens(false) },
        dragHandle = { Spacer(Modifier.height(10.dp)) }
    ) {
        // Edit text
        OutlinedTextField(
            value = textValue,
            onValueChange = { textValue = it },
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 5.dp)
                .fillMaxWidth(),
            label = { Text(stringResource(R.string.weight)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape = CardDefaults.shape,
            colors = OutlinedTextFieldDefaults.colors(
                cursorColor = color,
                focusedBorderColor = color,
                focusedLabelColor = color,
                selectionColors = TextSelectionColors(
                    handleColor = color,
                    backgroundColor = color.copy(alpha = 0.4f)
                )
            )
        )
        // Button
        Button(
            onClick = {
                vm.saveWeight(textValue)
                vm.updateIsEditSheetOpens(false)
            },
            modifier = Modifier
                .padding(start = 20.dp, end = 20.dp, top = 5.dp, bottom = 15.dp)
                .fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = color,
                contentColor = BottomSheetDefaults.ContainerColor
            ),
            content = { Icon(painterResource(R.drawable.ic_save), "save") }
        )
    }
}