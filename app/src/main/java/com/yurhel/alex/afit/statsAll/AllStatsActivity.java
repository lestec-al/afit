package com.yurhel.alex.afit.statsAll;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.widget.TextViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.yurhel.alex.afit.MainActivity;
import com.yurhel.alex.afit.R;
import com.yurhel.alex.afit.core.Click;
import com.yurhel.alex.afit.core.DB;
import com.yurhel.alex.afit.core.Help;
import com.yurhel.alex.afit.core.Obj;
import com.yurhel.alex.afit.databinding.ActivityAllStatsBinding;
import com.yurhel.alex.afit.databinding.DialogExerciseBinding;
import com.yurhel.alex.afit.databinding.DialogStatsBinding;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;

public class AllStatsActivity extends AppCompatActivity implements AllStatsClick, Click {
    ArrayList<Obj> allData;
    DB db;
    int mainColor;
    Date startDate, endDate;
    Button weekPointsButton, allPointsButton;
    ActivityAllStatsBinding views;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        views = ActivityAllStatsBinding.inflate(getLayoutInflater());
        setContentView(views.getRoot());
        // On back pressed
        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                startActivity(new Intent(AllStatsActivity.this, MainActivity.class));
                finish();
            }
        });

        mainColor = getColor(R.color.on_background);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setTitle("");
        db = new DB(this);

        // Date buttons
        views.buttonDateStart.setOnClickListener(v -> calendarDialog(true));
        views.buttonDateEnd.setOnClickListener(v -> calendarDialog(false));
        Help.setButtonsTextColor(mainColor, new Button[] {views.buttonDateStart, views.buttonDateEnd});

        // Show/Hide graph button
        views.buttonGraphVisibility.setColorFilter(mainColor);
        views.buttonGraphVisibility.setOnClickListener(v -> {
            if (views.layoutForHide.getVisibility() == View.VISIBLE) {
                views.layoutForHide.setVisibility(View.GONE);
                views.buttonGraphVisibility.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_arrow_down));
            } else {
                views.layoutForHide.setVisibility(View.VISIBLE);
                views.buttonGraphVisibility.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_arrow_up));
            }
        });

        // Bottom navigation
        Help.setupBottomNavigation(AllStatsActivity.this, R.id.actionGraph, views.navigation, this::finish);

        updateAll();
    }

    @SuppressLint("InflateParams")
    private void updateAll() {
        ArrayList<int[]> selectedItems = db.getAllDataSelected();

        // Dates
        String[] startEndDates = db.getAllDataGraphDates();
        Date dateToday = new Date();
        startDate = (startEndDates[0].isEmpty()) ? dateToday: new Date(Long.parseLong(startEndDates[0]));
        endDate = (startEndDates[1].isEmpty()) ? dateToday: new Date(Long.parseLong(startEndDates[1]));
        boolean startDateNotExist = startDate == dateToday;

        allData = new ArrayList<>();

        // Loop data for RV
        for (int[] i: selectedItems) {
            boolean isExercise = i[1] == 1;
            // Check if selected items exist
            if (db.isEntriesTableExist(i[0], isExercise)) {
                // Get data
                Obj obj = db.getOneMainObj(i[0], isExercise);
                ArrayList<Obj> data = db.getTableEntries(i[0], isExercise);
                data.sort(Comparator.comparing(obj1 -> new Date(obj1.date)));

                for (Obj item: data) {
                    Date d = new Date(item.date);

                    if (d.compareTo(endDate) <= 0) {
                        if (startDateNotExist || d.compareTo(startDate) >= 0) {
                            item.color = obj.color;
                            allData.add(item);

                            // Get earliest date for all data
                            if (startDateNotExist && startDate.after(d)) {
                                startDate = d;
                            }
                        }
                    }
                }
            } else {
                // Delete obj from selected (if obj in selected but deleted from db)
                db.setOrDelAllDataSelected(i[0], i[1], true);
            }
        }

        views.buttonDateStart.setText(Help.dateFormat(this, startDate));
        views.buttonDateEnd.setText(Help.dateFormat(this, endDate));

        // Sort main data
        ArrayList<Obj> dataMain = db.getTableEntries(null, true);
        dataMain.addAll(db.getTableEntries(null, false));
        try {
            LinkedHashMap<String, Integer> pos = db.getPositions();
            dataMain.sort(Comparator.comparing(obj1 -> pos.get(obj1.id + "_" + ((obj1.sets != 0/*Is exercise*/) ? "ex" : "st"))));
        } catch (Exception ignored) {}

        // Init score ???
        int allTimeMin = 0;
        int allTimeSec = 0;
        int lastWeekMin = 0;
        int lastWeekSec = 0;
        int nowWeek = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR);
        int nowYear = Calendar.getInstance().get(Calendar.YEAR);
        Calendar objDate = Calendar.getInstance();

        // Setup pie chart
        views.pieLayout.removeAllViews();
        int allDataSize = allData.size();
        int allPieDataSize = 0;
        int idx = 0;

        // Loop data for pie chart & scores
        for (Obj obj: dataMain) {
            // Loop selected stats
            for (int[] i: selectedItems) {
                boolean isExercise = i[1] == 1;
                // If item found
                if (i[0] == obj.id && (isExercise == (obj.sets != 0))) {
                    int previousAllPieDataSize = allPieDataSize;
                    int thisDataSize = 0;
                    for (Obj item: db.getTableEntries(i[0], isExercise)) {
                        Date d = new Date(item.date);
                        if (d.compareTo(startDate) >= 0 && d.compareTo(endDate) <= 0) {
                            thisDataSize++;
                            allPieDataSize++;

                            // Get score ???
                            if (isExercise) {
                                String[] t = item.time.split(":");
                                allTimeMin += Integer.parseInt(t[0]);
                                allTimeSec += Integer.parseInt(t[1]);

                                objDate.setTimeInMillis(item.date);
                                if (nowWeek == objDate.get(Calendar.WEEK_OF_YEAR) && nowYear == objDate.get(Calendar.YEAR)) {
                                    lastWeekMin += Integer.parseInt(t[0]);
                                    lastWeekSec += Integer.parseInt(t[1]);
                                }
                            }
                        }
                    }
                    obj.entriesQuantity = thisDataSize;

                    if (thisDataSize != 0) {
                        ProgressBar pie = (ProgressBar) LayoutInflater.from(this).inflate(R.layout.pie, null);
                        pie.setMax(allDataSize);
                        pie.setProgress(thisDataSize);
                        pie.setProgressTintList(ColorStateList.valueOf(obj.color));

                        // Rotate pie, set start where previous stat ended
                        if (idx != 0) pie.setRotation(360f / (((float) allDataSize) / previousAllPieDataSize));

                        idx++;
                        views.pieLayout.addView(pie);
                    }
                }
            }
        }

        // Update score ???
        if (allPointsButton != null) allPointsButton.setText(
                String.valueOf(allTimeMin + (allTimeSec / 60) + ((allTimeSec % 60 > 0) ? 1 : 0))
        );
        if (weekPointsButton != null) weekPointsButton.setText(
                String.valueOf(lastWeekMin + (lastWeekSec / 60) + ((lastWeekSec % 60 > 0) ? 1 : 0))
        );

        // Update Graph legend
        views.statsChooseRV.setLayoutManager(new LinearLayoutManager(this));
        views.statsChooseRV.setHasFixedSize(true);
        views.statsChooseRV.setAdapter(new AllStatsSmallAdapter(this, dataMain, allDataSize, this));

        // All data label
        views.allEntries.setText(String.valueOf(allDataSize));
        TextViewCompat.setCompoundDrawableTintList(views.allEntries, ColorStateList.valueOf(mainColor));

        // Update rv list
        allData.sort(Comparator.comparing(obj1 -> new Date(obj1.date)));
        Collections.reverse(allData);
        views.statsRV.setLayoutManager(new LinearLayoutManager(this));
        views.statsRV.setHasFixedSize(true);
        views.statsRV.setAdapter(new AllStatsAdapter(
                this,
                this,
                allData,
                ((getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES)
                        ? getColor(R.color.white) : getColor(R.color.dark)
        ));

        if (allDataSize == 0) {
            // Empty text
            views.emptyTV.setVisibility(View.VISIBLE);
            // Empty graph
            ProgressBar pie = (ProgressBar) LayoutInflater.from(this).inflate(R.layout.pie, null);
            pie.setProgress(pie.getMax());
            pie.setProgressTintList(ColorStateList.valueOf(getColor(R.color.grey_progress)));
            views.pieLayout.addView(pie);
        } else {
            views.emptyTV.setVisibility(View.GONE);
        }
    }

    // DIALOGS
    private void calendarDialog(boolean isStartDate) {
        String[] manyObjStat = db.getAllDataGraphDates();
        Calendar c = Calendar.getInstance();
        c.setTime((isStartDate)? startDate: endDate);
        DatePickerDialog dpd = new DatePickerDialog(
                this,
                R.style.DateDialog,
                (view1, year1, monthOfYear, dayOfMonth) -> {
                    c.set(Calendar.YEAR, year1);
                    c.set(Calendar.MONTH, monthOfYear);
                    c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    String newDate = String.valueOf(c.getTime().getTime());
                    db.setAllDataGraphDates((isStartDate)? newDate: manyObjStat[0], (!isStartDate)? newDate: manyObjStat[1]);
                    updateAll();
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        );
        dpd.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.default_t), (dialog, which) -> {
            db.setAllDataGraphDates((isStartDate)? "": manyObjStat[0], (!isStartDate)? "": manyObjStat[1]);
            updateAll();
        });
        dpd.getDatePicker().setMaxDate(System.currentTimeMillis());
        dpd.show();
        Drawable dOk = AppCompatResources.getDrawable(this, R.drawable.ic_ok);
        if (dOk != null) {
            dpd.getButton(DatePickerDialog.BUTTON_POSITIVE)
                    .setCompoundDrawablesWithIntrinsicBounds(dOk, null, null, null);
            dpd.getButton(DatePickerDialog.BUTTON_POSITIVE).setText("");
        }
        Drawable dNo = AppCompatResources.getDrawable(this, R.drawable.ic_no);
        if (dNo != null) {
            dpd.getButton(DatePickerDialog.BUTTON_NEGATIVE)
                    .setCompoundDrawablesWithIntrinsicBounds(dNo, null, null, null);
            dpd.getButton(DatePickerDialog.BUTTON_NEGATIVE).setText("");
        }
        Drawable dDef = AppCompatResources.getDrawable(this, R.drawable.ic_auto);
        if (dDef != null) {
            dpd.getButton(DatePickerDialog.BUTTON_NEUTRAL)
                    .setCompoundDrawablesWithIntrinsicBounds(dDef, null, null, null);
            dpd.getButton(DatePickerDialog.BUTTON_NEUTRAL).setText("");
        }
    }

    private void showEntryDialog(Obj passObj, String type) {
        Dialog dialog = new Dialog(this);

        if (type.equals("st")) {
            // Stats entry dialog
            DialogStatsBinding dViews = DialogStatsBinding.inflate(getLayoutInflater());
            dialog.setContentView(dViews.getRoot());

            dViews.editNote.setVisibility(View.VISIBLE);
            dViews.editNote.setText(passObj.longerValue);
            dViews.editNote.setEnabled(false);

            dViews.numberPicker0.setVisibility(View.GONE);
            dViews.numberPicker1.setVisibility(View.GONE);
            dViews.mainValue.setVisibility(View.VISIBLE);
            dViews.mainValue.setText(String.valueOf(passObj.mainValue));

            dViews.pickDateButton.setText(Help.dateFormat(this, new Date(passObj.date)));
            dViews.pickDateButton.setEnabled(false);

            if (dViews.numberPicker0.getValue() == dViews.numberPicker0.getMaxValue())
                dViews.numberPicker0.setMaxValue(dViews.numberPicker0.getValue()+100);

            dViews.okButton.setVisibility(View.GONE);
            dViews.cancelButton.setVisibility(View.GONE);

        } else if (type.equals("ex") && passObj != null) {
            // Exercise entry dialog
            DialogExerciseBinding dViews = DialogExerciseBinding.inflate(getLayoutInflater());
            dialog.setContentView(dViews.getRoot());
            ColorStateList mainColorS = ColorStateList.valueOf(getColor(R.color.on_background));

            String durationStr = " " + passObj.time + " ";
            dViews.time.setText(durationStr);
            TextViewCompat.setCompoundDrawableTintList(dViews.time, mainColorS);

            String resultStr = " " + (int) passObj.mainValue + " ";
            dViews.mainValue.setText(resultStr);
            TextViewCompat.setCompoundDrawableTintList(dViews.mainValue, mainColorS);

            dViews.addValue.setText(passObj.longerValue);
            TextViewCompat.setCompoundDrawableTintList(dViews.addValue, mainColorS);

            if (!passObj.allWeights.isEmpty()) {
                String weightStr = getText(R.string.weight) + ": " + passObj.allWeights;
                dViews.addValue2.setText(weightStr);
                dViews.addValue2.setVisibility(View.VISIBLE);
                TextViewCompat.setCompoundDrawableTintList(dViews.addValue2, mainColorS);
            }

            dViews.pickDateButton.setText(Help.dateFormat(this, new Date(passObj.date)));
            dViews.pickDateButton.setEnabled(false);

            dViews.deleteButton.setVisibility(View.GONE);
        }

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        dialog.show();
    }

    // NAVIGATION
    @SuppressLint("InflateParams")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.stats_all, menu);

        weekPointsButton = (Button) LayoutInflater.from(this).inflate(R.layout.view_point_button, null);
        weekPointsButton.setTooltipText(getText(R.string.week_points_info));
        weekPointsButton.setOnClickListener(v -> weekPointsButton.performLongClick());
        Drawable dO = AppCompatResources.getDrawable(this, R.drawable.ic_time);
        if (dO != null) weekPointsButton.setCompoundDrawablesWithIntrinsicBounds(dO, null, null, null);
        menu.findItem(R.id.actionWeekPoints).setActionView(weekPointsButton);

        allPointsButton = (Button) LayoutInflater.from(this).inflate(R.layout.view_point_button, null);
        allPointsButton.setTooltipText(getText(R.string.all_fit_points_info));
        allPointsButton.setOnClickListener(v -> allPointsButton.performLongClick());
        Drawable dF = AppCompatResources.getDrawable(this, R.drawable.ic_winner);
        if (dF != null) allPointsButton.setCompoundDrawablesWithIntrinsicBounds(dF, null, null, null);
        menu.findItem(R.id.actionAllPoints).setActionView(allPointsButton);

        updateAll();
        return true;
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
}
