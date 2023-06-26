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
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("");
        }
        int exerciseID = getIntent().getIntExtra("ex_id", 0);
        exercise = exerciseID != 0;
        oneID = (exercise) ? exerciseID : getIntent().getIntExtra("st_id", 0);
        db = new DB(this);
        targetDate = new Date();
        graphView = findViewById(R.id.graph_view);
        nightMode = Help.isNightMode(this);
        // Date buttons
        startDateB = findViewById(R.id.button_date_start);
        startDateB.setOnClickListener(v -> calendarDialog(startDate, startDateB, true));
        endDateB = findViewById(R.id.button_date_end);
        endDateB.setOnClickListener(v -> calendarDialog(endDate, endDateB, false));
        // Show/Hide graph button
        graphVisB = findViewById(R.id.button_graph_visibility);
        graphVisB.setOnClickListener(v -> {
            LinearLayout l = findViewById(R.id.graph_layout);
            if (l.getVisibility() == View.VISIBLE) {
                l.setVisibility(View.GONE);
                graphVisB.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_arrow_down));
            } else {
                l.setVisibility(View.VISIBLE);
                graphVisB.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_arrow_up));
            }
            graphVisB.setColorFilter(obj.color);
        });
        //
        updateAll(true);
    }

    private void updateAll(boolean onCreate) {
        data = db.getTableEntries(oneID, exercise);
        obj = db.getOneMainObj(oneID, exercise);
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
        Help.setButtonsTextColor(obj.color, new Button[] {startDateB, endDateB});
        graphVisB.setColorFilter(obj.color);
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
        int mainColor = Help.getMainColor(this);
        ((ImageView)findViewById(R.id.all_entries_label)).setColorFilter(mainColor);
        ((ImageView)findViewById(R.id.result_max_label)).setColorFilter(mainColor);
        ((ImageView)findViewById(R.id.result_min_label)).setColorFilter(mainColor);
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
                series.setColor(obj.color);
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
        EditText addStatsEditText = dialog.findViewById(R.id.add_stats_edit_text);
        addStatsEditText.setVisibility(View.VISIBLE);
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
            Help.setButtonsTextColor(obj.color, new Button[] {pickDate, ok, cancel});
        } else {
            numberPicker0.setValue((int) passObj.value);
            numberPicker1.setValue(Integer.parseInt(String.valueOf(passObj.value).split("\\.")[1]));
            pickDate.setText(Help.dateFormat(this, new Date(passObj.date)));
            pickDate.setEnabled(false);
            addStatsEditText.setText(passObj.notes);
            Help.setButtonsTextColor(obj.color, new Button[] {ok, cancel});
        }
        if (numberPicker0.getValue() == numberPicker0.getMaxValue())
            numberPicker0.setMaxValue(numberPicker0.getValue()+100);
        ok.setOnClickListener(view -> {
            Double s = Double.parseDouble(String.format("%1$s.%2$s", numberPicker0.getValue(), numberPicker1.getValue()));
            if (passObj == null)
                db.addStatsEntry(oneID, s, String.valueOf(targetDate.getTime()), addStatsEditText.getText().toString());
            else
                db.updateStatsEntry(passObj.id, oneID, s, addStatsEditText.getText().toString());
            dialog.cancel();
            updateAll(false);
        });
        cancel.setOnClickListener(view -> dialog.cancel());
        dialog.show();
    }

    private void calendarDialog(Date oldDate, Object dateButton, Boolean start) {
        Calendar c = Calendar.getInstance();
        if (oldDate != null)
            c.setTime(oldDate);
        DatePickerDialog dpd = new DatePickerDialog(this, R.style.DateDialog, (view1, year1, monthOfYear, dayOfMonth) -> {
            c.set(Calendar.YEAR, year1);
            c.set(Calendar.MONTH, monthOfYear);
            c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            Date newDate = c.getTime();
            ((TextView)dateButton).setText(Help.dateFormat(this, newDate));
            if (start != null) {
                db.setDate(String.valueOf(newDate.getTime()), oneID, exercise, start);
                updateAll(false);
            } else {
                targetDate = newDate;
            }
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        if (start != null) {
            dpd.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.default_t), (dialog, which) -> {
                db.setDate("", oneID, exercise, start);
                updateAll(false);
            });
        }
        dpd.getDatePicker().setMaxDate(System.currentTimeMillis());
        dpd.show();
        dpd.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(obj.color);
        dpd.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(obj.color);
        if (start != null)
            dpd.getButton(DatePickerDialog.BUTTON_NEUTRAL).setTextColor(obj.color);
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
        if (!exercise)
            upMenu.findItem(R.id.action_add).setTitle(R.string.add_st);
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
            // Exercise specifics
            EditText textName = dialog.findViewById(R.id.text_name);
            EditText textRest = dialog.findViewById(R.id.text_rest);
            EditText textReps = dialog.findViewById(R.id.text_reps);
            EditText textWeight = dialog.findViewById(R.id.text_weight);
            EditText textSets = dialog.findViewById(R.id.text_sets);
            textName.setText(obj.name);
            if (exercise) {
                textRest.setText(String.valueOf(obj.rest));
                textReps.setText(String.valueOf(obj.reps));
                textSets.setText(String.valueOf(obj.sets));
                textWeight.setText(String.valueOf(obj.weight));
                TextView date = dialog.findViewById(R.id.tv_date_dialog);
                date.setText(Help.dateFormat(this, targetDate));
                dialog.findViewById(R.id.click_date).setOnClickListener(v -> calendarDialog(targetDate, date, null));
            } else {
                dialog.findViewById(R.id.date_layout).setVisibility(View.GONE);
                dialog.findViewById(R.id.sec_layout).setVisibility(View.GONE);
                dialog.findViewById(R.id.weight_layout).setVisibility(View.GONE);
                dialog.findViewById(R.id.reps_layout).setVisibility(View.GONE);
                dialog.findViewById(R.id.sets_layout).setVisibility(View.GONE);
            }
            for (EditText e: new EditText[] {textName, textRest, textReps, textWeight, textSets}) {
                e.addTextChangedListener(new TextWatcher() {
                    final ColorStateList defColor = e.getTextColors();
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}
                    @Override
                    public void afterTextChanged(Editable s) {
                        if ((e == textName && !s.toString().equals(obj.name)) || (exercise && (
                                e == textRest && !s.toString().equals(String.valueOf(obj.rest)) ||
                                e == textReps && !s.toString().equals(String.valueOf(obj.reps)) ||
                                e == textSets && !s.toString().equals(String.valueOf(obj.sets)) ||
                                e == textWeight && !s.toString().equals(String.valueOf(obj.weight))
                        )))
                            e.setTextColor(obj.color);
                        else
                            e.setTextColor(defColor);
                    }
                });
            }
            // Colors
            final int[] c = {Color.red(obj.color), Color.green(obj.color), Color.blue(obj.color)};
            SeekBar seekR = dialog.findViewById(R.id.seek_color_r);
            SeekBar seekG = dialog.findViewById(R.id.seek_color_g);
            SeekBar seekB = dialog.findViewById(R.id.seek_color_b);
            seekR.setMax(255);
            seekG.setMax(255);
            seekB.setMax(255);
            TextView colorView = dialog.findViewById(R.id.color_view);
            setSeekBarsColor(seekR, seekG, seekB, colorView, c);
            // Set def color
            ImageButton defaultColor = dialog.findViewById(R.id.button_default_color_dialog);
            defaultColor.setOnClickListener(v -> {
                c[0] = Color.red(obj.color);
                c[1] = Color.green(obj.color);
                c[2] = Color.blue(obj.color);
                setSeekBarsColor(seekR, seekG, seekB, colorView, c);
            });
            // On color change
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
                        int fullColor = Color.rgb(c[0], c[1], c[2]);
                        colorView.setTextColor(fullColor);
                        defaultColor.setColorFilter(fullColor);
                        seekBar.setProgressTintList(sl);
                        seekBar.setThumbTintList(sl);
                    }
                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {}
                });
            }
            // Others
            ImageButton delete = dialog.findViewById(R.id.button_delete_dialog);
            delete.setOnClickListener(v -> {
                // Delete dialog
                Object[] d = Help.editTextDialog(this, obj.color, R.string.delete, "", null, false);
                ((Button)d[1]).setOnClickListener(v1 -> {
                    ((Dialog)d[0]).cancel();
                    dialog.dismiss();
                    this.onBackPressed();
                    db.deleteObj(oneID, exercise);
                });
                ((Dialog)d[0]).show();
            });
            ImageButton back = dialog.findViewById(R.id.button_back_settings_dialog);
            back.setOnClickListener(v -> dialog.cancel());
            Help.setImageButtonsColor(obj.color, new ImageButton[] {back, delete, defaultColor});
            // Save settings
            dialog.setOnCancelListener(d1 -> {
                String name = textName.getText().toString();
                if (name.length() > 0) {
                    if (exercise) {
                        int s = (int) tryParseIntDouble(textRest.getText().toString(), true);
                        int f = (int) tryParseIntDouble(textReps.getText().toString(), true);
                        int r = (int) tryParseIntDouble(textSets.getText().toString(), true);
                        double w = (double) tryParseIntDouble(textWeight.getText().toString(), false);
                        db.updateExercise(name, oneID, (s==0)?1:s, (f==0)?1:f, (r==0)?1:r, w, Color.rgb(c[0], c[1], c[2]));
                    } else {
                        db.updateStats(name, oneID, Color.rgb(c[0], c[1], c[2]));
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
        Help.setActionBackIconColor(this, obj.color, actionBar);
        Help.setActionIconsColor(obj.color, upMenu, new int[] {R.id.action_add, R.id.action_settings});
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

    private void setSeekBarsColor(SeekBar seekR, SeekBar seekG, SeekBar seekB, TextView colorView, int[] c) {
        seekR.setProgress(c[0]);
        seekG.setProgress(c[1]);
        seekB.setProgress(c[2]);
        seekR.setProgressTintList(ColorStateList.valueOf(Color.rgb(c[0], 0, 0)));
        seekG.setProgressTintList(ColorStateList.valueOf(Color.rgb(0, c[1], 0)));
        seekB.setProgressTintList(ColorStateList.valueOf(Color.rgb(0, 0, c[2])));
        seekR.setThumbTintList(ColorStateList.valueOf(Color.rgb(c[0], 0, 0)));
        seekG.setThumbTintList(ColorStateList.valueOf(Color.rgb(0, c[1], 0)));
        seekB.setThumbTintList(ColorStateList.valueOf(Color.rgb(0, 0, c[2])));
        colorView.setTextColor(Color.rgb(c[0], c[1], c[2]));
    }
}