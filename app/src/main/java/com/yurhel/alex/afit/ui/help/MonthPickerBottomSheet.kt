package com.yurhel.alex.afit.ui.help

import android.text.format.DateFormat
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yurhel.alex.afit.R
import java.util.Calendar
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthPickerBottomSheet(
    visible: Boolean,
    currentMonth: Int,
    currentYear: Int,
    currentDay: Int?,
    confirmButtonCLicked: (month: Int, year: Int, day: Int) -> Unit,
    cancelClicked: () -> Unit,
    resetClicked: () -> Unit,
    primaryColor: Color? = null
) {
    if (visible) {
        val months by remember {
            // Get all months (to viewModel ?)
            val c = Calendar.getInstance()
            c[Calendar.DAY_OF_MONTH] = 1
            val max = c.getActualMaximum(Calendar.MONTH)
            val months = mutableListOf<String>()
            var i = 0
            while (i <= max) {
                c[Calendar.MONTH] = i
                months.add(i, DateFormat.format("LLL", c).toString())
                i += 1
            }
            mutableStateOf(months.toList())
        }
        var month by remember { mutableStateOf(months[currentMonth]) }
        var year by remember { mutableIntStateOf(currentYear) }
        var day by remember { mutableStateOf("$currentDay") }
        val itemColor = primaryColor ?: MaterialTheme.colorScheme.onBackground

        ModalBottomSheet(
            onDismissRequest = cancelClicked,
            dragHandle = { Spacer(Modifier.height(10.dp)) }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Years
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { year-- },
                        modifier = Modifier.rotate(90f)
                    ) {
                        Icon(painterResource(R.drawable.ic_arrow_down), null)
                    }
                    Text(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        text = year.toString(),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = { year++ },
                        modifier = Modifier.rotate(90f)
                    ) {
                        Icon(painterResource(R.drawable.ic_arrow_up), null)
                    }
                }
                // Day
                if (currentDay != null) {
                    Spacer(Modifier.width(40.dp))
                    OutlinedTextField(
                        value = day,
                        onValueChange = {
                            day = it
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        label = {
                            Text(stringResource(R.string.day))
                        },
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                        modifier = Modifier.width(100.dp),
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
            }
            // Months
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth()
            ) {
                items(items = months) {
                    val isThisMonthChosen = month == it
                    Box(
                        modifier = Modifier
                            .border(
                                width = 1.dp,
                                color = if (isThisMonthChosen) {
                                    MaterialTheme.colorScheme.onBackground
                                } else {
                                    Color.Unspecified
                                },
                                shape = CircleShape
                            )
                            .clip(CircleShape)
                            .clickable {
                                month = it
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = it.upperFirstChar(),
                            modifier = Modifier.padding(8.dp),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            // Buttons
            LazyVerticalGrid(
                modifier = Modifier
                    .padding(start = 15.dp, end = 15.dp, top = 10.dp, bottom = 15.dp)
                    .fillMaxWidth(),
                columns = GridCells.Fixed(2)
            ) {
                items(count = 2) {
                    Button(
                        onClick = { if (it == 0) {
                            resetClicked()
                        } else {
                            val dayChecked = try { day.toInt() } catch (_: Exception) { 1 }
                            confirmButtonCLicked(months.indexOf(month), year, dayChecked)
                        } },
                        modifier = Modifier.padding(horizontal = 5.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = itemColor,
                            contentColor = BottomSheetDefaults.ContainerColor
                        )
                    ) {
                        Icon(
                            painter = painterResource(
                                if (it == 0) R.drawable.ic_auto else R.drawable.ic_ok
                            ),
                            contentDescription = if (it == 0) "reset" else "ok"
                        )
                    }
                }
            }
        }
    }
}