package com.yurhel.alex.afit.data

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher as Launcher
import androidx.activity.result.ActivityResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.Scope
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.AbstractInputStreamContent
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.yurhel.alex.afit.R
import java.io.ByteArrayOutputStream

class RemoteRepo(val context: Context) {
    fun sync(
        isImport: Boolean,
        launcher: Launcher<Intent, ActivityResult>,
        importDB: (String, Context) -> Unit,
        exportDB: () -> String
    ) {
        if (GoogleSignIn.getLastSignedInAccount(context) != null) {
            driveSync(isImport, importDB, exportDB)
        } else {
            googleSighIn(launcher)
        }
    }

    fun resultAuth(result: ActivityResult) {
        result.data?.let { GoogleSignIn.getSignedInAccountFromIntent(it) }
    }

    private fun googleSighIn(launcher: Launcher<Intent, ActivityResult>) {
        val signInOption = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(Scopes.DRIVE_APPFOLDER))
            .build()
        val signInClient = GoogleSignIn.getClient(context, signInOption)
        launcher.launch(signInClient.signInIntent)
    }

    private fun driveSync(
        isImport: Boolean,
        importDB: (String, Context) -> Unit,
        exportDB: () -> String
    ) {
        try {
            GoogleSignIn.getLastSignedInAccount(context)?.let { account ->
                // Setup drive service
                val cred = GoogleAccountCredential.usingOAuth2(
                    context,
                    listOf(Scopes.DRIVE_APPFOLDER)
                )
                cred.setSelectedAccount(account.account)

                val service = Drive.Builder(
                    AndroidHttp.newCompatibleTransport(),
                    GsonFactory.getDefaultInstance(),
                    cred
                )
                    .setApplicationName("com.yurhel.alex.afit")
                    .build()

                val files = service.files().list()
                    .setSpaces("appDataFolder")
                    .setFields("nextPageToken, files(id, name)")
                    .setPageSize(10)
                    .execute()

                // Search config file
                var confId = ""
                for (file in files.files) {
                    if (file.name == "config.json") confId = file.id
                }

                if (isImport) {
                    // Try get data
                    if (confId.isEmpty()) {
                        showToast(R.string.no_data)
                    } else {
                        val outputStream = ByteArrayOutputStream()
                        service.files()[confId].executeMediaAndDownloadTo(outputStream)
                        importDB(outputStream.toString(), context)
                    }
                } else {
                    // Send data
                    val content: AbstractInputStreamContent = ByteArrayContent.fromString(
                        "application/json", exportDB()
                    )
                    if (confId.isEmpty()) {
                        val file = File()
                        file.setName("config.json")
                        file.setParents(listOf("appDataFolder"))
                        service.files().create(file, content).setFields("id").execute()
                    } else {
                        service.files().update(confId, null, content).setFields("id").execute()
                    }
                }

                showToast(R.string.ok)
            }
        } catch (_: Exception) {
            showToast(R.string.error)
        }
    }

    private fun showToast(resId: Int) {
        (context as Activity).runOnUiThread {
            Toast.makeText(context, resId, Toast.LENGTH_LONG).show()
        }
    }
}