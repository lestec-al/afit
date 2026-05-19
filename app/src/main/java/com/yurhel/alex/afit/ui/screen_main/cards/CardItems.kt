package com.yurhel.alex.afit.ui.screen_main.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
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
import com.yurhel.alex.afit.ui.help.edit.EditBottomSheet
import com.yurhel.alex.afit.ui.help.edit.EditBottomSheetViewModel
import androidx.compose.ui.res.stringResource

@Composable
fun CardItems(
    onCard: (String, Int) -> Unit,
    modifier: Modifier,
    viewModel: CardItemsViewModel
) {
    if (viewModel.editBottomSheetOpen) {
        EditBottomSheet(
            onDismiss = { viewModel.updateEditBottomSheetOpen(false) },
            onSave = {
                viewModel.updateData()
                viewModel.updateEditBottomSheetOpen(false)
            },
            onDelete = {},
            vm = EditBottomSheetViewModel(viewModel.localRepo, null, null)
        )
    }
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
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.titleMedium
            )
            Row {
                // Change view
                IconButton(onClick = { viewModel.updateViewType(
                    when(viewModel.viewType) {
                        ViewType.Grid -> ViewType.Column
                        ViewType.Column -> ViewType.Grid
                    }
                ) }) {
                    Icon(
                        painter = painterResource(when(viewModel.viewType) {
                            ViewType.Grid -> R.drawable.ic_list
                            ViewType.Column -> R.drawable.ic_grid
                        }),
                        contentDescription = "change view"
                    )
                }
                // Add card button
                IconButton(onClick = { viewModel.updateEditBottomSheetOpen(true) }) {
                    Icon(painterResource(R.drawable.ic_add), stringResource(R.string.add_card))
                }
            }
        }
        // Show buttons to all cards
        when (viewModel.viewType) {
            ViewType.Column -> {
                viewModel.data.forEach {
                    CardItem(
                        onClick = onCard,
                        obj = it,
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .fillMaxWidth()
                    )
                    Spacer(Modifier.height(2.dp))
                }
            }
            ViewType.Grid -> {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 4.dp),
                    verticalItemSpacing = 4.dp,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.heightIn(max = (((viewModel.data.size / 2) * 104) + 500).dp)
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
        }
        if (viewModel.data.isEmpty()) {
            EmptyBox()
        }
        Spacer(Modifier.height(4.dp))
    }
}