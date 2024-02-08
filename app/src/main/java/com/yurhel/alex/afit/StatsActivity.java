package com.yurhel.alex.afit;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Random;
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
        ((TextView) findViewById(R.id.record1)).setText((isExercise)? String.valueOf((int) statsMax) : String.valueOf(statsMax));
        ((TextView) findViewById(R.id.record2)).setText((isExercise)? String.valueOf(oneSetMax) : String.valueOf(statsMin));
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
        // Empty text
        if (data.size() == 0) findViewById(R.id.emptyTV).setVisibility(View.VISIBLE);
        else findViewById(R.id.emptyTV).setVisibility(View.GONE);
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

            Button cancel = dialog.findViewById(R.id.cancelButton);
            cancel.setTextColor(obj.color);
            cancel.setOnClickListener(view -> dialog.cancel());

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
            ((Button)dateButton).setText(Help.dateFormat(this, newDate));
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
            // Settings dialog
            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.dialog_settings_stats);
            Window window = dialog.getWindow();
            if (window != null) window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            EditText textRest = dialog.findViewById(R.id.textRest);
            EditText textWeight = dialog.findViewById(R.id.textWeight);
            EditText textSets = dialog.findViewById(R.id.textSets);

            EditText textName = dialog.findViewById(R.id.textName);
            textName.setText(obj.name);

            Button date = dialog.findViewById(R.id.clickDate);
            if (isExercise) {
                textRest.setText(String.valueOf(obj.rest));
                textSets.setText(String.valueOf(obj.sets));
                textWeight.setText(String.valueOf(obj.weight));

                date.setText(Help.dateFormat(this, targetDate));
                date.setOnClickListener(v -> calendarDialog(targetDate, date, null));
            } else {
                date.setVisibility(View.GONE);
                dialog.findViewById(R.id.restLayout).setVisibility(View.GONE);
                dialog.findViewById(R.id.weightLayout).setVisibility(View.GONE);
                dialog.findViewById(R.id.setsLayout).setVisibility(View.GONE);
                textRest.setVisibility(View.GONE);
                textWeight.setVisibility(View.GONE);
                textSets.setVisibility(View.GONE);
            }

            // Text listeners
            for (EditText e: new EditText[] {textName, textRest, textWeight, textSets}) {
                if (e.getVisibility() == View.VISIBLE) {
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
                            ))) e.setTextColor(obj.color);
                            else e.setTextColor(defColor);
                        }
                    });
                }
            }

            // Init for seek bars changes color ???
            ImageButton randomColor = dialog.findViewById(R.id.randomColor);
            ImageButton defaultColor = dialog.findViewById(R.id.defaultColorButton);

            Button delete = dialog.findViewById(R.id.deleteButton);
            delete.setOnClickListener(v -> {
                Object[] askDeleteDialog = Help.editTextDialog(
                        this,
                        obj.color,
                        R.string.delete,
                        "",
                        null,
                        false
                );
                ((Button)askDeleteDialog[1]).setOnClickListener(v1 -> {
                    ((Dialog)askDeleteDialog[0]).cancel();
                    dialog.dismiss();
                    db.deleteObj(oneID, isExercise);
                    getOnBackPressedDispatcher().onBackPressed();
                });
                ((Dialog)askDeleteDialog[0]).show();
            });

            // Colors
            final int[] c = {Color.red(obj.color), Color.green(obj.color), Color.blue(obj.color)};
            SeekBar seekR = dialog.findViewById(R.id.seekColorR);
            SeekBar seekG = dialog.findViewById(R.id.seekColorG);
            SeekBar seekB = dialog.findViewById(R.id.seekColorB);
            seekR.setMax(255);
            seekG.setMax(255);
            seekB.setMax(255);
            setSeekBarsColor(seekR, seekG, seekB, c, randomColor, defaultColor, delete, date);

            // Random color
            randomColor.setOnClickListener(v -> {
                Random r = new Random();
                c[0] = Color.red(Color.rgb(r.nextInt(256), 0, 0));
                c[1] = Color.green(Color.rgb(0, r.nextInt(256), 0));
                c[2] = Color.blue(Color.rgb(0, 0, r.nextInt(256)));
                setSeekBarsColor(seekR, seekG, seekB, c, randomColor, defaultColor, delete, date);
            });

            // Def color
            defaultColor.setOnClickListener(v -> {
                c[0] = Color.red(obj.color);
                c[1] = Color.green(obj.color);
                c[2] = Color.blue(obj.color);
                setSeekBarsColor(seekR, seekG, seekB, c, randomColor, defaultColor, delete, date);
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
                        seekBar.setProgressTintList(sl);
                        seekBar.setThumbTintList(sl);
                        setSeekBarsColor(seekR, seekG, seekB, c, randomColor, defaultColor, delete, date);
                    }
                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {}
                });
            }

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
    private void clickOnAddActionButton() {
        if (isExercise) {
            startActivity(new Intent(StatsActivity.this, TrainingActivity.class)
                    .putExtra("ex_id", oneID)
                    .putExtra("date", targetDate.getTime())
            );
            finish();
        } else {
            entryDialog(null, "st");
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

    private void setSeekBarsColor(
            SeekBar seekR,
            SeekBar seekG,
            SeekBar seekB,
            int[] c,
            ImageButton randomColor,
            ImageButton defaultColor,
            Button delete,
            Button date
    ) {
        // Get color from bars
        seekR.setProgress(c[0]);
        seekG.setProgress(c[1]);
        seekB.setProgress(c[2]);
        seekR.setProgressTintList(ColorStateList.valueOf(Color.rgb(c[0], 0, 0)));
        seekG.setProgressTintList(ColorStateList.valueOf(Color.rgb(0, c[1], 0)));
        seekB.setProgressTintList(ColorStateList.valueOf(Color.rgb(0, 0, c[2])));
        seekR.setThumbTintList(ColorStateList.valueOf(Color.rgb(c[0], 0, 0)));
        seekG.setThumbTintList(ColorStateList.valueOf(Color.rgb(0, c[1], 0)));
        seekB.setThumbTintList(ColorStateList.valueOf(Color.rgb(0, 0, c[2])));
        int color = Color.rgb(c[0], c[1], c[2]);

        // Set color
        Help.setImageButtonsColor(color, new ImageButton[] {defaultColor, randomColor});
        Help.setButtonsTextColor(color, new Button[] {delete, date});
        TextViewCompat.setCompoundDrawableTintList(delete, ColorStateList.valueOf(color));
        TextViewCompat.setCompoundDrawableTintList(date, ColorStateList.valueOf(color));
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