package com.yurhel.alex.afit.ui.screen_stats

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.LabelFormatter
import com.jjoe64.graphview.Viewport
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import com.yurhel.alex.afit.R
import com.yurhel.alex.afit.ui.help.EmptyBox
import com.yurhel.alex.afit.ui.help.RowStats
import com.yurhel.alex.afit.ui.help.formatMillsDate
import com.yurhel.alex.afit.ui.screen_stats.components.SetupBottomSheets
import kotlinx.coroutines.launch
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    onBack: () -> Unit,
    onWorkout: () -> Unit,
    vm: StatsViewModel
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    @SuppressLint("ConfigurationScreenWidthHeight")
    val statsHeight = LocalConfiguration.current.screenHeightDp / 3

    val color = vm.graphData?.objColor?.let { Color(it) } ?: Color.Unspecified
    val onBackgroundColor = MaterialTheme.colorScheme.onBackground.toArgb()
    val allText = stringResource(R.string.all)
    val isExercise = vm.graphData?.isExercise == true

    SetupBottomSheets(
        onBack = onBack,
        vm = vm,
        context = context,
        color = color
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = vm.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        colors = IconButtonDefaults.iconButtonColors(contentColor = color)
                    ) {
                        Icon(painterResource(R.drawable.ic_back), "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { vm.updateEditBottomSheetOpen(true) },
                        colors = IconButtonDefaults.iconButtonColors(contentColor = color)
                    ) {
                        Icon(painterResource(R.drawable.ic_settings), "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (isExercise) onWorkout() else {
                        vm.updateIsEditValue(false, vm.graphData?.listData?.firstOrNull())
                        vm.updateAddBottomSheetOpen(true)
                    }
                },
                icon = {
                    Icon(
                        painter = painterResource(
                            if (isExercise) R.drawable.ic_start_workout else R.drawable.ic_add
                        ),
                        contentDescription = null
                    )
                },
                text = {
                    Text(text = stringResource(
                        if (isExercise) R.string.start_workout else R.string.add_st
                    ))
                },
                containerColor = color,
                contentColor = Color.White
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            // Graph
            ElevatedCard(modifier = Modifier.padding(horizontal = 4.dp)) {
                // Top row
                LazyRow(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Short info
                    item {
                        Spacer(Modifier.width(12.dp))
                        Icon(painterResource(R.drawable.ic_st_entries), null)
                        Text(text = vm.graphData?.size ?: "")
                        Spacer(Modifier.width(8.dp))
                        Icon(painterResource(R.drawable.ic_st_up), null)
                        Text(text = vm.graphData?.heightValue ?: "")
                        if (!isExercise) {
                            Icon(
                                painter = painterResource(R.drawable.ic_st_down),
                                contentDescription = null,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                        Text(text = vm.graphData?.smallValue ?: "")
                    }
                    // Sort buttons
                    item {
                        if (isExercise) {
                            TextButton(
                                onClick = vm::updateNumberOfSets,
                                colors = ButtonDefaults.textButtonColors(contentColor = color)
                            ) {
                                Text(text = "${stringResource(R.string.sets)}: ${
                                    if (vm.numberOfSets == 0) allText else vm.numberOfSets
                                }")
                            }
                            TextButton(
                                onClick = vm::updateWeightFilter,
                                colors = ButtonDefaults.textButtonColors(contentColor = color)
                            ) {
                                Text(text = "${stringResource(R.string.weight)}: ${
                                    when (vm.weightFilter) {
                                        Filter.All -> allText
                                        Filter.With -> "✓"
                                        Filter.Without -> "✗"
                                    }
                                }")
                            }
                        } else {
                            TextButton(
                                onClick = vm::updateNoteFilter,
                                colors = ButtonDefaults.textButtonColors(contentColor = color)
                            ) {
                                Text(text = "${stringResource(R.string.note)}: ${
                                    when (vm.noteFilter) {
                                        Filter.All -> allText
                                        Filter.With -> "✓"
                                        Filter.Without -> "✗"
                                    }
                                }")
                            }
                        }
                    }
                }
                // Graph itself
                if (!vm.isGraphHidden) {
                    key(vm.graphData) {
                        AndroidView(
                            factory = {
                                GraphView(it)
                            },
                            modifier = Modifier
                                .height(statsHeight.dp)
                                .fillMaxWidth()
                                .padding(4.dp),
                            update = { graphView ->
                                val graphLabels = graphView.gridLabelRenderer
                                graphLabels.isVerticalLabelsVisible = false
                                graphLabels.isHorizontalLabelsVisible = false
                                graphLabels.isHighlightZeroLines = false

                                val graphDataSt = vm.graphData
                                if (graphDataSt != null && graphDataSt.xyData.size > 1) {
                                    val graphViewPort = graphView.viewport
                                    graphViewPort.isXAxisBoundsManual = true
                                    graphViewPort.setMinX(graphDataSt.startDate.toDouble())
                                    graphViewPort.setMaxX(graphDataSt.endDate.toDouble())
                                    graphViewPort.isYAxisBoundsManual = true
                                    graphViewPort.setMinY(graphDataSt.statsMin)
                                    graphViewPort.setMaxY(graphDataSt.statsMax)
                                    graphLabels.setHumanRounding(false)
                                    graphLabels.isVerticalLabelsVisible = true
                                    graphLabels.labelFormatter = object : LabelFormatter {
                                        @SuppressLint("DefaultLocale")
                                        override fun formatLabel(value: Double, isValueX: Boolean): String? {
                                            return if (isValueX) null else {
                                                if (isExercise) value.toInt().toString() else String.format("%1.1f", value)
                                            }
                                        }
                                        override fun setViewport(viewport: Viewport) {}
                                    }
                                    graphLabels.verticalLabelsColor = onBackgroundColor
                                    graphLabels.horizontalLabelsColor = onBackgroundColor
                                    graphLabels.gridColor = onBackgroundColor
                                    val series = LineGraphSeries(graphDataSt.xyData.toTypedArray<DataPoint>())
                                    series.thickness = 7
                                    series.color = graphDataSt.objColor
                                    series.setOnDataPointTapListener { _, dataPoint ->
                                        var pos = 0
                                        for (item in graphDataSt.listData) {
                                            if (item.date == dataPoint.x.toLong() && item.mainValue == dataPoint.y) break
                                            pos++
                                        }
                                        scope.launch {
                                            listState.animateScrollToItem(pos)
                                        }
                                    }
                                    graphView.addSeries(series)
                                }
                            }
                        )
                    }
                }
                // Date buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { vm.setIsDatePickerON(true, DateButtonType.Start) },
                        colors = ButtonDefaults.textButtonColors(contentColor = color),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = if (vm.graphData?.startDate == null) "" else {
                            vm.graphData!!.startDate.formatMillsDate(context)
                        })
                    }
                    IconButton(
                        onClick = { vm.flipGraphVisibility() },
                        colors = IconButtonDefaults.iconButtonColors(contentColor = color),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            painter = painterResource(
                                if (vm.isGraphHidden) R.drawable.ic_arrow_down else R.drawable.ic_arrow_up
                            ),
                            contentDescription = "show/hide graph"
                        )
                    }
                    TextButton(
                        onClick = { vm.setIsDatePickerON(true, DateButtonType.End) },
                        colors = ButtonDefaults.textButtonColors(contentColor = color),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = if (vm.graphData?.endDate != null) {
                            vm.graphData!!.endDate.formatMillsDate(context)
                        } else {
                            ""
                        })
                    }
                }
            }
            // List
            LazyColumn(
                state = listState,
                // Add extra bottom padding so the FAB doesn't hide the last item
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(items = vm.graphData?.listData ?: emptyList()) {
                    RowStats(
                        onClick = {
                            if (isExercise) {
                                vm.updateIsEditValue(true, it)
                                vm.updateShowExBottomSheetOpen(true)
                            } else {
                                vm.updateIsEditValue(true, it)
                                vm.updateAddBottomSheetOpen(true)
                            }
                        },
                        it = it,
                        isForCalendar = false,
                        modifier = Modifier
                            .height(60.dp)
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp)
                    )
                }
            }
            // Text for empty
            if (vm.graphData?.listData?.isEmpty() == true) EmptyBox(R.string.no_data_info)
        }
    }
}