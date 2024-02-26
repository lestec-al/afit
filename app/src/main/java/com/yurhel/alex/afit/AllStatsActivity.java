package com.yurhel.alex.afit;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.widget.TextViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;

public class AllStatsActivity extends AppCompatActivity implements AllStatsClick, ClickInterface {
    ArrayList<MyObject> allData;
    DB db;
    int mainColor;
    Button startDateB, endDateB;
    Date startDate, endDate;
    RecyclerView statsView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_stats);
        // On back pressed
        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                startActivity(new Intent(AllStatsActivity.this, MainActivity.class));
                finish();
            }
        });

        mainColor = Help.getMainColor(this);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getString(R.string.all_data));
            Help.setActionBackIconColor(this, mainColor, actionBar);
        }
        db = new DB(this);

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

        updateAll();
    }

    private void updateAll() {
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
        allData = new ArrayList<>();

        // Loop selected stats
        for (int[] i: selectedItems) {
            boolean isExercise = i[1] == 1;
            // Check if selected items exist
            if (db.isEntriesTableExist(i[0], isExercise)) {
                // Get data
                MyObject obj = db.getOneMainObj(i[0], isExercise);
                ArrayList<MyObject> data = db.getTableEntries(i[0], isExercise);
                data.sort(Comparator.comparing(obj1 -> new Date(obj1.date)));

                for (MyObject item: data) {
                    Date d = new Date(item.date);
                    if (d.compareTo(startDate) >= 0 && d.compareTo(endDate) <= 0) {
                        item.color = obj.color;
                        allData.add(item);
                    }
                }
            } else {
                // Delete obj from selected (if obj in selected but deleted from db)
                db.setOrDelAllDataSelected(i[0], i[1], true);
            }
        }

        // Sort main data
        ArrayList<MyObject> dataMain = db.getTableEntries(null, true);
        dataMain.addAll(db.getTableEntries(null, false));

        LinkedHashMap<String, Integer> pos = db.getPositions();
        try {
            dataMain.sort(Comparator.comparing(obj1 -> pos.get(obj1.id + "_" + ((obj1.sets != 0/*Is exercise*/) ? "ex" : "st"))));
        } catch (Exception ignored) {}

        // Pie chart
        FrameLayout pieLayout = findViewById(R.id.pieLayout);
        pieLayout.removeAllViews();

        int allDataSize = allData.size();
        int allPieDataSize = 0;
        int idx = 0;
        for (MyObject obj: dataMain) {
            // Loop selected stats
            for (int[] i: selectedItems) {
                boolean isExercise = i[1] == 1;
                // If item found
                if (i[0] == obj.id && (isExercise == (obj.sets != 0))) {
                    int allPreviousPieDataSize = allPieDataSize;
                    int thisDataSize = 0;
                    for (MyObject item: db.getTableEntries(i[0], isExercise)) {
                        Date d = new Date(item.date);
                        if (d.compareTo(startDate) >= 0 && d.compareTo(endDate) <= 0) {
                            thisDataSize++;
                            allPieDataSize++;
                        }
                    }
                    obj.entriesQuantity = thisDataSize;

                    if (thisDataSize != 0) {
                        @SuppressLint("InflateParams") ProgressBar pie =
                                (ProgressBar) LayoutInflater.from(this).inflate(R.layout.pie, null);
                        pie.setMax(allDataSize);
                        pie.setProgress(thisDataSize);
                        pie.setProgressTintList(ColorStateList.valueOf(obj.color));

                        // Rotate pie, set start where previous stat ended
                        if (idx != 0) pie.setRotation(360f / (((float) allDataSize) / allPreviousPieDataSize));

                        idx++;
                        pieLayout.addView(pie);
                    }
                }
            }
        }

        // Update Graph legend
        RecyclerView statsChooseView = findViewById(R.id.statsChooseRV);
        statsChooseView.setLayoutManager(new LinearLayoutManager(this));
        statsChooseView.setHasFixedSize(true);
        statsChooseView.setAdapter(new AllStatsSmallAdapter(this, dataMain, allDataSize, this));

        // All data label
        String allDataText = allDataSize + " (100%)";
        TextView allEntries = findViewById(R.id.allEntries);
        allEntries.setText(allDataText);
        TextViewCompat.setCompoundDrawableTintList(allEntries, ColorStateList.valueOf(mainColor));

        // Update rv list
        allData.sort(Comparator.comparing(obj1 -> new Date(obj1.date)));
        Collections.reverse(allData);
        statsView.setLayoutManager(new LinearLayoutManager(this));
        statsView.setHasFixedSize(true);
        statsView.setAdapter(new AllStatsAdapter(
                this,
                this,
                allData,
                ((getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) ? getColor(R.color.white) : getColor(R.color.dark)
        ));

        if (allDataSize == 0) {
            // Empty text
            findViewById(R.id.emptyTV).setVisibility(View.VISIBLE);
            // Empty graph
            @SuppressLint("InflateParams") ProgressBar pie =
                    (ProgressBar) LayoutInflater.from(this).inflate(R.layout.pie, null);
            pie.setProgress(pie.getMax());
            pie.setProgressTintList(ColorStateList.valueOf(getColor(R.color.grey_progress)));
            pieLayout.addView(pie);
        } else {
            findViewById(R.id.emptyTV).setVisibility(View.GONE);
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) getOnBackPressedDispatcher().onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClickCheckBox(int statsId, int isObjExercise, Boolean isCheck) {
        db.setOrDelAllDataSelected(statsId, isObjExercise, isCheck);
        updateAll();
    }

    @Override
    public void onClickItem(int pos, String option) {
        showEntryDialog(allData.get(pos), (option.equals("ex")) ? "ex": "st");
    }

    private void showEntryDialog(MyObject passObj, String type) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_show_ex);
        ColorStateList mainColorS = ColorStateList.valueOf(Help.getMainColor(this));

        TextView mainValueTV = dialog.findViewById(R.id.mainValue);
        TextViewCompat.setCompoundDrawableTintList(mainValueTV, mainColorS);
        mainValueTV.setText(String.valueOf((type.equals("st")) ? passObj.mainValue : (int) passObj.mainValue));

        if (passObj.longerValue.length() > 0) {
            TextView addValueTV = dialog.findViewById(R.id.addValue);
            TextViewCompat.setCompoundDrawableTintList(addValueTV, mainColorS);
            addValueTV.setText(passObj.longerValue);
        } else {
            dialog.findViewById(R.id.addValue).setVisibility(View.GONE);
        }

        Button pickDate = dialog.findViewById(R.id.pickDateButton);
        pickDate.setText(Help.dateFormat(this, new Date(passObj.date)));
        pickDate.setEnabled(false);

        if (type.equals("ex")) {
            // Exercise only
            TextView timeTV = dialog.findViewById(R.id.time);
            TextViewCompat.setCompoundDrawableTintList(timeTV, mainColorS);
            timeTV.setText(passObj.time);

            if (!passObj.allWeights.equals("")) {
                TextView addValue2TV = dialog.findViewById(R.id.addValue2);
                TextViewCompat.setCompoundDrawableTintList(addValue2TV, mainColorS);
                addValue2TV.setVisibility(View.VISIBLE);
                addValue2TV.setText(passObj.allWeights);
            }
        } else {
            // Stats only
            dialog.findViewById(R.id.time).setVisibility(View.GONE);
        }

        dialog.findViewById(R.id.deleteButton).setVisibility(View.GONE);
        dialog.show();
    }
}
