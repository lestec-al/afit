package com.yurhel.alex.afit.ui.screen_main.cards

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import com.yurhel.alex.afit.R
import com.yurhel.alex.afit.data.Obj

@Composable
fun CardGraphItem(
    onClick: (String, Int) -> Unit,
    obj: Obj,
    graphData: GraphDataLite,
    modifier: Modifier
) {
    Box(modifier = modifier) {
        Card(
            modifier = Modifier.fillMaxSize(),
            colors = CardColors(
                containerColor = Color(graphData.objColor),
                contentColor = Color.White,
                disabledContainerColor = Color.Gray,
                disabledContentColor = Color.White
            )
        ) {
            // Short info
            Row(
                modifier = Modifier
                    .padding(start = 10.dp, end = 10.dp, top = 10.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isTextTooLong = obj.name.length > 15
                val isObjColorWhite = graphData.objColor == Color.White.toArgb()
                Text(
                    text = if (isTextTooLong) "${obj.name.substring(0, 15)}..." else obj.name,
                    color = if (isObjColorWhite) Color.Black else Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
                Icon(
                    painter = painterResource(
                        if (obj.isExercise) R.drawable.ic_rv_exercise else R.drawable.ic_rv_stats
                    ),
                    contentDescription = null
                )
            }
            // Graph itself
            AndroidView(
                factory = { GraphView(it) },
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxSize(),
                update = { graphView ->
                    if (graphData.xyData.size > 1) {
                        val graphViewPort = graphView.viewport
                        graphViewPort.isXAxisBoundsManual = true
                        graphViewPort.setMinX(graphData.startDate.toDouble())
                        graphViewPort.setMaxX(graphData.endDate.toDouble())
                        graphViewPort.isYAxisBoundsManual = true
                        graphViewPort.setMinY(graphData.statsMin)
                        graphViewPort.setMaxY(graphData.statsMax)
                    }

                    val graphLabels = graphView.gridLabelRenderer
                    graphLabels.isVerticalLabelsVisible = false
                    graphLabels.isHorizontalLabelsVisible = false
                    graphLabels.isHighlightZeroLines = false
                    graphLabels.gridColor = graphData.objColor
                    graphLabels.setHumanRounding(false)
                    val series = LineGraphSeries(
                        if (graphData.xyData.size > 1) {
                            graphData.xyData.toTypedArray<DataPoint>()
                        } else {
                            arrayOf(DataPoint(0.0, 0.0), DataPoint(1.1, 0.0))
                        }
                    )
                    series.thickness = 7
                    series.color = Color.White.toArgb()
                    graphView.addSeries(series)
                }
            )
        }
        // This used for handle clicks. It needs to be rendered last
        // Need to override clicks from other objects
        Box(modifier = Modifier
            .fillMaxSize()
            .clip(CardDefaults.elevatedShape)
            .clickable {
                onClick(if (obj.isExercise) "ex_id" else "st_id", obj.id)
            }
        )
    }
}