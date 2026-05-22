package com.yurhel.alex.afit.ui.screen_settings

import android.content.Context
import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher as Launcher
import androidx.activity.result.ActivityResult
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yurhel.alex.afit.R
import com.yurhel.alex.afit.data.LocalRepo
import com.yurhel.alex.afit.data.Obj
import com.yurhel.alex.afit.data.RemoteRepo
import com.yurhel.alex.afit.data.StorageRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.core.net.toUri

class SettingsViewModel(
    private val localRepo: LocalRepo,
    private val remoteRepo: RemoteRepo,
    private val storageRepo: StorageRepo
): ViewModel() {
    class Factory(
        private val localRepo: LocalRepo,
        private val remoteRepo: RemoteRepo,
        private val storageRepo: StorageRepo
    ): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = SettingsViewModel(
            localRepo, remoteRepo, storageRepo
        ) as T
    }

    val syncSettings = listOf(
        Setting(text = R.string.import_db, iconId = R.drawable.ic_file_download) { _, _, _ ->
            setAskDialogVisibility(true, R.string.import_db)
        },
        Setting(text = R.string.export_db, iconId = R.drawable.ic_file_upload) { _, lExport, _ ->
            if (lExport != null) storageRepo.exportDB(lExport)
        },
        Setting(text = R.string.importDriveDB, iconId = R.drawable.ic_cloud_download) { _, _, _ ->
            setAskDialogVisibility(true, R.string.importDriveDB)
        },
        Setting(text = R.string.exportDriveDB, iconId = R.drawable.ic_cloud_upload) { _, _, lAuth ->
            if (lAuth != null) syncRemote(false, lAuth)
        }
    )
    val statsSettings = listOf(
        Setting(text = R.string.stats_visibility, iconId = R.drawable.ic_visibility) { _, _, _ ->
            showStats = !showStats
        }
    )
    val langSettings = listOf(
        Setting(text = R.string.lang, iconId = R.drawable.ic_language) { _, _, _ ->
            showLangs = !showLangs
        }
    )
    val aboutSettings = listOf(
        Setting(text = R.string.about_app, iconId = R.drawable.ic_info) { _, _, _ ->
            showAbout = !showAbout
        }
    )

    var showStats by mutableStateOf(false)
        private set
    var showLangs by mutableStateOf(false)
        private set
    var showAbout by mutableStateOf(false)
        private set
    var data by mutableStateOf(listOf<Obj>())
        private set
    var hiddens by mutableStateOf(listOf<Hidden>())
        private set

    fun updateData() {
        data = localRepo.getMainTableEntries(true, false) + localRepo.getMainTableEntries(false, false)
        hiddens = localRepo.getHidden()
    }

    fun onObjClick(isExercise: Boolean, id: Int, isCheck: Boolean) {
        val obj = Hidden(id, isExercise)
        if (isCheck) localRepo.removeHidden(obj) else localRepo.addHidden(obj)
        updateData()
    }

    fun getAppVersion(context: Context): String {
        return try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            "${context.getString(R.string.app_ver)} ${pInfo.versionName}"
        } catch (_: Exception) { "" }
    }

    fun openLink(context: Context, linkId: Int) {
        context.startActivity(Intent(Intent.ACTION_VIEW).setData(context.getString(linkId).toUri()))
    }

    var isLoading by mutableStateOf(false)
        private set
    var isAskDialogOpen by mutableStateOf(false)
        private set
    private var askDialogAction: Int? = null

    fun setAskDialogVisibility(value: Boolean, actionStringId: Int?) {
        isAskDialogOpen = value
        askDialogAction = actionStringId
    }

    fun askDialogConfirm(
        launcherImport: Launcher<Intent, ActivityResult>,
        launcherAuth: Launcher<Intent, ActivityResult>
    ) {
        when (askDialogAction) {
            R.string.import_db -> storageRepo.importDB(launcherImport)
            R.string.importDriveDB -> syncRemote(true, launcherAuth)
        }
        setAskDialogVisibility(false, null)
    }

    fun resultExportDb(result: ActivityResult) {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading = true
            storageRepo.resultExportDb(result) { localRepo.exportDB().toString() }
            isLoading = false
        }
    }

    fun resultImportDb(result: ActivityResult) {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading = true
            storageRepo.resultImportDb(result, localRepo::importDB)
            isLoading = false
        }
    }

    private fun syncRemote(isImport: Boolean, launcher: Launcher<Intent, ActivityResult>) {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading = true
            remoteRepo.sync(
                isImport = isImport,
                launcher = launcher,
                importDB = localRepo::importDB,
                exportDB = { localRepo.exportDB().toString() }
            )
            isLoading = false
        }
    }

    fun resultAuth(result: ActivityResult) {
        remoteRepo.resultAuth(result)
    }

    init {
        updateData()
    }
}