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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yurhel.alex.afit.R
import com.yurhel.alex.afit.data.LocalRepo
import com.yurhel.alex.afit.data.getAllScoreEmoji
import com.yurhel.alex.afit.data.getScores
import com.yurhel.alex.afit.data.getWeekScoreEmoji
import com.yurhel.alex.afit.ui.screen_main.calendar.CalendarCard
import com.yurhel.alex.afit.ui.screen_main.calendar.CalendarCardViewModel
import com.yurhel.alex.afit.ui.screen_main.cards.CardItems
import com.yurhel.alex.afit.ui.screen_main.cards.CardItemsViewModel
import com.yurhel.alex.afit.ui.screen_main.upbar.LevelObj
import com.yurhel.alex.afit.ui.screen_main.upbar.ScoreLevelItem
import com.yurhel.alex.afit.ui.screen_main.upbar.ScoreTextItem
import com.yurhel.alex.afit.ui.screen_main.upbar.UpSheet
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onBack: () -> Unit,
    onSettings: () -> Unit,
    onCard: (String, Int) -> Unit,
    localRepo: LocalRepo
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val scores by remember { mutableStateOf(getScores(localRepo)) }
    var isUpSheetVisible by remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    @SuppressLint("ConfigurationScreenWidthHeight")
    val calendarMaxGridHeight = LocalConfiguration.current.screenHeightDp
    val levelObj by remember {
        val x = "${(scores.allPoints / 1000f) + 1}".split('.')
        val currentLevel = x[0]
        mutableStateOf(
            @SuppressLint("LocalContextGetResourceValueCall")
            LevelObj(
                title = "${getAllScoreEmoji()} ${context.getString(R.string.workout_score_is)}",
                descriptions = listOf(context.getString(R.string.all_fit_points_info)),
                currentLevel = currentLevel,
                nextLevel = "${currentLevel.toInt() + 1}",
                progress = "0.${x[1]}".toFloat()
            )
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.clickable(
                            interactionSource = null,
                            indication = null
                        ) {
                            isUpSheetVisible = !isUpSheetVisible
                        },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "${getAllScoreEmoji()} ${levelObj.currentLevel}")
                        Spacer(Modifier.width(16.dp))
                        Text(text = "${getWeekScoreEmoji()} ${scores.weekPoints}")
                    }
                },
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(painterResource(R.drawable.ic_settings), stringResource(R.string.settings))
                    }
                },
                scrollBehavior = scrollBehavior
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
                gridHeightDp = calendarMaxGridHeight / 2,
                modifier = Modifier.padding(horizontal = 4.dp),
                vm = viewModel(factory = CalendarCardViewModel.Factory(localRepo))
            )
            Spacer(Modifier.height(8.dp))
            CardItems(
                onCard = onCard,
                modifier = Modifier.padding(horizontal = 4.dp),
                viewModel = viewModel(factory = CardItemsViewModel.Factory(localRepo))
            )
            Spacer(Modifier.height(8.dp))
        }
        UpSheet(
            onDismiss = { isUpSheetVisible = false },
            isVisible = isUpSheetVisible,
            padding = innerPadding
        ) {
            ScoreLevelItem(
                obj = levelObj,
                modifier = Modifier.padding(bottom = 20.dp, start = 10.dp, end = 10.dp)
            )
            ScoreTextItem(
                title = "${getWeekScoreEmoji()} ${stringResource(R.string.week_score_is)} ${scores.weekPoints}",
                descriptions = listOf(
                    Pair(stringResource(R.string.week_points_info), null)
                ),
                modifier = Modifier.padding(bottom = 20.dp, start = 10.dp, end = 10.dp)
            )
        }
    }
}