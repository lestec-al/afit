package com.yurhel.alex.afit;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.yurhel.alex.afit.core.Click;
import com.yurhel.alex.afit.core.DB;
import com.yurhel.alex.afit.core.Help;
import com.yurhel.alex.afit.core.Obj;
import com.yurhel.alex.afit.databinding.ActivityMainBinding;
import com.yurhel.alex.afit.edit.EditActivity;
import com.yurhel.alex.afit.settings.SettingsActivity;
import com.yurhel.alex.afit.stats.StatsActivity;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements Click, MainCallback {
    DB db;
    List<Obj> data;
    int themeColor;
    ActivityMainBinding views;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        views = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(views.getRoot());

        // On back pressed
        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                MainActivity.this.finishAffinity();
            }
        });

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setTitle(R.string.app_name);
        themeColor = getColor(R.color.on_background);
        db = new DB(this);

        // Bottom navigation
        Help.setupBottomNavigation(MainActivity.this, R.id.actionHome, views.navigation, this::finish);

        // Get data
        data = db.getTableEntries(null, true);
        data.addAll(db.getTableEntries(null, false));

        // Sort data
        LinkedHashMap<String, Integer> pos = db.getPositions();
        try {
            data.sort(Comparator.comparing(obj1 -> pos.get(obj1.id + "_" + ((obj1.sets != 0/*Is exercise*/) ? "ex" : "st"))));
        } catch (Exception ignored) {}

        // Setup RV
        views.mainRV.setLayoutManager(new LinearLayoutManager(this));
        views.mainRV.setHasFixedSize(true);
        MainAdapter adapter = new MainAdapter(this, data, this, this);
        ItemTouchHelper touchHelper = new ItemTouchHelper(new MainItemMoveCallback(adapter));
        touchHelper.attachToRecyclerView(views.mainRV);
        views.mainRV.setAdapter(adapter);

        // Empty text
        views.emptyTV.setVisibility((data.size() == 0) ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onClickItem(int pos, String option) {
        startActivity(new Intent(MainActivity.this, StatsActivity.class).putExtra(option, data.get(pos).id));
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        Help.setActionIconsColor(themeColor, menu, new int[] {R.id.actionAddCard, R.id.actionSettings});
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.actionAddCard) {
            startActivity(new Intent(MainActivity.this, EditActivity.class));
            finish();
            return true;

        } else if (item.getItemId() == R.id.actionSettings) {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            finish();
            return true;
        }
        return false;
    }

    @Override
    public void onItemMoved() {
        // When item in mainRV changes position
        LinkedHashMap<String, Integer> pos = new LinkedHashMap<>();
        int idx = 0;
        for (Obj i: data) {
            // id="1_st", pos=4
            pos.put(i.id + "_" + ((i.sets != 0/*Is exercise*/) ? "ex" : "st"), idx);
            idx++;
        }
        db.setPositions(pos);
        recreate();
    }
}