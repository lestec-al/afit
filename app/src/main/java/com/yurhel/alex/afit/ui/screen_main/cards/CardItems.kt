package com.yurhel.alex.afit.ui.screen_main.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.yurhel.alex.afit.R
import com.yurhel.alex.afit.ui.help.EmptyBox
import androidx.compose.ui.res.stringResource
import com.yurhel.alex.afit.ui.screen_main.MainViewModel

@Composable
fun CardItems(
    onCard: (String, Int) -> Unit,
    modifier: Modifier,
    viewModel: MainViewModel
) {
    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        // Upper row with text & actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Info text
            Text(
                text = stringResource(R.string.ex_st_cards),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = MaterialTheme.typography.titleMedium
            )
            Row {
                // Change view
                IconButton(onClick = { viewModel.updateViewType(
                    when(viewModel.viewType) {
                        ViewType.Grid -> ViewType.Column
                        ViewType.Column -> ViewType.SmallGrid
                        ViewType.SmallGrid -> ViewType.Grid
                    }
                ) }) {
                    Icon(
                        painter = painterResource(when(viewModel.viewType) {
                            ViewType.Grid -> R.drawable.ic_list
                            ViewType.Column -> R.drawable.ic_small_grid
                            ViewType.SmallGrid -> R.drawable.ic_grid
                        }),
                        contentDescription = "change view"
                    )
                }
            }
        }
        // Show buttons to all cards
        when (viewModel.viewType) {
            ViewType.Column -> {
                viewModel.data.forEachIndexed { id, it ->
                    CardItem(
                        onClick = onCard,
                        obj = it,
                        graphData = viewModel.getCardData(it).first,
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .fillMaxWidth()
                            .height(50.dp)
                    )
                    if (id != viewModel.data.size - 1) {
                        Spacer(Modifier.height(4.dp))
                    }
                }
            }
            ViewType.Grid -> {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 4.dp),
                    verticalItemSpacing = 4.dp,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .padding(top = 1.dp)
                        .heightIn(max = (((viewModel.data.size / 2) * 104) + 500).dp)
                ) {
                    items(items = viewModel.data) {
                        CardGraphItem(
                            onClick = onCard,
                            obj = it,
                            graphData = viewModel.getCardData(it).first,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                        )
                    }
                }
            }
            ViewType.SmallGrid -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.heightIn(max = (((viewModel.data.size / 2) * 104) + 500).dp)
                ) {
                    items(items = viewModel.data) {
                        CardSmallItem(
                            onClick = onCard,
                            obj = it,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
        if (viewModel.data.isEmpty()) {
            EmptyBox(R.string.no_cards_info)
        }
        Spacer(Modifier.height(4.dp))
    }
}