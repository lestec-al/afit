package com.yurhel.alex.afit.stats;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
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
import android.widget.ImageButton;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.widget.TextViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.LabelFormatter;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.yurhel.alex.afit.MainActivity;
import com.yurhel.alex.afit.R;
import com.yurhel.alex.afit.core.Click;
import com.yurhel.alex.afit.core.DB;
import com.yurhel.alex.afit.core.Help;
import com.yurhel.alex.afit.core.Obj;
import com.yurhel.alex.afit.databinding.ActivityStatsBinding;
import com.yurhel.alex.afit.databinding.DialogStatsBinding;
import com.yurhel.alex.afit.databinding.DialogExerciseBinding;
import com.yurhel.alex.afit.edit.EditActivity;
import com.yurhel.alex.afit.training.TrainingActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class StatsActivity extends AppCompatActivity implements Click {
    DB db;
    boolean isExercise;
    int oneID;
    ArrayList<Obj> data;
    Obj obj;
    Date targetDate;
    Date startDate, endDate;
    Menu upMenu;
    ActionBar actionBar;
    Integer autoScrollPos = null;
    Boolean withWeight;
    ActivityStatsBinding views;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        views = ActivityStatsBinding.inflate(getLayoutInflater());
        setContentView(views.getRoot());

        // On back pressed
        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                startActivity(new Intent(StatsActivity.this, MainActivity.class));
                finish();
            }
        });

        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("");
        }
        int exerciseID = getIntent().getIntExtra("ex_id", 0);
        isExercise = exerciseID != 0;
        oneID = (isExercise) ? exerciseID : getIntent().getIntExtra("st_id", 0);
        long dataFromIntent = getIntent().getLongExtra("target_date", 0);
        targetDate = (dataFromIntent != 0) ? new Date(dataFromIntent) : new Date();

        db = new DB(this);

        // Is weights shown
        withWeight = db.getIsWeightShowForMainStat(oneID + "_" + ((isExercise) ? "ex" : "st"));

        // Date buttons
        views.buttonDateStart.setOnClickListener(v -> calendarDialog(startDate, views.buttonDateStart, true));
        views.buttonDateEnd.setOnClickListener(v -> calendarDialog(endDate, views.buttonDateEnd, false));
        // Show/Hide graph button

        views.buttonGraphVisibility.setOnClickListener(v -> {
            if (views.graphLayout.getVisibility() == View.VISIBLE) {
                views.graphLayout.setVisibility(View.GONE);
                views.buttonGraphVisibility.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_arrow_down));
            } else {
                views.graphLayout.setVisibility(View.VISIBLE);
                views.buttonGraphVisibility.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_arrow_up));
            }
            views.buttonGraphVisibility.setColorFilter(obj.color);
        });
        // Auto scroll
        views.statsRV.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (autoScrollPos != null) {
                    RecyclerView.ViewHolder selectedItem = views.statsRV.findViewHolderForAdapterPosition(autoScrollPos);
                    if (selectedItem != null) {
                        autoScrollPos = null;
                        highlightRVItem(selectedItem.itemView);
                    }
                }
            }
        });
        //
        updateAll(true);

        if (getIntent().getBooleanExtra("workoutIsOver", false)) {
            entryDialog(db.getLastExerciseEntry(oneID), "ex", true);
        }
    }

    private void updateAll(boolean onCreate) {
        obj = db.getOneMainObj(oneID, isExercise);
        if (!onCreate) setUpActionBar();
        views.buttonGraphVisibility.setColorFilter(obj.color);
        data = db.getTableEntries(oneID, isExercise);
        int dataSize = data.size();
        if (dataSize > 0)
            data.sort(Comparator.comparing(obj -> new Date(obj.date)));
        // Setup date boundaries
        Date dateToday = new Date();
        if (obj.start.equals(""))
            startDate = (dataSize > 0) ? new Date(data.get(0).date): dateToday;
        else
            startDate = new Date(Long.parseLong(obj.start));
        if (obj.end.equals(""))
            endDate = (dataSize > 0) ? new Date(data.get(dataSize-1).date): dateToday;
        else
            endDate = new Date(Long.parseLong(obj.end));
        views.buttonDateStart.setText(Help.dateFormat(this, startDate));
        views.buttonDateEnd.setText(Help.dateFormat(this, endDate));
        Help.setButtonsTextColor(obj.color, new Button[] {views.buttonDateStart, views.buttonDateEnd});
        // Cut data within date boundaries + get short info
        int oneSetMax = 0;
        double statsMin = 0.0;
        double statsMax = 0.0;
        ArrayList<Obj> newData = new ArrayList<>();
        ArrayList<DataPoint> xyData = new ArrayList<>();
        for (Obj item: data) {
            Date d = new Date(item.date);
            if (d.compareTo(startDate) >= 0 && d.compareTo(endDate) <= 0) {
                newData.add(item);
                xyData.add(new DataPoint(new Date(item.date), item.mainValue));
                // Get short info
                if (isExercise) {
                    for (String i: item.longerValue.split(" ")) {
                        if (!i.equals("+")) {
                            int iInt = Integer.parseInt(i);
                            if (iInt > oneSetMax)
                                oneSetMax = iInt;
                        }
                    }
                }
                if (item.mainValue > statsMax)
                    statsMax = item.mainValue;
                if (statsMin == 0)
                    statsMin = item.mainValue;
                else if (item.mainValue < statsMin)
                    statsMin = item.mainValue;
            }
        }
        data = newData;
        // Update short info
        views.allEntriesText.setText(String.valueOf(data.size()));
        views.record1.setText((isExercise)? String.valueOf((int) statsMax) : String.valueOf(statsMax));
        views.record2.setText((isExercise)? String.valueOf(oneSetMax) : String.valueOf(statsMin));
        ((isExercise)? views.recordMinLabel : views.comma).setVisibility(View.GONE);
        int mainColor = getColor(R.color.on_background);
        views.allEntriesLabel.setColorFilter(mainColor);
        views.recordMaxLabel.setColorFilter(mainColor);
        views.recordMinLabel.setColorFilter(mainColor);
        if (isExercise && oneSetMax > 0) db.updateExercise(obj.name, oneID, obj.rest, oneSetMax+1, obj.sets, obj.weight, obj.color);
        // Graph setup
        views.graphViewLayout.removeAllViews();
        GraphView graphView = new GraphView(this);
        views.graphViewLayout.addView(graphView);
        if (xyData.size() > 1) {
            Viewport graphViewPort = graphView.getViewport();
            graphViewPort.setXAxisBoundsManual(true);
            graphViewPort.setMinX(startDate.getTime());
            graphViewPort.setMaxX(endDate.getTime());
            graphViewPort.setYAxisBoundsManual(true);
            graphViewPort.setMinY(statsMin);
            graphViewPort.setMaxY(statsMax);
            GridLabelRenderer graphLabelRenderer = graphView.getGridLabelRenderer();
            graphLabelRenderer.setHumanRounding(false);
            graphLabelRenderer.setHorizontalLabelsVisible(false);
            graphLabelRenderer.setHighlightZeroLines(false);
            graphLabelRenderer.setLabelFormatter(new LabelFormatter() {
                @SuppressLint("DefaultLocale")
                @Override
                public String formatLabel(double value, boolean isValueX) {
                    return (!isValueX)? String.format("%1.1f", value): null;
                }
                @Override
                public void setViewport(Viewport viewport) {}
            });
            LineGraphSeries<DataPoint> series = new LineGraphSeries<>(xyData.toArray(new DataPoint[0]));
            series.setThickness(7);
            series.setColor(obj.color);
            series.setOnDataPointTapListener((series1, dataPoint) -> {
                int pos = 0;
                for (Obj item: data) {
                    if (item.date == (long)dataPoint.getX() && item.mainValue == dataPoint.getY())
                        break;
                    pos++;
                }
                RecyclerView.ViewHolder selectedItem = views.statsRV.findViewHolderForAdapterPosition(pos);
                if (selectedItem != null) {
                    highlightRVItem(selectedItem.itemView);
                } else {
                    views.statsRV.smoothScrollToPosition(pos);
                    autoScrollPos = pos;
                }
            });
            graphView.addSeries(series);
        } else {
            GridLabelRenderer graphLabelRenderer = graphView.getGridLabelRenderer();
            graphLabelRenderer.setVerticalLabelsVisible(false);
            graphLabelRenderer.setHorizontalLabelsVisible(false);
            graphLabelRenderer.setHighlightZeroLines(false);
        }
        // Update rv list
        Collections.reverse(data);
        views.statsRV.setLayoutManager(new LinearLayoutManager(this));
        views.statsRV.setHasFixedSize(true);
        views.statsRV.setAdapter(new StatsAdapter(this, data, this));
        // Empty text
        views.emptyTV.setVisibility((data.size() == 0) ? View.VISIBLE : View.GONE);
    }

    // DIALOGS
    private void entryDialog(Obj passObj, String type, boolean startAfterWorkout) {
        Dialog dialog = new Dialog(this);

        if (type.equals("st")) {
            // Stats entry dialog
            DialogStatsBinding dViews = DialogStatsBinding.inflate(getLayoutInflater());
            dialog.setContentView(dViews.getRoot());

            dViews.editNote.setVisibility(View.VISIBLE);

            dViews.numberPicker0.setMaxValue(99999);
            dViews.numberPicker0.setMinValue(1);
            dViews.numberPicker0.setWrapSelectorWheel(false);
            dViews.numberPicker0.setOnValueChangedListener((picker, oldVal, newVal) -> {
                if (newVal == dViews.numberPicker0.getMaxValue()) dViews.numberPicker0.setMaxValue(newVal+100);
            });

            dViews.numberPicker1.setMaxValue(9);
            dViews.numberPicker1.setMinValue(0);
            dViews.numberPicker1.setWrapSelectorWheel(false);

            if (passObj == null) {
                // Create
                dViews.pickDateButton.setText(Help.dateFormat(this, targetDate));
                dViews.pickDateButton.setOnClickListener(view -> calendarDialog(targetDate, dViews.pickDateButton, null));
                dViews.pickDateButton.setTextColor(obj.color);
                if (data.size() > 0) {
                    dViews.numberPicker0.setValue((int) data.get(0).mainValue);
                    dViews.numberPicker1.setValue(Integer.parseInt(String.valueOf(data.get(0).mainValue).split("\\.")[1]));
                } else {
                    dViews.numberPicker0.setValue(0);
                    dViews.numberPicker1.setValue(0);
                }
            } else {
                // Edit
                dViews.pickDateButton.setText(Help.dateFormat(this, new Date(passObj.date)));
                dViews.pickDateButton.setEnabled(false);
                dViews.editNote.setText(passObj.longerValue);
                dViews.numberPicker0.setValue((int) passObj.mainValue);
                dViews.numberPicker1.setValue(Integer.parseInt(String.valueOf(passObj.mainValue).split("\\.")[1]));
            }

            if (dViews.numberPicker0.getValue() == dViews.numberPicker0.getMaxValue())
                dViews.numberPicker0.setMaxValue(dViews.numberPicker0.getValue()+100);

            dViews.okButton.setColorFilter(obj.color);
            dViews.okButton.setOnClickListener(view -> {
                Double s = Double.parseDouble(String.format("%1$s.%2$s", dViews.numberPicker0.getValue(), dViews.numberPicker1.getValue()));
                if (passObj == null) db.addStatsEntry(oneID, s, String.valueOf(targetDate.getTime()), dViews.editNote.getText().toString());
                else db.updateStatsEntry(passObj.id, oneID, s, dViews.editNote.getText().toString());
                dialog.cancel();
                updateAll(false);
            });

            dViews.cancelButton.setColorFilter(obj.color);
            dViews.cancelButton.setOnClickListener(view -> dialog.cancel());

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

            if (!passObj.allWeights.equals("")) {
                String weightStr = getText(R.string.weight) + ": " + passObj.allWeights;
                dViews.addValue2.setText(weightStr);
                dViews.addValue2.setVisibility(View.VISIBLE);
                TextViewCompat.setCompoundDrawableTintList(dViews.addValue2, mainColorS);
            }

            dViews.pickDateButton.setText(Help.dateFormat(this, new Date(passObj.date)));
            dViews.pickDateButton.setEnabled(false);

            if (startAfterWorkout) {
                dViews.workoutOverText.setVisibility(View.VISIBLE);
                dViews.actionsLayout.setVisibility(View.GONE);
            }
        }

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        if (passObj != null) {
            // We have 2 dialog binding classes, they both had this button, but only one class passed to this point
            ImageButton delete = dialog.findViewById(R.id.deleteButton);
            delete.setVisibility(View.VISIBLE);
            delete.setColorFilter(obj.color);
            delete.setOnClickListener(v -> {
                Object[] askDeleteDialog = Help.editDialog(
                        this,
                        obj.color,
                        R.string.delete_entry_info,
                        "",
                        null,
                        false
                );
                ((ImageButton)askDeleteDialog[1]).setOnClickListener(v1 -> {
                    ((Dialog)askDeleteDialog[0]).cancel();
                    dialog.dismiss();
                    db.deleteSmallObj(oneID, passObj.id, isExercise);
                    updateAll(false);
                });
                ((Dialog)askDeleteDialog[0]).show();
            });
        }

        dialog.show();
    }

    private void calendarDialog(Date oldDate, Button dateButton, Boolean start) {
        Calendar c = Calendar.getInstance();
        if (oldDate != null)
            c.setTime(oldDate);
        DatePickerDialog dpd = new DatePickerDialog(
                this,
                R.style.DateDialog,
                (view1, year1, monthOfYear, dayOfMonth) -> {
                    c.set(Calendar.YEAR, year1);
                    c.set(Calendar.MONTH, monthOfYear);
                    c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    Date newDate = c.getTime();
                    dateButton.setText(Help.dateFormat(this, newDate));
                    if (start != null) {
                        db.setDate(String.valueOf(newDate.getTime()), oneID, isExercise, start);
                        updateAll(false);
                    } else {
                        targetDate = newDate;
                    }
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        );
        if (start != null) {
            dpd.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.default_t), (dialog, which) -> {
                db.setDate("", oneID, isExercise, start);
                updateAll(false);
            });
        }
        dpd.getDatePicker().setMaxDate(System.currentTimeMillis());
        dpd.show();
        Drawable dOk = AppCompatResources.getDrawable(this, R.drawable.ic_ok);
        if (dOk != null) {
            dOk.setTint(obj.color);
            dpd.getButton(DatePickerDialog.BUTTON_POSITIVE).setCompoundDrawablesWithIntrinsicBounds(dOk, null, null, null);
            dpd.getButton(DatePickerDialog.BUTTON_POSITIVE).setText("");
        }
        Drawable dNo = AppCompatResources.getDrawable(this, R.drawable.ic_no);
        if (dNo != null) {
            dNo.setTint(obj.color);
            dpd.getButton(DatePickerDialog.BUTTON_NEGATIVE).setCompoundDrawablesWithIntrinsicBounds(dNo, null, null, null);
            dpd.getButton(DatePickerDialog.BUTTON_NEGATIVE).setText("");
        }
        if (start != null) {
            Drawable dDef = AppCompatResources.getDrawable(this, R.drawable.ic_auto);
            if (dDef != null) {
                dDef.setTint(obj.color);
                dpd.getButton(DatePickerDialog.BUTTON_NEUTRAL).setCompoundDrawablesWithIntrinsicBounds(dDef, null, null, null);
                dpd.getButton(DatePickerDialog.BUTTON_NEUTRAL).setText("");
            }
        }
    }

    // NAVIGATION
    @Override
    public void onClickItem(int pos, String option) {
        entryDialog(data.get(pos), (isExercise) ? "ex": "st", false);
    }

    // TOOLBAR
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.stats, menu);
        upMenu = menu;

        // Feature highlight
        @SuppressLint("InflateParams") ImageButton b = (ImageButton) LayoutInflater.from(this).inflate(
                (isExercise) ? R.layout.view_workout_button : R.layout.view_add_button,
                null
        );
        b.setTooltipText(getString((isExercise) ? R.string.start_workout : R.string.add_st));
        b.setOnClickListener(v -> clickOnAddActionButton());
        if (db.getTableEntries(oneID, isExercise).isEmpty()) {
            TapTargetView.showFor(
                    this,
                    TapTarget.forView(b, getString((isExercise) ? R.string.start_workout : R.string.add_st), "")
                            .outerCircleColorInt(obj.color)
                            .textColor(R.color.white)
                            .drawShadow(true)
                            .cancelable(true)
                            .tintTarget(true)
                            .targetRadius(30),
                    new TapTargetView.Listener() {
                        @Override
                        public void onTargetClick(TapTargetView view) {
                            super.onTargetClick(view);
                            clickOnAddActionButton();
                        }
                    }
            );
        }
        upMenu.findItem(R.id.actionAdd).setActionView(b);

        setUpActionBar();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;

        } else if (item.getItemId() == R.id.actionSettings) {
            startActivity(new Intent(StatsActivity.this, EditActivity.class)
                    .putExtra((isExercise) ? "ex_id" : "st_id", oneID)
                    .putExtra("target_date", targetDate.getTime())
            );
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // HELPS
    private void clickOnAddActionButton() {
        if (isExercise) {
            startActivity(new Intent(StatsActivity.this, TrainingActivity.class)
                    .putExtra("ex_id", oneID)
                    .putExtra("date", targetDate.getTime())
                    .putExtra("withWeight", withWeight)
            );
            finish();
        } else {
            entryDialog(null, "st", false);
        }
    }

    private void setUpActionBar() {
        actionBar.setTitle(obj.name);
        Help.setActionBackIconColor(this, obj.color, actionBar);

        // Set color to action settings
        MenuItem action = upMenu.findItem(R.id.actionSettings);
        Drawable drawable1 = action.getIcon();
        if (drawable1 != null) {
            Drawable drawable2 = DrawableCompat.wrap(drawable1);
            drawable2.setTint(obj.color);
            action.setIcon(drawable2);
        }
        // Set color to action add
        ImageButton b = (ImageButton) upMenu.findItem(R.id.actionAdd).getActionView();
        if (b != null) b.setColorFilter(obj.color);
    }

    private void highlightRVItem(View view) {
        new Thread(() -> {
            runOnUiThread(() -> view.setBackground(AppCompatResources.getDrawable(StatsActivity.this, R.drawable.highlight)));
            try {
                TimeUnit.MILLISECONDS.sleep(300);
            } catch (Exception ignored) {}
            runOnUiThread(() -> view.setBackground(AppCompatResources.getDrawable(StatsActivity.this, R.drawable.divider)));
        }).start();
    }
}