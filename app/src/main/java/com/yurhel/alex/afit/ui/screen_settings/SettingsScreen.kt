package com.yurhel.alex.afit.ui.screen_settings

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yurhel.alex.afit.R
import com.yurhel.alex.afit.ui.screen_settings.components.AskDialog
import com.yurhel.alex.afit.ui.screen_settings.components.CheckedCardItem
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.colorResource
import com.yurhel.alex.afit.ui.screen_settings.components.LanguageSelector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    onBack: () -> Unit,
    vm: SettingsViewModel
) {
    val context = LocalContext.current
    BackHandler(onBack = onBack)
    val launcherImport = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        vm.resultImportDb(it)
    }
    val launcherExport = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        vm.resultExportDb(it)
    }
    val launcherAuth = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        vm.resultAuth(it)
    }
    AskDialog(
        visible = vm.isAskDialogOpen,
        text = stringResource(R.string.data_replace),
        confirmButtonCLicked = {
            vm.askDialogConfirm(launcherImport, launcherAuth)
        },
        cancelClicked = {
            vm.setAskDialogVisibility(false, null)
        }
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.settings))
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.ic_back), "Back")
                    }
                },
                actions = {
                    if (vm.isLoading) {
                        CircularProgressIndicator()
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                items(items = vm.settings) { items ->
                    ElevatedCard(modifier = Modifier.padding(horizontal = 10.dp)) {
                        Column {
                            items.forEach {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .defaultMinSize(minHeight = 50.dp)
                                        .clickable(
                                            enabled = !vm.isLoading,
                                            role = Role.Button
                                        ) {
                                            it.action(context, launcherExport, launcherAuth)
                                        },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(it.iconId),
                                        contentDescription = null,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                    Text(
                                        text = stringResource(it.text),
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    if (items == vm.statsSettings || items == vm.languageSettings) {
                                        val showIndicator = if (it.text == R.string.stats_visibility) {
                                            vm.showStats
                                        } else {
                                            vm.showLangs
                                        }

                                        Spacer(Modifier.weight(1f))
                                        Icon(
                                            painter = painterResource(
                                                if (showIndicator) R.drawable.ic_arrow_up else {
                                                    R.drawable.ic_arrow_down
                                                }
                                            ),
                                            contentDescription = null,
                                            modifier = Modifier.padding(horizontal = 12.dp)
                                        )
                                    }
                                }
                            }
                        }
                        if (items == vm.statsSettings && vm.showStats) {
                            Column(
                                modifier = Modifier.padding(start = 10.dp, end = 10.dp, bottom = 5.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                vm.data.forEachIndexed { idx, obj ->
                                    if (idx != 0) HorizontalDivider()
                                    CheckedCardItem(
                                        onClick = vm::onObjClick,
                                        hiddens = vm.hiddens,
                                        obj = obj,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        } else if (items == vm.languageSettings && vm.showLangs) {
                            LanguageSelector()
                        }
                    }
                }
            }
            Spacer(Modifier.height(32.dp).weight(1f))
            Text(
                text = vm.getAppVersion(context),
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                color = colorResource(R.color.grey),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}