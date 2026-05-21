package com.yurhel.alex.afit.ui.screen_stats.components

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.yurhel.alex.afit.R
import com.yurhel.alex.afit.data.LocalRepo
import com.yurhel.alex.afit.data.Obj
import com.yurhel.alex.afit.ui.help.formatMillsDate
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ShowExBottomSheet(
    onDismiss: () -> Unit,
    onAction: () -> Unit,
    isShowCongratsText: Boolean,
    localRepo: LocalRepo,
    mainObjId: Int,
    mainObjColor: Color,
    entryObj: Obj
) {
    val context = LocalContext.current
    val disabledTextColor = TextFieldDefaults.colors().disabledTextColor
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        dragHandle = { Spacer(Modifier.height(10.dp)) }
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .fillMaxWidth()
        ) {
            if (isShowCongratsText) {
                // Congratulations emoji & text
                Text(
                    text = "\uD83C\uDFC5",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontSize = TextUnit(70f, TextUnitType.Sp)
                )
                Text(
                    text = stringResource(R.string.congratulations),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineLarge
                )
                Spacer(Modifier.height(25.dp))
            }
            // Date
            Text(
                text = entryObj.date.formatMillsDate(context),
                modifier = Modifier
                    .padding(bottom = 5.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = disabledTextColor
            )
            // Title
            val youCompletedT = stringResource(R.string.you_completed)
            val repsInT = stringResource(R.string.reps_in)
            Text(
                text = "$youCompletedT ${entryObj.mainValue.toInt()} $repsInT ${entryObj.time}",
                modifier = Modifier
                    .padding(bottom = 5.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge
            )
            // Details
            Text(
                text = "${stringResource(R.string.details)}: ${entryObj.longerValue}",
                modifier = Modifier
                    .padding(bottom = 5.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = disabledTextColor
            )
            if (entryObj.allWeights != "") {
                Text(
                    text = "${stringResource(R.string.weight)}: ${entryObj.allWeights}",
                    modifier = Modifier
                        .padding(bottom = 5.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = disabledTextColor
                )
            }
            if (!isShowCongratsText) {
                // Delete button
                Card(
                    modifier = Modifier
                        .padding(horizontal = 10.dp, vertical = 15.dp)
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
                                localRepo.deleteSmallObj(mainObjId, entryObj.id, true)
                                onAction()
                            }
                        ),
                    shape = ButtonDefaults.shape,
                    colors = CardDefaults.cardColors(
                        containerColor = mainObjColor,
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
            } else {
                // Empty space
                Spacer(Modifier.height(25.dp))
            }
        }
    }
}