package com.yurhel.alex.afit;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import java.util.LinkedHashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ClickInterface {
    DB db;
    List<MyObject> dataSt;
    List<MyObject> dataEx;
    int themeColor;
    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        themeColor = Help.getMainColor(this);
        db = new DB(this);
        dataEx = db.getAllEntries(null, true);
        dataSt = db.getAllEntries(null, false);
        LinkedHashMap<String, Integer> colors = db.getAllObjColors();
        RecyclerView mainView = findViewById(R.id.main_view);
        mainView.setLayoutManager(new LinearLayoutManager(this));
        mainView.setHasFixedSize(true);
        mainView.setAdapter(new MainAdapter(this, dataEx, true, this, themeColor, colors));
        RecyclerView statsMainView = findViewById(R.id.stats_main_view);
        statsMainView.setLayoutManager(new LinearLayoutManager(this));
        statsMainView.setHasFixedSize(true);
        statsMainView.setAdapter(new MainAdapter(this, dataSt, false, this, themeColor, colors));
    }

    @Override
    public void onBackPressed() {
        this.finishAffinity();
    }

    @Override
    public void onClickItem(int pos, String option) {
        if (option.equals("ex"))
            startActivity(new Intent(MainActivity.this, StatsActivity.class).putExtra("ex_id", dataEx.get(pos).id));
        else
            startActivity(new Intent(MainActivity.this, StatsActivity.class).putExtra("st_id", dataSt.get(pos).id));
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
            dialog.findViewById(R.id.color_layout).setVisibility(View.GONE);
            dialog.findViewById(R.id.button_date_dialog).setVisibility(View.GONE);
            TextView title = dialog.findViewById(R.id.title_settings);
            title.setText(R.string.about);
            ImageButton sendFeedback = dialog.findViewById(R.id.button_send_feedback_dialog);
            sendFeedback.setOnClickListener(v ->
                startActivity(new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", getString(R.string.email), null)))
            );
            try {// Get version
                TextView info_tv = dialog.findViewById(R.id.info_app_tv);
                PackageInfo pInfo = this.getPackageManager().getPackageInfo(this.getPackageName(), 0);
                String version = info_tv.getText().toString()+" "+pInfo.versionName;
                info_tv.setText(version);
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
            Help.setButtonsTextColor(themeColor, new Button[] {exportDB, importDB});
            ImageButton back = dialog.findViewById(R.id.button_back_settings_dialog);
            back.setOnClickListener(view -> dialog.cancel());
            Help.setImageButtonsColor(themeColor, new ImageButton[] {back, sendFeedback});
            dialog.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
                    db.addCustomStats(t);
                dialog1.cancel();
                recreate();
            }
        });
        dialog1.show();
    }

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
                recreate();
            } catch (Exception e) {
                e.printStackTrace();
                dialog.cancel();
                Toast.makeText(this, getString(R.string.error), Toast.LENGTH_LONG).show();
            }
        }
    });
}