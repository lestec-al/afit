package com.yurhel.alex.afit;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class ManyStatsActivity extends AppCompatActivity {
    DB db;
    int mainColor;
    RelativeLayout graphLayout;
    Button startDateB, endDateB;
    Date startDate, endDate;
    RecyclerView statsView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_many_stats);
        //
        mainColor = Help.getMainColor(this);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getString(R.string.all_data));
            Help.setActionBackIconColor(this, mainColor, actionBar);
        }
        db = new DB(this);
        graphLayout = findViewById(R.id.graphLayout);
        // Date buttons
        startDateB = findViewById(R.id.buttonDateStart);
        startDateB.setOnClickListener(v -> calendarDialog(true));
        endDateB = findViewById(R.id.buttonDateEnd);
        endDateB.setOnClickListener(v -> calendarDialog(false));
        Help.setButtonsTextColor(mainColor, new Button[] {startDateB, endDateB});
        statsView = findViewById(R.id.statsRV);
        // Show/Hide graph button
        ImageButton graphVisB = findViewById(R.id.buttonGraphVisibility);
        graphVisB.setColorFilter(mainColor);
        graphVisB.setOnClickListener(v -> {
            LinearLayout hiddenLayout = findViewById(R.id.layoutForHide);
            if (hiddenLayout.getVisibility() == View.VISIBLE) {
                hiddenLayout.setVisibility(View.GONE);
                graphVisB.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_arrow_down));
            } else {
                hiddenLayout.setVisibility(View.VISIBLE);
                graphVisB.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_arrow_up));
            }
        });
        // Graph legend/setting
        ArrayList<MyObject> dataMain = db.getTableEntries(null, true);
        dataMain.addAll(db.getTableEntries(null, false));
        LinearLayout statsLayoutLeft = findViewById(R.id.statsLayoutLeft);
        LinearLayout statsLayoutRight = findViewById(R.id.statsLayoutRight);
        int rightItems = dataMain.size() / 2;
        int leftItems = dataMain.size() - rightItems;
        int idx = 1;
        for (MyObject obj: dataMain) {
            CheckBox c = new CheckBox(this);
            c.setText(obj.name);
            c.setChecked(false);
            c.setButtonTintList(ColorStateList.valueOf(obj.color));
            ArrayList<int[]> selectedItems = db.getAllDataSelected();
            for (int[] i: selectedItems) {
                if (i[0] == obj.id && ((i[1] == 1) == (obj.sets != 0)))
                    c.setChecked(true);
            }
            c.setOnCheckedChangeListener((buttonView, isChecked) -> {
                db.setOrDelAllDataSelected(obj.id, (obj.sets != 0)? 1: 0, !isChecked);
                updateAll();
            });
            if (idx > leftItems) {
                statsLayoutRight.addView(c);
            } else {
                statsLayoutLeft.addView(c);
                idx++;
            }
        }
        updateAll();
    }
    private void updateAll() {
        graphLayout.removeAllViews();
        ArrayList<int[]> selectedItems = db.getAllDataSelected();
        // Dates
        String[] startEndDates = db.getAllDataGraphDates();
        Date dateToday = new Date();
        startDate = (startEndDates[0].equals("")) ? dateToday: new Date(Long.parseLong(startEndDates[0]));
        endDate = (startEndDates[1].equals("")) ? dateToday: new Date(Long.parseLong(startEndDates[1]));
        // Get earliest date for all data
        if (startEndDates[0].equals("")) {
            for (int[] i: selectedItems) {
                if (db.isEntriesTableExist(i[0], i[1] == 1)) {
                    ArrayList<MyObject> d = db.getTableEntries(i[0], i[1] == 1);
                    if (d.size() > 0) {
                        d.sort(Comparator.comparing(obj1 -> new Date(obj1.date)));
                        Date newDate = new Date(d.get(0).date);
                        if (newDate.before(startDate))
                            startDate = newDate;
                    }
                }
            }
        }
        startDateB.setText(Help.dateFormat(this, startDate));
        endDateB.setText(Help.dateFormat(this, endDate));
        ArrayList<MyObject> allData = new ArrayList<>();
        // Loop selected stats
        for (int[] i: selectedItems) {
            boolean isExercise = i[1] == 1;
            // Check if selected items exist
            if (db.isEntriesTableExist(i[0], isExercise)) {
                // Get data
                MyObject obj = db.getOneMainObj(i[0], isExercise);
                ArrayList<MyObject> data = db.getTableEntries(i[0], isExercise);
                data.sort(Comparator.comparing(obj1 -> new Date(obj1.date)));
                // Within date boundaries: get min/max Y values, populate XY data
                double yMin = 0.0;
                double yMax = 0.0;
                ArrayList<DataPoint> xyData = new ArrayList<>();
                for (MyObject item: data) {
                    Date d = new Date(item.date);
                    if (d.compareTo(startDate) >= 0 && d.compareTo(endDate) <= 0) {
                        item.color = obj.color;
                        allData.add(item);
                        xyData.add(new DataPoint(new Date(item.date), item.mainValue));
                        if (item.mainValue > yMax)
                            yMax = item.mainValue;
                        if (yMin == 0)
                            yMin = item.mainValue;
                        else if (item.mainValue < yMin)
                            yMin = item.mainValue;
                    }
                }
                // Graph setup
                if (xyData.size() > 1) {
                    GraphView graphView = new GraphView(this);
                    Viewport graphViewPort = graphView.getViewport();
                    graphViewPort.setXAxisBoundsManual(true);
                    graphViewPort.setMinX(startDate.getTime());
                    graphViewPort.setMaxX(endDate.getTime());
                    graphViewPort.setYAxisBoundsManual(true);
                    graphViewPort.setMinY(yMin);
                    graphViewPort.setMaxY(yMax);
                    GridLabelRenderer graphLabelRenderer = graphView.getGridLabelRenderer();
                    graphLabelRenderer.setHumanRounding(false);
                    graphLabelRenderer.setVerticalLabelsVisible(false);
                    graphLabelRenderer.setHorizontalLabelsVisible(false);
                    graphLabelRenderer.setHighlightZeroLines(false);
                    LineGraphSeries<DataPoint> series = new LineGraphSeries<>(xyData.toArray(new DataPoint[0]));
                    series.setThickness(7);
                    series.setColor(obj.color);
                    graphView.addSeries(series);
                    graphLayout.addView(graphView);
                }
            } else {
                db.setOrDelAllDataSelected(i[0], i[1], true);
            }
        }
        // Update rv list
        allData.sort(Comparator.comparing(obj1 -> new Date(obj1.date)));
        Collections.reverse(allData);
        statsView.setLayoutManager(new LinearLayoutManager(this));
        statsView.setHasFixedSize(true);
        statsView.setAdapter(new ManyStatsAdapter(this, allData));
        // Add empty graph
        if (allData.size() < 2) {
            GraphView graphView = new GraphView(this);
            GridLabelRenderer graphLabelRenderer = graphView.getGridLabelRenderer();
            graphLabelRenderer.setVerticalLabelsVisible(false);
            graphLabelRenderer.setHorizontalLabelsVisible(false);
            graphLabelRenderer.setHighlightZeroLines(false);
            graphLayout.addView(graphView);
        }
    }

    private void calendarDialog(boolean isStartDate) {
        String[] manyObjStat = db.getAllDataGraphDates();
        Calendar c = Calendar.getInstance();
        c.setTime((isStartDate)? startDate: endDate);
        DatePickerDialog dpd = new DatePickerDialog(this, R.style.DateDialog, (view1, year1, monthOfYear, dayOfMonth) -> {
            c.set(Calendar.YEAR, year1);
            c.set(Calendar.MONTH, monthOfYear);
            c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            String newDate = String.valueOf(c.getTime().getTime());
            db.setAllDataGraphDates((isStartDate)? newDate: manyObjStat[0], (!isStartDate)? newDate: manyObjStat[1]);
            updateAll();
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        dpd.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.default_t), (dialog, which) -> {
            db.setAllDataGraphDates((isStartDate)? "": manyObjStat[0], (!isStartDate)? "": manyObjStat[1]);
            updateAll();
        });
        dpd.getDatePicker().setMaxDate(System.currentTimeMillis());
        dpd.show();
        dpd.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(mainColor);
        dpd.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(mainColor);
        dpd.getButton(DatePickerDialog.BUTTON_NEUTRAL).setTextColor(mainColor);
    }

    @Override
    public void onBackPressed() {
        finish();
        startActivity(new Intent(ManyStatsActivity.this, MainActivity.class));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
