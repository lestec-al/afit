package com.yurhel.alex.afit.ui.screen_settings

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
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
        Box(
            modifier = Modifier.padding(innerPadding),
            contentAlignment = if (vm.isLoading) Alignment.Center else Alignment.TopStart
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                listOf(vm.syncSettings, vm.linksSettings, vm.otherSettings).forEach { items ->
                    ElevatedCard(modifier = Modifier.padding(horizontal = 10.dp)) {
                        LazyColumn {
                            items(items = items) {
                                val text = stringResource(it.text)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .defaultMinSize(minHeight = 50.dp)
                                        .clickable(
                                            enabled = !vm.isLoading,
                                            onClickLabel = text,
                                            role = Role.Button
                                        ) {
                                            it.action(context, launcherExport, launcherAuth)
                                        },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (it.iconId != null) {
                                        Icon(
                                            painter = painterResource(it.iconId),
                                            contentDescription = null,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                    if (it.iconVector != null) {
                                        Icon(
                                            imageVector = it.iconVector,
                                            contentDescription = null,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                    Text(
                                        text = text,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    if (items == vm.otherSettings) {
                                        Spacer(Modifier.weight(1f))
                                        Icon(
                                            imageVector = if (vm.isShowData) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                            contentDescription = null,
                                            modifier = Modifier.padding(horizontal = 12.dp)
                                        )
                                    }
                                }
                            }
                        }
                        if (items == vm.otherSettings && vm.isShowData) {
                            LazyColumn(
                                modifier = Modifier.padding(start = 10.dp, end = 10.dp, bottom = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                itemsIndexed(items = vm.data) { idx, obj ->
                                    if (idx != 0) HorizontalDivider()
                                    CheckedCardItem(
                                        onClick = vm::onObjClick,
                                        hiddens = vm.hiddens,
                                        obj = obj,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier
                    .height(32.dp)
                    .weight(1f))
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
}