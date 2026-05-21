package com.yurhel.alex.afit.ui.help.edit

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import com.yurhel.alex.afit.R
import androidx.compose.ui.res.stringResource

@Composable
fun EditBottomSheetTextField(
    idx: Int,
    obj: SettingsObj,
    vm: EditBottomSheetController,
    itemColor: Color,
    modifier: Modifier
) {
    OutlinedTextField(
        value = obj.value,
        onValueChange = { vm.updateTextSettings(obj.copy(value = it), idx) },
        modifier = modifier.fillMaxWidth(),
        label = { Text(stringResource(obj.label)) },
        keyboardOptions = if (vm.selectedCard != CardTypes.Exercise) KeyboardOptions.Default else {
            if (obj.label == R.string.name) {
                KeyboardOptions.Default
            } else {
                KeyboardOptions(keyboardType = KeyboardType.Decimal)
            }
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