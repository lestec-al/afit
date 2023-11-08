package com.yurhel.alex.afit;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.view.MenuCompat;
import androidx.core.widget.TextViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements ClickInterface {
    DB db;
    List<MyObject> data;
    int themeColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setTitle(R.string.app_name);
        themeColor = Help.getMainColor(this);
        db = new DB(this);
        updateAll();
    }

    @Override
    public void onBackPressed() {
        this.finishAffinity();
    }

    @Override
    public void onClickItem(int pos, String option) {
        startActivity(new Intent(MainActivity.this, StatsActivity.class).putExtra(option, data.get(pos).id));
        finish();
    }

    // TOOLBAR
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        MenuCompat.setGroupDividerEnabled(menu, true);
        Help.setActionIconsColor(themeColor, menu, new int[] {
                R.id.actionAddEx, R.id.actionAddStats, R.id.actionSettings, R.id.actionCalendar, R.id.actionMore, R.id.actionGraph
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.actionAddEx) {
            addDialog(R.string.add_ex);
            return true;

        } else if (item.getItemId() == R.id.actionAddStats) {
            addDialog(R.string.add_st);
            return true;

        } else if (item.getItemId() == R.id.actionCalendar) {
            startActivity(new Intent(MainActivity.this, CalendarActivity.class));
            finish();

        } else if (item.getItemId() == R.id.actionGraph) {
            startActivity(new Intent(MainActivity.this, ManyStatsActivity.class));
            finish();

        } else if (item.getItemId() == R.id.actionSettings) {
            googleSighIn(true);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // HELPS
    private void settingsDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_settings);

        Window window = dialog.getWindow();
        if (window != null)
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        dialog.findViewById(R.id.statsSettingsLayout).setVisibility(View.GONE);
        dialog.findViewById(R.id.deleteButton).setVisibility(View.GONE);

        ImageButton back = dialog.findViewById(R.id.backButton);
        back.setOnClickListener(view -> dialog.cancel());
        back.setColorFilter(themeColor);

        // Try set app name and version
        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(this.getPackageName(), 0);
            ((TextView)dialog.findViewById(R.id.versionTV)).setText(String.format("%s %s", getString(R.string.app_ver), pInfo.versionName));
        } catch (Exception ignored) {}

        ProgressBar progress = dialog.findViewById(R.id.progressSync);
        progress.setProgressTintList(ColorStateList.valueOf(themeColor));
        progress.getIndeterminateDrawable().setColorFilter(themeColor, android.graphics.PorterDuff.Mode.SRC_IN);

        Button sendFeedback = dialog.findViewById(R.id.feedbackButton);
        sendFeedback.setTextColor(themeColor);
        TextViewCompat.setCompoundDrawableTintList(sendFeedback, ColorStateList.valueOf(themeColor));
        sendFeedback.setOnClickListener(v -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW)
                        .setData(Uri.parse(getString(R.string.app_link)))
                        .setPackage("com.android.vending"));
            } catch (Exception ignore) {}
        });

        Button privacyPolicy = dialog.findViewById(R.id.privacyButton);
        privacyPolicy.setTextColor(themeColor);
        TextViewCompat.setCompoundDrawableTintList(privacyPolicy, ColorStateList.valueOf(themeColor));
        privacyPolicy.setOnClickListener(v -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(getString(R.string.privacy_link))));
            } catch (Exception ignore) {}
        });

        Button exportDB = dialog.findViewById(R.id.exportButton);
        exportDB.setTextColor(themeColor);
        TextViewCompat.setCompoundDrawableTintList(exportDB, ColorStateList.valueOf(themeColor));
        exportDB.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/json");
            intent.putExtra(Intent.EXTRA_TITLE, "data.json");
            resultExport.launch(intent);
        });

        Button importDB = dialog.findViewById(R.id.importButton);
        importDB.setTextColor(themeColor);
        TextViewCompat.setCompoundDrawableTintList(importDB, ColorStateList.valueOf(themeColor));
        importDB.setOnClickListener(v -> {
            Object[] d = Help.editTextDialog(this, themeColor,
                    R.string.data_replace, "", null, false);
            ((Button)d[1]).setOnClickListener(v1 -> {
                ((Dialog)d[0]).cancel();

                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("application/json");
                resultImport.launch(intent);
            });
            ((Dialog)d[0]).show();
        });

        // If user authenticated or not
        if (GoogleSignIn.getLastSignedInAccount(this) != null) {
            Button exportDriveDB = dialog.findViewById(R.id.exportDriveButton);
            exportDriveDB.setTextColor(themeColor);
            TextViewCompat.setCompoundDrawableTintList(exportDriveDB, ColorStateList.valueOf(themeColor));
            exportDriveDB.setOnClickListener(v ->
                    driveSync(false, () -> progress.setVisibility(View.VISIBLE), () -> progress.setVisibility(View.GONE))
            );

            Button importDriveDB = dialog.findViewById(R.id.importDriveButton);
            importDriveDB.setTextColor(themeColor);
            TextViewCompat.setCompoundDrawableTintList(importDriveDB, ColorStateList.valueOf(themeColor));
            importDriveDB.setOnClickListener(v -> {
                Object[] d = Help.editTextDialog(this, themeColor,
                        R.string.data_replace, "", null, false);
                ((Button)d[1]).setOnClickListener(v1 -> {
                    ((Dialog)d[0]).cancel();
                    driveSync(true, () -> progress.setVisibility(View.VISIBLE), () -> progress.setVisibility(View.GONE));
                });
                ((Dialog)d[0]).show();
            });
        } else {
            dialog.findViewById(R.id.exportDriveButton).setVisibility(View.GONE);
            dialog.findViewById(R.id.importDriveButton).setVisibility(View.GONE);

            Button cloudConnect = dialog.findViewById(R.id.buttonCloudConnect);
            cloudConnect.setVisibility(View.VISIBLE);
            cloudConnect.setTextColor(themeColor);
            TextViewCompat.setCompoundDrawableTintList(cloudConnect, ColorStateList.valueOf(themeColor));
            cloudConnect.setOnClickListener(v -> {
                Object[] d = Help.editTextDialog(this, themeColor,
                        R.string.auth_info, "", null, false);
                ((Button)d[1]).setOnClickListener(v1 -> {
                    ((Dialog)d[0]).cancel();
                    dialog.cancel();
                    googleSighIn(false);
                });
                ((Dialog)d[0]).show();
            });
        }
        dialog.show();
    }

    private void updateAll() {
        data = db.getTableEntries(null, true);
        data.addAll(db.getTableEntries(null, false));
        RecyclerView mainView = findViewById(R.id.mainRV);
        mainView.setLayoutManager(new LinearLayoutManager(this));
        mainView.setHasFixedSize(true);
        mainView.setAdapter(new MainAdapter(this, data, this));
        // Down buttons and empty text
        if (data.size() == 0) {
            findViewById(R.id.mainDownActions).setVisibility(View.VISIBLE);
            findViewById(R.id.emptyTV).setVisibility(View.VISIBLE);
            // Add exercise
            Drawable dEx = Objects.requireNonNull(AppCompatResources.getDrawable(this, R.drawable.ic_up_add));
            dEx.setTint(themeColor);
            Button addExDown = findViewById(R.id.addExDown);
            addExDown.setOnClickListener(v -> addDialog(R.string.add_ex));
            addExDown.setTextColor(themeColor);
            addExDown.setCompoundDrawablesWithIntrinsicBounds(dEx, null, null, null);
            // Add statistic
            Drawable dSt = Objects.requireNonNull(AppCompatResources.getDrawable(this, R.drawable.ic_up_add_stats));
            dSt.setTint(themeColor);
            Button addStDown = findViewById(R.id.addStDown);
            addStDown.setOnClickListener(v -> addDialog(R.string.add_st));
            addStDown.setTextColor(themeColor);
            addStDown.setCompoundDrawablesWithIntrinsicBounds(dSt, null, null, null);
        } else {
            findViewById(R.id.mainDownActions).setVisibility(View.GONE);
            findViewById(R.id.emptyTV).setVisibility(View.GONE);
        }
    }

    public void addDialog(int msg) {
        Object[] d = Help.editTextDialog(this, themeColor, msg, "", false, false);
        ((Button)d[1]).setOnClickListener(v1 -> {
            String t = ((EditText)d[2]).getText().toString();
            if (!t.equals("")) {
                if (msg == R.string.add_ex)
                    db.addExercise(t);
                else
                    db.addStats(t);
                ((Dialog)d[0]).cancel();
                updateAll();
            }
        });
        ((Dialog)d[0]).show();
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
                updateAll();
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
        if (isOpenSettings)
            signInClient.silentSignIn().addOnCompleteListener(task -> settingsDialog());
        else
            resultAuth.launch(signInClient.getSignInIntent());
    }
    private final ActivityResultLauncher<Intent> resultAuth = registerForActivityResult(new StartActivityForResult(), result -> {
        Intent intent = result.getData();
        if (intent != null)
            GoogleSignIn.getSignedInAccountFromIntent(intent);
    });

    private void driveSync(boolean isImport, Runnable actionBefore, Runnable actionAfter) {
        new Thread(() -> {
            if (actionBefore != null)
                runOnUiThread(actionBefore);
            try {
                GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
                if (account != null) {
                    // Setup drive service
                    GoogleAccountCredential credentials = GoogleAccountCredential.usingOAuth2(this, List.of(Scopes.DRIVE_APPFOLDER));
                    credentials.setSelectedAccount(account.getAccount());
                    Drive service = new Drive.Builder(AndroidHttp.newCompatibleTransport(), GsonFactory.getDefaultInstance(), credentials)
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
                        if (file.getName().equals("config.json"))
                            configID = file.getId();
                    }
                    if (isImport) {
                        // Try get data
                        if (configID.equals("")) {
                            runOnUiThread(() -> Toast.makeText(MainActivity.this, R.string.no_data, Toast.LENGTH_LONG).show());
                        } else {
                            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                            service.files().get(configID).executeMediaAndDownloadTo(outputStream);
                            db.importDB(outputStream.toString());
                        }
                        runOnUiThread(this::updateAll);
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
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, R.string.ok, Toast.LENGTH_LONG).show());
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(MainActivity.this, R.string.error, Toast.LENGTH_LONG).show());
            }
            if (actionAfter != null)
                runOnUiThread(actionAfter);
        }).start();
    }
}