package com.yurhel.alex.afit.ui.screen_main

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.yurhel.alex.afit.R
import com.yurhel.alex.afit.ui.screen_main.calendar.CalendarCard
import com.yurhel.alex.afit.ui.screen_main.calendar.CalendarCardViewModel
import com.yurhel.alex.afit.ui.screen_main.cards.CardItems
import com.yurhel.alex.afit.ui.screen_main.upbar.ScoreItem
import com.yurhel.alex.afit.ui.screen_main.upbar.UpSheet
import androidx.compose.ui.res.stringResource
import com.yurhel.alex.afit.ui.help.edit.EditBottomSheet
import com.yurhel.alex.afit.ui.help.edit.EditBottomSheetController
import com.yurhel.alex.afit.ui.screen_main.upbar.ScoreIcon
import kotlin.String

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onBack: () -> Unit,
    onSettings: () -> Unit,
    onCard: (String, Int) -> Unit,
    calendarVm: CalendarCardViewModel,
    vm: MainViewModel
) {
    BackHandler(onBack = onBack)
    val allScoreIconId = R.drawable.pic_medal
    val weekScoreIconId = R.drawable.pic_week

    @SuppressLint("ConfigurationScreenWidthHeight")
    val calendarMaxHeight = LocalConfiguration.current.screenHeightDp

    if (vm.editBottomSheetOpen) {
        EditBottomSheet(
            onDismiss = { vm.updateEditBottomSheet(false) },
            onSave = { vm.updateEditBottomSheet(visibility = false, withDataUpdate = true) },
            onDelete = {},
            vm = EditBottomSheetController(vm.localRepo, null, null)
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.clickable(
                            interactionSource = null,
                            indication = null
                        ) {
                            vm.setUpSheetVisibility(!vm.isUpSheetVisible)
                        },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ScoreIcon(allScoreIconId)
                        Text(vm.levelObj.currentLevel)
                        Spacer(Modifier.width(16.dp))
                        ScoreIcon(weekScoreIconId)
                        Text("${vm.scores.weekPoints}")
                    }
                },
                actions = {
                    IconButton(onClick = { vm.updateEditBottomSheet(true) }) {
                        Icon(painterResource(R.drawable.ic_add), stringResource(R.string.add_card))
                    }
                    IconButton(onClick = onSettings) {
                        Icon(painterResource(R.drawable.ic_settings), stringResource(R.string.settings))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            CalendarCard(
                gridHeightDp = calendarMaxHeight / 2,
                modifier = Modifier.padding(horizontal = 4.dp),
                vm = calendarVm
            )
            Spacer(Modifier.height(8.dp))
            CardItems(
                onCard = onCard,
                modifier = Modifier.padding(horizontal = 4.dp),
                viewModel = vm
            )
            Spacer(Modifier.height(8.dp))
        }
        UpSheet(
            onDismiss = { vm.setUpSheetVisibility(false) },
            isVisible = vm.isUpSheetVisible,
            padding = innerPadding
        ) {
            ScoreItem(
                title = "${stringResource(R.string.workout_score_is)} ${vm.levelObj.currentLevel}",
                iconId = allScoreIconId,
                descriptions = listOf(stringResource(R.string.all_fit_points_info)),
                currentLevel = vm.levelObj.currentLevel,
                nextLevel = vm.levelObj.nextLevel,
                progress = vm.levelObj.progress,
                modifier = Modifier.padding(bottom = 20.dp, start = 10.dp, end = 10.dp)
            )
            ScoreItem(
                title = "${stringResource(R.string.week_score_is)} ${vm.scores.weekPoints}",
                iconId = weekScoreIconId,
                descriptions = listOf(stringResource(R.string.week_points_info)),
                modifier = Modifier.padding(bottom = 20.dp, start = 10.dp, end = 10.dp)
            )
        }
    }
}