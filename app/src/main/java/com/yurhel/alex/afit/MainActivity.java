package com.yurhel.alex.afit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.yurhel.alex.afit.core.Click;
import com.yurhel.alex.afit.core.DB;
import com.yurhel.alex.afit.core.Help;
import com.yurhel.alex.afit.core.Obj;
import com.yurhel.alex.afit.databinding.ActivityMainBinding;
import com.yurhel.alex.afit.edit.EditActivity;
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
        if (actionBar != null) actionBar.hide();
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

        if (data.isEmpty()) {
            // Show empty text
            views.emptyTV.setVisibility(View.VISIBLE);

            // Feature highlight
            TapTargetView.showFor(
                    this,
                    TapTarget.forView(
                                views.navigation.findViewById(R.id.actionAddCard),
                                getString(R.string.add_card),
                                ""
                            )
                            .outerCircleColorInt(getColor(R.color.green_main))
                            .textColor(R.color.white)
                            .drawShadow(true)
                            .cancelable(true)
                            .tintTarget(true)
                            .targetRadius(30),
                    new TapTargetView.Listener() {
                        @Override
                        public void onTargetClick(TapTargetView view) {
                            super.onTargetClick(view);
                            // Trying fix crash (after returning from EditActivity -> fast click) ?
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException ignored) {}
                            startActivity(new Intent(getApplicationContext(), EditActivity.class));
                            finish();
                        }
                    }
            );
        } else {
            // Hide empty text
            views.emptyTV.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClickItem(int pos, String option) {
        startActivity(new Intent(MainActivity.this, StatsActivity.class).putExtra(option, data.get(pos).id));
        finish();
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