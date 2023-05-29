package com.yurhel.alex.afit;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
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

public class StatsActivity extends AppCompatActivity implements ClickInterface {
    int themeColor;
    int[] objColor;
    String objColorID;
    DB db;
    boolean exercise;
    int oneID;
    ArrayList<MyObject> data;
    MyObject obj;
    Date targetDate;
    Date startDate, endDate;
    Menu upMenu;
    ActionBar actionBar;
    ImageButton graphVisB;
    Button startDateB;
    Button endDateB;
    GraphView graphView;
    boolean nightMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        int exerciseID = getIntent().getIntExtra("ex_id", 0);
        oneID = (exerciseID != 0) ? exerciseID : getIntent().getIntExtra("st_id", 0);
        exercise = exerciseID != 0;
        objColorID = (exercise) ? oneID +"_ex": oneID +"_st";
        db = new DB(this);
        targetDate = new Date();
        graphView = findViewById(R.id.graph_view);
        nightMode = Help.isNightMode(this);
        // Date buttons
        startDateB = findViewById(R.id.button_date_start);
        startDateB.setOnClickListener(v -> calendarDialog(startDate, startDateB, true));
        endDateB = findViewById(R.id.button_date_end);
        endDateB.setOnClickListener(v -> calendarDialog(endDate, endDateB, false));
        Help.setButtonsTextColor(themeColor, new Button[] {startDateB, endDateB});
        // Show/Hide graph button
        graphVisB = findViewById(R.id.button_graph_visibility);
        graphVisB.setOnClickListener(v -> {
            LinearLayout l = findViewById(R.id.graph_layout);
            if (l.getVisibility() == View.VISIBLE) {
                l.setVisibility(View.GONE);
                graphVisB.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_g_arrow_down));
            } else {
                l.setVisibility(View.VISIBLE);
                graphVisB.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_g_arrow_up));
            }
            Help.setImageButtonsColor(themeColor, new ImageButton[] {graphVisB});
        });
        Help.setImageButtonsColor(themeColor, new ImageButton[] {graphVisB});
        //
        updateAll(true);
    }

    private void updateAll(boolean onCreate) {
        data = db.getAllEntries(oneID, exercise);
        obj = db.getOneInfo(oneID, exercise);
        objColor = db.getObjColor(objColorID);
        themeColor = Help.getMainGreyColor(this);
        themeColor = (objColor[0] != 0) ? objColor[0]: themeColor;
        if (!onCreate)
            setUpActionBar();
        graphView.removeAllSeries();
        GridLabelRenderer graphLabelRenderer = graphView.getGridLabelRenderer();
        int dataSize = data.size();
        if (dataSize > 0) {
            data.sort(Comparator.comparing(obj -> new Date(obj.date)));
            // Date boundaries
            if (obj.start.equals(""))
                startDate = new Date(data.get(0).date);
            else
                startDate = new Date(Long.parseLong(obj.start));
            if (obj.end.equals(""))
                endDate = new Date(data.get(dataSize-1).date);
            else
                endDate = new Date(Long.parseLong(obj.end));
            // Cut data within date boundaries
            ArrayList<MyObject> newData = new ArrayList<>();
            for (MyObject item: data) {
                Date d = new Date(item.date);
                if (d.compareTo(startDate) >= 0 && d.compareTo(endDate) <= 0)
                    newData.add(item);
            }
            // Update data
            data = newData;
            dataSize = data.size();
        }
        if (dataSize == 0) {
            // Date boundaries
            Date dateToday = new Date();
            if (obj.start.equals(""))
                startDate = dateToday;
            else
                startDate = new Date(Long.parseLong(obj.start));
            if (obj.end.equals(""))
                endDate = dateToday;
            else
                endDate = new Date(Long.parseLong(obj.end));
        }
        // Date buttons
        startDateB.setText(Help.dateFormat(this, startDate));
        endDateB.setText(Help.dateFormat(this, endDate));
        Help.setButtonsTextColor(themeColor, new Button[] {startDateB, endDateB});
        Help.setImageButtonsColor(themeColor, new ImageButton[] {graphVisB});
        // Update short info
        int record = 0;
        int recordSet = 0;
        double recordMin = 0.0;
        double recordMax = 0.0;
        for (MyObject item: data) {
            if (exercise) {
                int r = Integer.parseInt(item.result_s);
                if (r > recordSet)
                    recordSet = r;
                for (String i: item.result_l.split(" ")) {
                    if (!i.equals("+")) {
                        int iInt = Integer.parseInt(i);
                        if (iInt > record)
                            record = iInt;
                    }
                }
            } else {
                double r = item.value;
                if (r > recordMax)
                    recordMax = r;
                if (recordMin == 0)
                    recordMin = r;
                else if (r < recordMin)
                    recordMin = r;
            }
        }
        ((TextView) findViewById(R.id.all_entries)).setText(String.valueOf(data.size()));
        ((TextView) findViewById(R.id.record_1)).setText((exercise)?""+record:""+recordMax);
        ((TextView) findViewById(R.id.record_2)).setText((exercise)?""+recordSet:""+recordMin);
        if (exercise)
            findViewById(R.id.result_min_label).setVisibility(View.GONE);
        else
            findViewById(R.id.div_comma).setVisibility(View.GONE);
        if (nightMode) {
            int white = getColor(R.color.white);
            ((ImageView)findViewById(R.id.all_entries_label)).setColorFilter(white);
            ((ImageView)findViewById(R.id.result_max_label)).setColorFilter(white);
            ((ImageView)findViewById(R.id.result_min_label)).setColorFilter(white);
        }
        // Graph setup
        if (dataSize > 1) {
            Viewport graphViewPort = graphView.getViewport();
            graphViewPort.setXAxisBoundsManual(true);
            graphViewPort.setMinX(startDate.getTime());
            graphViewPort.setMaxX(endDate.getTime());
        }
        if (dataSize < 2)
            graphLabelRenderer.setVerticalLabelsVisible(false);
        graphLabelRenderer.setHorizontalLabelsVisible(false);
        graphLabelRenderer.setNumHorizontalLabels(1);
        graphLabelRenderer.setHighlightZeroLines(false);
        if (!nightMode) {
            graphLabelRenderer.setVerticalLabelsColor(getColor(R.color.grey_on_light));
            graphLabelRenderer.reloadStyles();
        }
        // Check data within boundaries
        if (dataSize > 0) {
            if (dataSize > 1) {
                // Update graph
                DataPoint[] XYData = new DataPoint[dataSize];
                int idx = 0;
                for (MyObject item: data) {
                    XYData[idx] = new DataPoint(new Date(item.date), (exercise)? Double.parseDouble(item.result_s): item.value);
                    idx += 1;
                }
                LineGraphSeries<DataPoint> series = new LineGraphSeries<>(XYData);
                series.setThickness(7);
                series.setColor(themeColor);
                series.setOnDataPointTapListener((series1, dataPoint) -> Snackbar.make(
                        findViewById(R.id.graph_view),
                        Help.dateFormat(this, new Date((long) dataPoint.getX()))+" - "+dataPoint.getY(),
                        Snackbar.LENGTH_LONG
                ).show());
                graphView.addSeries(series);
            }
            // Update list
            Collections.reverse(data);
            RecyclerView statsView = findViewById(R.id.stats_view);
            statsView.setLayoutManager(new LinearLayoutManager(this));
            statsView.setHasFixedSize(true);
            statsView.setAdapter(new StatsAdapter(this, data, exercise, this));
        }
    }

    // DIALOGS
    private void statsDialog(MyObject passObj) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_stats);
        Button pickDate = dialog.findViewById(R.id.button_pick_date_stats_dialog);
        Button ok = dialog.findViewById(R.id.button_create_stats_dialog);
        Button cancel = dialog.findViewById(R.id.button_cancel_stats_dialog);
        NumberPicker numberPicker0 = dialog.findViewById(R.id.number_picker_0);
        NumberPicker numberPicker1 = dialog.findViewById(R.id.number_picker_1);
        numberPicker0.setMaxValue(99999);
        numberPicker0.setMinValue(1);
        numberPicker0.setWrapSelectorWheel(false);
        numberPicker0.setOnValueChangedListener((picker, oldVal, newVal) -> {
            if (newVal == numberPicker0.getMaxValue())
                numberPicker0.setMaxValue(newVal+100);
        });
        numberPicker1.setMaxValue(9);
        numberPicker1.setMinValue(0);
        numberPicker1.setWrapSelectorWheel(false);
        if (passObj == null) {
            if (data.size() > 0) {
                numberPicker0.setValue((int) data.get(0).value);
                numberPicker1.setValue(Integer.parseInt(String.valueOf(data.get(0).value).split("\\.")[1]));
            } else {
                numberPicker0.setValue(0);
                numberPicker1.setValue(0);
            }
            pickDate.setText(Help.dateFormat(this, targetDate));
            pickDate.setOnClickListener(view -> calendarDialog(targetDate, pickDate, null));
            Help.setButtonsTextColor(themeColor, new Button[] {pickDate, ok, cancel});
        } else {
            numberPicker0.setValue((int) passObj.value);
            numberPicker1.setValue(Integer.parseInt(String.valueOf(passObj.value).split("\\.")[1]));
            pickDate.setText(Help.dateFormat(this, new Date(passObj.date)));
            pickDate.setEnabled(false);
            Help.setButtonsTextColor(themeColor, new Button[] {ok, cancel});
        }
        if (numberPicker0.getValue() == numberPicker0.getMaxValue())
            numberPicker0.setMaxValue(numberPicker0.getValue()+100);
        ok.setOnClickListener(view -> {
            Double s = Double.parseDouble(String.format("%1$s.%2$s", numberPicker0.getValue(), numberPicker1.getValue()));
            if (passObj == null)
                db.addVStats(oneID, s, String.valueOf(targetDate.getTime()));
            else
                db.updateStatsEntry(passObj.id, oneID, s);
            dialog.cancel();
            updateAll(false);
        });
        cancel.setOnClickListener(view -> dialog.cancel());
        dialog.show();
    }

    private void calendarDialog(Date oldDate, Button dateButton, Boolean start) {
        Calendar c = Calendar.getInstance();
        if (oldDate != null)
            c.setTime(oldDate);
        DatePickerDialog dpd = new DatePickerDialog(this, R.style.DateDialog, (view1, year1, monthOfYear, dayOfMonth) -> {
            c.set(Calendar.YEAR, year1);
            c.set(Calendar.MONTH, monthOfYear);
            c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            Date newDate = c.getTime();
            dateButton.setText(Help.dateFormat(this, newDate));
            if (start != null) {
                db.saveDate(String.valueOf(newDate.getTime()), oneID, exercise, start);
                updateAll(false);
            } else {
                targetDate = newDate;
            }
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        if (start != null) {
            dpd.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.default_t), (dialog, which) -> {
                db.saveDate("", oneID, exercise, start);
                updateAll(false);
            });
        }
        dpd.getDatePicker().setMaxDate(System.currentTimeMillis());
        dpd.show();
        dpd.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(themeColor);
        dpd.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(themeColor);
        if (start != null)
            dpd.getButton(DatePickerDialog.BUTTON_NEUTRAL).setTextColor(themeColor);
    }

    // NAVIGATION
    @Override
    public void onClickItem(int pos, String option) {
        statsDialog(data.get(pos));
    }

    @Override
    public void onBackPressed() {
        finish();
        startActivity(new Intent(StatsActivity.this, MainActivity.class));
    }

    // TOOLBAR
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.stats, menu);
        upMenu = menu;
        actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        setUpActionBar();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;

        } else if (item.getItemId() == R.id.action_add) {
            if (exercise)
                startActivity(new Intent(StatsActivity.this, TrainingActivity.class)
                        .putExtra("ex_id", oneID)
                        .putExtra("date", targetDate.getTime()
                        ));
            else
                statsDialog(null);
            return true;

        } else if (item.getItemId() == R.id.action_settings) {
            // Settings dialog
            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.dialog_settings);
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.findViewById(R.id.data_layout).setVisibility(View.GONE);
            dialog.findViewById(R.id.info_layout).setVisibility(View.GONE);
            // Exercise specifics
            EditText textExerciseName = dialog.findViewById(R.id.text_exercise_name);
            EditText textSeconds = dialog.findViewById(R.id.text_seconds);
            EditText textFirstSet = dialog.findViewById(R.id.text_first_set);
            EditText textWeight = dialog.findViewById(R.id.text_weight);
            EditText textReps = dialog.findViewById(R.id.text_reps);
            Button date = dialog.findViewById(R.id.button_date_dialog);
            textExerciseName.setText(obj.name);
            if (exercise) {
                textSeconds.setText(String.valueOf(obj.seconds));
                textFirstSet.setText(String.valueOf(obj.first));
                textReps.setText(String.valueOf(obj.reps));
                textWeight.setText(String.valueOf(obj.weight));
                date.setText(Help.dateFormat(this, targetDate));
                date.setOnClickListener(v -> calendarDialog(targetDate, date, null));
            } else {
                dialog.findViewById(R.id.sec_layout).setVisibility(View.GONE);
                dialog.findViewById(R.id.weight_layout).setVisibility(View.GONE);
                dialog.findViewById(R.id.reps_layout).setVisibility(View.GONE);
                dialog.findViewById(R.id.sets_layout).setVisibility(View.GONE);
                date.setVisibility(View.GONE);
            }
            Help.setButtonsTextColor(themeColor, new Button[] {date});
            for (EditText e: new EditText[] {textExerciseName, textSeconds, textFirstSet, textWeight, textReps}) {
                e.addTextChangedListener(new TextWatcher() {
                    final ColorStateList defColor = e.getTextColors();
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}
                    @Override
                    public void afterTextChanged(Editable s) {
                        if ((e == textExerciseName && !s.toString().equals(obj.name)) || (exercise && (
                                e == textSeconds && !s.toString().equals(String.valueOf(obj.seconds)) ||
                                e == textFirstSet && !s.toString().equals(String.valueOf(obj.first)) ||
                                e == textReps && !s.toString().equals(String.valueOf(obj.reps)) ||
                                e == textWeight && !s.toString().equals(String.valueOf(obj.weight))
                        )))
                            e.setTextColor(themeColor);
                        else
                            e.setTextColor(defColor);
                    }
                });
            }
            // Colors
            final int[] c = {Color.red(themeColor), Color.green(themeColor), Color.blue(themeColor)};
            SeekBar seekR = dialog.findViewById(R.id.seek_color_r);
            SeekBar seekG = dialog.findViewById(R.id.seek_color_g);
            SeekBar seekB = dialog.findViewById(R.id.seek_color_b);
            seekR.setMax(255);
            seekG.setMax(255);
            seekB.setMax(255);
            seekR.setProgress(c[0]);
            seekG.setProgress(c[1]);
            seekB.setProgress(c[2]);
            seekR.setProgressTintList(ColorStateList.valueOf(Color.rgb(c[0], 0, 0)));
            seekG.setProgressTintList(ColorStateList.valueOf(Color.rgb(0, c[1], 0)));
            seekB.setProgressTintList(ColorStateList.valueOf(Color.rgb(0, 0, c[2])));
            seekR.setThumbTintList(ColorStateList.valueOf(Color.rgb(c[0], 0, 0)));
            seekG.setThumbTintList(ColorStateList.valueOf(Color.rgb(0, c[1], 0)));
            seekB.setThumbTintList(ColorStateList.valueOf(Color.rgb(0, 0, c[2])));
            TextView colorView = dialog.findViewById(R.id.color_view);
            colorView.setTextColor(Color.rgb(c[0], c[1], c[2]));
            for (SeekBar bar: new SeekBar[] {seekR, seekG, seekB}) {
                bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        ColorStateList sl;
                        if (seekBar == seekR) {
                            c[0] = progress;
                            sl = ColorStateList.valueOf(Color.rgb(c[0], 0, 0));
                        } else if (seekBar == seekG) {
                            c[1] = progress;
                            sl = ColorStateList.valueOf(Color.rgb(0, c[1], 0));
                        } else {
                            c[2] = progress;
                            sl = ColorStateList.valueOf(Color.rgb(0, 0, c[2]));
                        }
                        colorView.setTextColor(Color.rgb(c[0], c[1], c[2]));
                        seekBar.setProgressTintList(sl);
                        seekBar.setThumbTintList(sl);
                    }
                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {}
                });
            }
            ImageButton defaultColor = dialog.findViewById(R.id.button_default_color_dialog);
            defaultColor.setOnClickListener(v -> {
                db.setObjColor(0, objColorID);
                dialog.cancel();
            });
            // Others
            ImageButton delete = dialog.findViewById(R.id.button_delete_dialog);
            delete.setOnClickListener(v -> {
                // Delete dialog
                Object[] d = Help.editTextDialog(this, themeColor, R.string.delete, "", null, false);
                Dialog dialog1 = (Dialog)d[0];
                ((Button)d[1]).setOnClickListener(v1 -> {
                    if (exercise)
                        db.deleteExercise(oneID);
                    else
                        db.deleteStats(oneID);
                    dialog1.cancel();
                    this.onBackPressed();
                });
                dialog1.show();
            });
            ImageButton back = dialog.findViewById(R.id.button_back_settings_dialog);
            back.setOnClickListener(v -> dialog.cancel());
            Help.setImageButtonsColor(themeColor, new ImageButton[] {back, delete, defaultColor});
            // Save settings
            dialog.setOnCancelListener(d1 -> {
                String name = textExerciseName.getText().toString();
                if (name.length() > 0) {
                    if (exercise) {
                        int s = (int) tryParseIntDouble(textSeconds.getText().toString(), true);
                        int f = (int) tryParseIntDouble(textFirstSet.getText().toString(), true);
                        int r = (int) tryParseIntDouble(textReps.getText().toString(), true);
                        double w = (double) tryParseIntDouble(textWeight.getText().toString(), false);
                        db.updateExerciseSettings(name, oneID, (s==0)?1:s, (f==0)?1:f, (r==0)?1:r, w);
                    } else {
                        db.updateStatsName(name, oneID);
                    }
                }
                int[] oldList = new int[] {Color.red(themeColor), Color.green(themeColor), Color.blue(themeColor)};
                for (int i = 0; i < 3; i++) {
                    if (oldList[i] != c[i]) {
                        db.setObjColor(Color.rgb(c[0], c[1], c[2]), objColorID);
                        break;
                    }
                }
                updateAll(false);
            });
            dialog.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // HELPS
    private void setUpActionBar() {
        actionBar.setTitle(obj.name);
        Help.setActionBackIconColor(this, themeColor, actionBar);
        Help.setActionIconsColor(themeColor, upMenu, new int[] {R.id.action_add, R.id.action_settings});
    }

    private Object tryParseIntDouble(String text, boolean isInteger) {
        if (text.contains("-") || text.contains("+")) {
            if (isInteger) return 1;
            else return 0.0;
        }
        try {
            if (isInteger) return Integer.parseInt(text);
            else return Double.parseDouble(text);
        } catch (Exception e) {
            if (isInteger) return 1;
            else return 0.0;
        }
    }
}