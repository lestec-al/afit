package com.yurhel.alex.afit.ui.screen_main.calendar

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yurhel.alex.afit.R
import com.yurhel.alex.afit.ui.help.EmptyBox
import com.yurhel.alex.afit.ui.help.RowStats
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayBottomSheet(
    onDismissRequest: () -> Unit,
    visible: Boolean,
    vm: CalendarCardViewModel
) {
    if (visible) {
        val context = LocalContext.current
        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            dragHandle = { Spacer(Modifier.height(10.dp)) }
        ) {
            // Date
            Text(
                text = DateFormat.getLongDateFormat(context).format(Date(vm.timeForDay)),
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            // Activity time
            if (vm.trainingTimeText != "") {
                Text(
                    text = vm.trainingTimeText,
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
            // Stats
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                colors = CardDefaults.cardColors()
            ) {
                vm.dataForDay.forEach {
                    RowStats(
                        it = it,
                        isForCalendar = true,
                        modifier = Modifier
                            .height(60.dp)
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp)
                    )
                }
                if (vm.dataForDay.isEmpty()) {
                    EmptyBox(R.string.no_day_stats_info)
                }
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}