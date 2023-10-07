package com.yurhel.alex.afit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.widget.TextViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
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
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.LabelFormatter;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class StatsActivity extends AppCompatActivity implements ClickInterface {
    DB db;
    boolean isExercise;
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
    FrameLayout graphViewLayout;
    RecyclerView statsView;
    Integer autoScrollPos = null;

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
        isExercise = exerciseID != 0;
        oneID = (isExercise) ? exerciseID : getIntent().getIntExtra("st_id", 0);
        db = new DB(this);
        targetDate = new Date();
        graphViewLayout = findViewById(R.id.graphViewLayout);
        statsView = findViewById(R.id.statsRV);
        // Date buttons
        startDateB = findViewById(R.id.buttonDateStart);
        startDateB.setOnClickListener(v -> calendarDialog(startDate, startDateB, true));
        endDateB = findViewById(R.id.buttonDateEnd);
        endDateB.setOnClickListener(v -> calendarDialog(endDate, endDateB, false));
        // Show/Hide graph button
        graphVisB = findViewById(R.id.buttonGraphVisibility);
        graphVisB.setOnClickListener(v -> {
            LinearLayout l = findViewById(R.id.graphLayout);
            if (l.getVisibility() == View.VISIBLE) {
                l.setVisibility(View.GONE);
                graphVisB.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_arrow_down));
            } else {
                l.setVisibility(View.VISIBLE);
                graphVisB.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_arrow_up));
            }
            graphVisB.setColorFilter(obj.color);
        });
        // Auto scroll
        statsView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (autoScrollPos != null) {
                    RecyclerView.ViewHolder selectedItem = statsView.findViewHolderForAdapterPosition(autoScrollPos);
                    if (selectedItem != null) {
                        autoScrollPos = null;
                        highlightRVItem(selectedItem.itemView);
                    }
                }
            }
        });
        //
        updateAll(true);
    }

    private void updateAll(boolean onCreate) {
        obj = db.getOneMainObj(oneID, isExercise);
        if (!onCreate)
            setUpActionBar();
        graphVisB.setColorFilter(obj.color);
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
        startDateB.setText(Help.dateFormat(this, startDate));
        endDateB.setText(Help.dateFormat(this, endDate));
        Help.setButtonsTextColor(obj.color, new Button[] {startDateB, endDateB});
        // Cut data within date boundaries + get short info
        int oneSetMax = 0;
        double statsMin = 0.0;
        double statsMax = 0.0;
        ArrayList<MyObject> newData = new ArrayList<>();
        ArrayList<DataPoint> xyData = new ArrayList<>();
        for (MyObject item: data) {
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
        ((TextView) findViewById(R.id.allEntriesText)).setText(String.valueOf(data.size()));
        ((TextView) findViewById(R.id.record1)).setText((isExercise)?""+(int)statsMax:""+statsMax);
        ((TextView) findViewById(R.id.record2)).setText((isExercise)?""+oneSetMax:""+statsMin);
        findViewById((isExercise)? R.id.recordMinLabel : R.id.comma).setVisibility(View.GONE);
        int mainColor = Help.getMainColor(this);
        ((ImageView)findViewById(R.id.allEntriesLabel)).setColorFilter(mainColor);
        ((ImageView)findViewById(R.id.recordMaxLabel)).setColorFilter(mainColor);
        ((ImageView)findViewById(R.id.recordMinLabel)).setColorFilter(mainColor);
        if (isExercise && oneSetMax > 0) db.updateExercise(obj.name, oneID, obj.rest, oneSetMax+1, obj.sets, obj.weight, obj.color);
        // Graph setup
        graphViewLayout.removeAllViews();
        GraphView graphView = new GraphView(this);
        graphViewLayout.addView(graphView);
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
                for (MyObject item: data) {
                    if (item.date == (long)dataPoint.getX() && item.mainValue == dataPoint.getY())
                        break;
                    pos++;
                }
                RecyclerView.ViewHolder selectedItem = statsView.findViewHolderForAdapterPosition(pos);
                if (selectedItem != null) {
                    highlightRVItem(selectedItem.itemView);
                } else {
                    statsView.smoothScrollToPosition(pos);
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
        statsView.setLayoutManager(new LinearLayoutManager(this));
        statsView.setHasFixedSize(true);
        statsView.setAdapter(new StatsAdapter(this, data, this));
    }

    // DIALOGS
    private void entryDialog(MyObject passObj, String goal) {
        Dialog dialog = new Dialog(this);

        if (goal.equals("st")) {
            // Stats entry dialog
            dialog.setContentView(R.layout.dialog_add_stats);
            Button pickDate = dialog.findViewById(R.id.pickDateButton);
            EditText addStatsEditText = dialog.findViewById(R.id.editNote);
            addStatsEditText.setVisibility(View.VISIBLE);

            NumberPicker numberPicker0 = dialog.findViewById(R.id.numberPicker0);
            numberPicker0.setMaxValue(99999);
            numberPicker0.setMinValue(1);
            numberPicker0.setWrapSelectorWheel(false);
            numberPicker0.setOnValueChangedListener((picker, oldVal, newVal) -> {
                if (newVal == numberPicker0.getMaxValue())
                    numberPicker0.setMaxValue(newVal+100);
            });

            NumberPicker numberPicker1 = dialog.findViewById(R.id.numberPicker1);
            numberPicker1.setMaxValue(9);
            numberPicker1.setMinValue(0);
            numberPicker1.setWrapSelectorWheel(false);

            if (passObj == null) {
                // Create
                pickDate.setText(Help.dateFormat(this, targetDate));
                pickDate.setOnClickListener(view -> calendarDialog(targetDate, pickDate, null));
                pickDate.setTextColor(obj.color);
                if (data.size() > 0) {
                    numberPicker0.setValue((int) data.get(0).mainValue);
                    numberPicker1.setValue(Integer.parseInt(String.valueOf(data.get(0).mainValue).split("\\.")[1]));
                } else {
                    numberPicker0.setValue(0);
                    numberPicker1.setValue(0);
                }
            } else {
                // Edit
                pickDate.setText(Help.dateFormat(this, new Date(passObj.date)));
                pickDate.setEnabled(false);
                addStatsEditText.setText(passObj.longerValue);
                numberPicker0.setValue((int) passObj.mainValue);
                numberPicker1.setValue(Integer.parseInt(String.valueOf(passObj.mainValue).split("\\.")[1]));
            }

            if (numberPicker0.getValue() == numberPicker0.getMaxValue())
                numberPicker0.setMaxValue(numberPicker0.getValue()+100);

            Button ok = dialog.findViewById(R.id.OkButton);
            ok.setTextColor(obj.color);
            ok.setOnClickListener(view -> {
                Double s = Double.parseDouble(String.format("%1$s.%2$s", numberPicker0.getValue(), numberPicker1.getValue()));
                if (passObj == null)
                    db.addStatsEntry(oneID, s, String.valueOf(targetDate.getTime()), addStatsEditText.getText().toString());
                else
                    db.updateStatsEntry(passObj.id, oneID, s, addStatsEditText.getText().toString());
                dialog.cancel();
                updateAll(false);
            });

        } else if (goal.equals("ex") && passObj != null) {
            // Exercise entry dialog
            dialog.setContentView(R.layout.dialog_show_ex);
            ColorStateList mainColorS = ColorStateList.valueOf(Help.getMainColor(this));

            TextView timeTV = dialog.findViewById(R.id.time);
            timeTV.setText(passObj.time);
            TextViewCompat.setCompoundDrawableTintList(timeTV, mainColorS);

            TextView mainValueTV = dialog.findViewById(R.id.mainValue);
            mainValueTV.setText(String.valueOf((int) passObj.mainValue));
            TextViewCompat.setCompoundDrawableTintList(mainValueTV, mainColorS);

            TextView addValueTV = dialog.findViewById(R.id.addValue);
            addValueTV.setText(passObj.longerValue);
            TextViewCompat.setCompoundDrawableTintList(addValueTV, mainColorS);

            if (!passObj.allWeights.equals("")) {
                TextView addValue2TV = dialog.findViewById(R.id.addValue2);
                addValue2TV.setVisibility(View.VISIBLE);
                addValue2TV.setText(passObj.allWeights);
                TextViewCompat.setCompoundDrawableTintList(addValue2TV, mainColorS);
            }

            Button pickDate = dialog.findViewById(R.id.pickDateButton);
            pickDate.setText(Help.dateFormat(this, new Date(passObj.date)));
            pickDate.setEnabled(false);
        }

        // Universal
        Button cancel = dialog.findViewById(R.id.cancelButton);
        cancel.setTextColor(obj.color);
        cancel.setOnClickListener(view -> dialog.cancel());
        dialog.show();

        if (passObj != null) {
            ImageButton delete = dialog.findViewById(R.id.deleteButton);
            delete.setVisibility(View.VISIBLE);
            delete.setColorFilter(obj.color);
            delete.setOnClickListener(v -> {
                Object[] askDeleteDialog = Help.editTextDialog(this, obj.color,
                        R.string.delete_entry,"", null, false);
                ((Button)askDeleteDialog[1]).setOnClickListener(v1 -> {
                    ((Dialog)askDeleteDialog[0]).cancel();
                    dialog.dismiss();
                    db.deleteSmallObj(oneID, passObj.id, isExercise);
                    updateAll(false);
                });
                ((Dialog)askDeleteDialog[0]).show();
            });
        }
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
                db.setDate(String.valueOf(newDate.getTime()), oneID, isExercise, start);
                updateAll(false);
            } else {
                targetDate = newDate;
            }
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        if (start != null) {
            dpd.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.default_t), (dialog, which) -> {
                db.setDate("", oneID, isExercise, start);
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
        entryDialog(data.get(pos), (isExercise) ? "ex": "st");
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
        if (!isExercise)
            upMenu.findItem(R.id.actionAdd).setTitle(R.string.add_st);
        setUpActionBar();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;

        } else if (item.getItemId() == R.id.actionAdd) {
            if (isExercise)
                startActivity(new Intent(StatsActivity.this, TrainingActivity.class)
                        .putExtra("ex_id", oneID)
                        .putExtra("date", targetDate.getTime()
                        ));
            else
                entryDialog(null, "st");
            return true;

        } else if (item.getItemId() == R.id.actionSettings) {
            // Settings dialog
            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.dialog_settings);
            Window window = dialog.getWindow();
            if (window != null)
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.findViewById(R.id.mainSettingsLayout).setVisibility(View.GONE);
            // Exercise specifics
            EditText textName = dialog.findViewById(R.id.textName);
            EditText textRest = dialog.findViewById(R.id.textRest);
            EditText textWeight = dialog.findViewById(R.id.textWeight);
            EditText textSets = dialog.findViewById(R.id.textSets);
            textName.setText(obj.name);
            if (isExercise) {
                textRest.setText(String.valueOf(obj.rest));
                textSets.setText(String.valueOf(obj.sets));
                textWeight.setText(String.valueOf(obj.weight));
                TextView date = dialog.findViewById(R.id.textDate);
                date.setText(Help.dateFormat(this, targetDate));
                dialog.findViewById(R.id.clickDate).setOnClickListener(v -> calendarDialog(targetDate, date, null));
            } else {
                dialog.findViewById(R.id.dateLayout).setVisibility(View.GONE);
                dialog.findViewById(R.id.restLayout).setVisibility(View.GONE);
                dialog.findViewById(R.id.weightLayout).setVisibility(View.GONE);
                dialog.findViewById(R.id.setsLayout).setVisibility(View.GONE);
            }
            for (EditText e: new EditText[] {textName, textRest, textWeight, textSets}) {
                e.addTextChangedListener(new TextWatcher() {
                    final ColorStateList defColor = e.getTextColors();
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}
                    @Override
                    public void afterTextChanged(Editable s) {
                        if ((e == textName && !s.toString().equals(obj.name)) || (isExercise && (
                                e == textRest && !s.toString().equals(String.valueOf(obj.rest)) ||
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
            SeekBar seekR = dialog.findViewById(R.id.seekColorR);
            SeekBar seekG = dialog.findViewById(R.id.seekColorG);
            SeekBar seekB = dialog.findViewById(R.id.seekColorB);
            seekR.setMax(255);
            seekG.setMax(255);
            seekB.setMax(255);
            TextView colorView = dialog.findViewById(R.id.textColor);
            setSeekBarsColor(seekR, seekG, seekB, colorView, c);
            // Set def color
            ImageButton defaultColor = dialog.findViewById(R.id.defaultColorButton);
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
            ImageButton delete = dialog.findViewById(R.id.deleteButton);
            delete.setOnClickListener(v -> {
                Object[] askDeleteDialog = Help.editTextDialog(this, obj.color,
                        R.string.delete, "", null, false);
                ((Button)askDeleteDialog[1]).setOnClickListener(v1 -> {
                    ((Dialog)askDeleteDialog[0]).cancel();
                    dialog.dismiss();
                    this.onBackPressed();
                    db.deleteObj(oneID, isExercise);
                });
                ((Dialog)askDeleteDialog[0]).show();
            });
            ImageButton back = dialog.findViewById(R.id.backButton);
            back.setOnClickListener(v -> dialog.cancel());
            Help.setImageButtonsColor(obj.color, new ImageButton[] {back, delete, defaultColor});
            // Save settings
            dialog.setOnCancelListener(d1 -> {
                String name = textName.getText().toString();
                if (name.length() > 0) {
                    if (isExercise) {
                        int s = (int) tryParseIntDouble(textRest.getText().toString(), true);
                        int st = (int) tryParseIntDouble(textSets.getText().toString(), true);
                        double w = (double) tryParseIntDouble(textWeight.getText().toString(), false);
                        db.updateExercise(name, oneID, (s==0)?1:s, obj.reps, (st==0)?1:st, w, Color.rgb(c[0], c[1], c[2]));
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
        Help.setActionIconsColor(obj.color, upMenu, new int[] {R.id.actionAdd, R.id.actionSettings});
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