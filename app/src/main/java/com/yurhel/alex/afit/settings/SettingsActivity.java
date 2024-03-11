package com.yurhel.alex.afit.settings;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.TextViewCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.Scope;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.yurhel.alex.afit.MainActivity;
import com.yurhel.alex.afit.R;
import com.yurhel.alex.afit.core.DB;
import com.yurhel.alex.afit.core.Help;
import com.yurhel.alex.afit.databinding.ActivitySettingsBinding;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {
    DB db;
    int themeColor;
    Menu upMenu;
    ActivitySettingsBinding views;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        views = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(views.getRoot());

        // On back pressed
        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                startActivity(new Intent(SettingsActivity.this, MainActivity.class));
                finish();
            }
        });

        googleSighIn(true);
        db = new DB(this);
        themeColor = getColor(R.color.on_background);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.settings);
            actionBar.setDisplayHomeAsUpEnabled(true);
            Help.setActionBackIconColor(this, themeColor, actionBar);
        }

        // Try set app name and version
        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(this.getPackageName(), 0);
            views.versionTV.setText(String.format("%s %s", getString(R.string.app_ver), pInfo.versionName));
        } catch (Exception ignored) {}

        views.feedbackButton.setTextColor(themeColor);
        TextViewCompat.setCompoundDrawableTintList(views.feedbackButton, ColorStateList.valueOf(themeColor));
        views.feedbackButton.setOnClickListener(v -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW)
                        .setData(Uri.parse(getString(R.string.app_link)))
                        .setPackage("com.android.vending"));
            } catch (Exception ignore) {}
        });

        views.privacyButton.setTextColor(themeColor);
        TextViewCompat.setCompoundDrawableTintList(views.privacyButton, ColorStateList.valueOf(themeColor));
        views.privacyButton.setOnClickListener(v -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(getString(R.string.privacy_link))));
            } catch (Exception ignore) {}
        });

        views.exportButton.setTextColor(themeColor);
        TextViewCompat.setCompoundDrawableTintList(views.exportButton, ColorStateList.valueOf(themeColor));
        views.exportButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/json");
            intent.putExtra(Intent.EXTRA_TITLE, "data.json");
            resultExport.launch(intent);
        });

        views.importButton.setTextColor(themeColor);
        TextViewCompat.setCompoundDrawableTintList(views.importButton, ColorStateList.valueOf(themeColor));
        views.importButton.setOnClickListener(v -> askImportDialog(() -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/json");
            resultImport.launch(intent);
        }));

        // Export
        views.exportDriveButton.setTextColor(themeColor);
        TextViewCompat.setCompoundDrawableTintList(views.exportDriveButton, ColorStateList.valueOf(themeColor));
        views.exportDriveButton.setOnClickListener(v -> {
            // If user authenticated or not
            if (GoogleSignIn.getLastSignedInAccount(this) != null) {
                driveSync(false, () -> setIndicatorVisibility(View.VISIBLE), () -> setIndicatorVisibility(View.GONE));
            } else {
                askAuthDialog();
            }
        });

        // Import
        views.importDriveButton.setTextColor(themeColor);
        TextViewCompat.setCompoundDrawableTintList(views.importDriveButton, ColorStateList.valueOf(themeColor));
        views.importDriveButton.setOnClickListener(v -> {
            // If user authenticated or not
            if (GoogleSignIn.getLastSignedInAccount(this) != null) {
                askImportDialog(() ->
                        driveSync(true, () -> setIndicatorVisibility(View.VISIBLE), () -> setIndicatorVisibility(View.GONE))
                );
            } else {
                askAuthDialog();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save, menu);
        upMenu = menu;
        upMenu.findItem(R.id.actionSave).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return false;
    }

    private void askImportDialog(Runnable action) {
        Object[] d = Help.editDialog(
                this,
                themeColor,
                R.string.data_replace,
                "",
                null,
                false
        );
        ((ImageButton)d[1]).setOnClickListener(v1 -> {
            ((Dialog)d[0]).cancel();
            action.run();
        });
        ((Dialog)d[0]).show();
    }

    private void askAuthDialog() {
        Object[] d = Help.editDialog(
                this,
                themeColor,
                R.string.auth_info,
                "",
                null,
                false
        );
        ((ImageButton)d[1]).setOnClickListener(v1 -> {
            ((Dialog)d[0]).cancel();
            googleSighIn(false);
        });
        ((Dialog)d[0]).show();
    }

    @SuppressLint("InflateParams")
    private void setIndicatorVisibility(int visibility) {
        if (visibility == View.VISIBLE) {
            // Create indicator
            ProgressBar indicator = (ProgressBar) LayoutInflater.from(this).inflate(R.layout.view_indicator, null);
            indicator.setProgressTintList(ColorStateList.valueOf(themeColor));
            indicator.getIndeterminateDrawable().setColorFilter(themeColor, android.graphics.PorterDuff.Mode.SRC_IN);
            // Replace menu item with indicator and show it
            upMenu.findItem(R.id.actionSave).setActionView(indicator);
            upMenu.findItem(R.id.actionSave).setVisible(true);
        } else {
            // Hide indicator
            upMenu.findItem(R.id.actionSave).setVisible(false);
            upMenu.findItem(R.id.actionSave).setActionView(null);
        }
    }

    // EXPORT & IMPORT TO DEVICE
    private final ActivityResultLauncher<Intent> resultExport = registerForActivityResult(new StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            try {
                OutputStream os = getContentResolver().openOutputStream(Objects.requireNonNull(result.getData().getData()));
                byte[] input = db.exportDB().toString().getBytes(StandardCharsets.UTF_8);
                assert os != null;
                os.write(input, 0, input.length);
                os.close();
                Toast.makeText(this, R.string.ok, Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, R.string.error, Toast.LENGTH_LONG).show();
            }
        }
    });
    private final ActivityResultLauncher<Intent> resultImport = registerForActivityResult(new StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            try {
                InputStream is = getContentResolver().openInputStream(Objects.requireNonNull(result.getData().getData()));
                BufferedReader in = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null)
                    response.append(inputLine);
                in.close();
                assert is != null;
                is.close();
                if (!db.importDB(response.toString()))
                    throw new Exception("Import error");
                Toast.makeText(this, R.string.ok, Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, R.string.error, Toast.LENGTH_LONG).show();
            }
        }
    });

    // GOOGLE SERVICES
    private void googleSighIn(boolean isOpenSettings) {
        GoogleSignInOptions signInOption = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(Scopes.DRIVE_APPFOLDER))
                .build();
        GoogleSignInClient signInClient = GoogleSignIn.getClient(this, signInOption);
        if (isOpenSettings) signInClient.silentSignIn();//.addOnCompleteListener(task -> );
        else resultAuth.launch(signInClient.getSignInIntent());
    }
    private final ActivityResultLauncher<Intent> resultAuth = registerForActivityResult(new StartActivityForResult(), result -> {
        Intent intent = result.getData();
        if (intent != null) GoogleSignIn.getSignedInAccountFromIntent(intent);
    });

    private void driveSync(boolean isImport, Runnable actionBefore, Runnable actionAfter) {
        new Thread(() -> {
            if (actionBefore != null) runOnUiThread(actionBefore);
            try {
                GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
                if (account != null) {
                    // Setup drive service
                    GoogleAccountCredential cred = GoogleAccountCredential.usingOAuth2(this, List.of(Scopes.DRIVE_APPFOLDER));
                    cred.setSelectedAccount(account.getAccount());
                    Drive service = new Drive.Builder(AndroidHttp.newCompatibleTransport(), GsonFactory.getDefaultInstance(), cred)
                            .setApplicationName("com.yurhel.alex.afit")
                            .build();
                    FileList files = service.files().list()
                            .setSpaces("appDataFolder")
                            .setFields("nextPageToken, files(id, name)")
                            .setPageSize(10)
                            .execute();
                    // Search config file
                    String configID = "";
                    for (File file: files.getFiles()) {
                        if (file.getName().equals("config.json")) configID = file.getId();
                    }
                    if (isImport) {
                        // Try get data
                        if (configID.equals("")) {
                            runOnUiThread(() -> Toast.makeText(
                                    SettingsActivity.this, R.string.no_data, Toast.LENGTH_LONG
                            ).show());
                        } else {
                            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                            service.files().get(configID).executeMediaAndDownloadTo(outputStream);
                            db.importDB(outputStream.toString());
                        }
                    } else {
                        // Send data
                        AbstractInputStreamContent mediaContent =
                                ByteArrayContent.fromString("application/json", db.exportDB().toString());
                        if (configID.equals("")) {
                            File fileMetadata = new File();
                            fileMetadata.setName("config.json");
                            fileMetadata.setParents(Collections.singletonList("appDataFolder"));
                            service.files().create(fileMetadata, mediaContent).setFields("id").execute();
                        } else {
                            service.files().update(configID, null, mediaContent).setFields("id").execute();
                        }
                    }
                    runOnUiThread(() -> Toast.makeText(SettingsActivity.this, R.string.ok, Toast.LENGTH_LONG).show());
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(SettingsActivity.this, R.string.error, Toast.LENGTH_LONG).show());
            }
            if (actionAfter != null) runOnUiThread(actionAfter);
        }).start();
    }
}