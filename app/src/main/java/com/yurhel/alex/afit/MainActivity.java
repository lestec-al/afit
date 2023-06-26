package com.yurhel.alex.afit;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Bundle;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ClickInterface {
    DB db;
    List<MyObject> data;
    int themeColor;
    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setTitle("");
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
    }

    // TOOLBAR
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        MenuCompat.setGroupDividerEnabled(menu, true);
        Help.setActionIconsColor(
                themeColor, menu, new int[] {R.id.action_add_ex, R.id.action_add_stats, R.id.action_settings, R.id.action_calendar, R.id.action_more}
        );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add_ex) {
            addDialog(R.string.add_ex);
            return true;

        } else if (item.getItemId() == R.id.action_add_stats) {
            addDialog(R.string.add_st);
            return true;

        } else if (item.getItemId() == R.id.action_calendar) {
            startActivity(new Intent(MainActivity.this, CalendarActivity.class));
            return true;

        } else if (item.getItemId() == R.id.action_settings) {
            // Settings dialog
            dialog = new Dialog(this);
            dialog.setContentView(R.layout.dialog_settings);
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.findViewById(R.id.stats_layout).setVisibility(View.GONE);
            dialog.findViewById(R.id.button_delete_dialog).setVisibility(View.GONE);
            Button sendFeedback = dialog.findViewById(R.id.button_send_feedback_dialog);
            sendFeedback.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW)
                    .setData(Uri.parse(getString(R.string.app_link)))
                    .setPackage("com.android.vending")
            ));
            try {// Get version
                PackageInfo pInfo = this.getPackageManager().getPackageInfo(this.getPackageName(), 0);
                String version = getString(R.string.app_name)+" "+pInfo.versionName;
                ((TextView)dialog.findViewById(R.id.title_settings)).setText(version);
            } catch (Exception ignored) {}
            Button exportDB = dialog.findViewById(R.id.button_export_dialog);
            exportDB.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("application/json");
                intent.putExtra(Intent.EXTRA_TITLE, "data.json");
                resultExport.launch(intent);
            });
            Button importDB = dialog.findViewById(R.id.button_import_dialog);
            importDB.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("application/json");
                resultImport.launch(intent);
            });
            Help.setButtonsTextColor(themeColor, new Button[] {exportDB, importDB, sendFeedback});
            ImageButton back = dialog.findViewById(R.id.button_back_settings_dialog);
            back.setOnClickListener(view -> dialog.cancel());
            back.setColorFilter(themeColor);
            dialog.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // HELPS
    private void updateAll() {
        data = db.getTableEntries(null, true);
        data.addAll(db.getTableEntries(null, false));
        RecyclerView mainView = findViewById(R.id.main_view);
        mainView.setLayoutManager(new LinearLayoutManager(this));
        mainView.setHasFixedSize(true);
        mainView.setAdapter(new MainAdapter(this, data, this, themeColor));
    }

    public void addDialog(int msg) {
        Object[] d = Help.editTextDialog(this, themeColor, msg, "", false, false);
        Dialog dialog1 = (Dialog)d[0];
        ((Button)d[1]).setOnClickListener(v1 -> {
            String t = ((EditText)d[2]).getText().toString();
            if (!t.equals("")) {
                if (msg == R.string.add_ex)
                    db.addExercise(t);
                else
                    db.addStats(t);
                dialog1.cancel();
                updateAll();
            }
        });
        dialog1.show();
    }

    // EXPORT / IMPORT
    private final ActivityResultLauncher<Intent> resultExport = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            try {
                OutputStream os = getContentResolver().openOutputStream(result.getData().getData());
                byte[] input = db.exportDatabase().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
                os.close();
                Toast.makeText(this, getString(R.string.ok), Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, getString(R.string.error), Toast.LENGTH_LONG).show();
            }
            dialog.cancel();
        }
    });

    private final ActivityResultLauncher<Intent> resultImport = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            try {
                InputStream is = getContentResolver().openInputStream(result.getData().getData());
                BufferedReader in = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null)
                    response.append(inputLine);
                in.close();
                is.close();
                if (!db.importDatabase(response.toString()))
                    throw new Exception("Import error");
                dialog.cancel();
                updateAll();
            } catch (Exception e) {
                e.printStackTrace();
                dialog.cancel();
                Toast.makeText(this, getString(R.string.error), Toast.LENGTH_LONG).show();
            }
        }
    });
}