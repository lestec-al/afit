package com.yurhel.alex.afit.ui.screen_settings

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.unit.dp
import com.yurhel.alex.afit.R
import com.yurhel.alex.afit.ui.screen_settings.components.AskDialog
import com.yurhel.alex.afit.ui.screen_settings.components.CheckedCardItems
import androidx.compose.ui.res.stringResource
import com.yurhel.alex.afit.ui.screen_settings.components.AboutApp
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
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            items(
                items = listOf(vm.syncSettings, vm.statsSettings, vm.langSettings, vm.aboutSettings)
            ) { items ->
                val showIndicator = when (items) {
                    vm.statsSettings -> vm.showStats
                    vm.langSettings -> vm.showLangs
                    vm.aboutSettings -> vm.showAbout
                    else -> null
                }

                ElevatedCard(modifier = Modifier.padding(horizontal = 10.dp)) {
                    items.forEach {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 50.dp)
                                .clickable(
                                    enabled = !vm.isLoading,
                                    role = Role.Button,
                                    onClick = {
                                        it.action(context, launcherExport, launcherAuth)
                                    }
                                ),
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
                            if (showIndicator != null) {
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
                    if (items == vm.statsSettings && vm.showStats) {
                        CheckedCardItems(
                            onClick = vm::onObjClick,
                            hiddens = vm.hiddens,
                            items = vm.data
                        )
                    } else if (items == vm.langSettings && vm.showLangs) {
                        LanguageSelector()
                    } else if (items == vm.aboutSettings && vm.showAbout) {
                        AboutApp(vm)
                    }
                }
            }
            item { Spacer(Modifier.height(10.dp)) }
        }
    }
}