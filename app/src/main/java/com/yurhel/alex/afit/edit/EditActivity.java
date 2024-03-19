package com.yurhel.alex.afit.edit;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.ColorUtils;
import androidx.core.widget.TextViewCompat;

import com.yurhel.alex.afit.MainActivity;
import com.yurhel.alex.afit.R;
import com.yurhel.alex.afit.core.DB;
import com.yurhel.alex.afit.core.Help;
import com.yurhel.alex.afit.core.Obj;
import com.yurhel.alex.afit.databinding.ActivityEditBinding;
import com.yurhel.alex.afit.stats.StatsActivity;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class EditActivity extends AppCompatActivity {
    DB db;
    int mainColor;
    boolean isCardCreation;
    boolean isExercise;
    boolean withWeight;
    int oneID;
    int[] c;
    Date targetDate;
    Obj obj;
    ActivityEditBinding views;
    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        views = ActivityEditBinding.inflate(getLayoutInflater());
        setContentView(views.getRoot());

        // On back pressed
        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isCardCreation) {
                    startActivity(new Intent(EditActivity.this, MainActivity.class));
                } else {
                    // Update card when move back
                    String name = views.textName.getText().toString();
                    if (!name.isBlank()) {
                        if (isExercise) {
                            // Ex
                            int s = (int) tryParseIntDouble(views.textRest.getText().toString(), true);
                            int st = (int) tryParseIntDouble(views.textSets.getText().toString(), true);
                            double w = (double) tryParseIntDouble(views.textWeight.getText().toString(), false);
                            db.updateExercise(name, oneID, (s==0)?1:s, obj.reps, (st==0)?1:st, w, Color.rgb(c[0], c[1], c[2]));
                        } else {
                            // St
                            db.updateStats(name, oneID, Color.rgb(c[0], c[1], c[2]));
                        }
                    }
                    startActivity(new Intent(EditActivity.this, StatsActivity.class)
                            .putExtra((isExercise) ? "ex_id" : "st_id", oneID)
                            .putExtra("target_date", targetDate.getTime())
                    );
                }
                finish();
            }
        });

        int exerciseID = getIntent().getIntExtra("ex_id", 0);
        isExercise = exerciseID != 0;
        oneID = (isExercise) ? exerciseID : getIntent().getIntExtra("st_id", 0);
        long dataFromIntent = getIntent().getLongExtra("target_date", 0);
        targetDate = (dataFromIntent != 0) ? new Date(dataFromIntent) : new Date();
        isCardCreation = dataFromIntent == 0;
        db = new DB(this);
        if (!isCardCreation) obj = db.getOneMainObj(oneID, isExercise);
        mainColor = getColor(R.color.on_background);
        int greenColor = getColor(R.color.green_main);
        int redColor = getColor(R.color.red);

        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle((isCardCreation) ? R.string.add_card : R.string.card_settings);
            actionBar.setDisplayHomeAsUpEnabled(true);
            Help.setActionBackIconColor(this, (isCardCreation) ? mainColor : obj.color, actionBar);
        }

        // VIEWS
        if (isCardCreation) {
            // Hide bottom part
            views.additionalEdits.setVisibility(View.GONE);

            // Card type chooser
            views.exerciseStatsSwitch.setTrackTintList(ColorStateList.valueOf(redColor).withAlpha(120));
            views.exerciseStatsSwitch.setThumbTintList(ColorStateList.valueOf(redColor));
            views.exerciseStatsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    views.exerciseStatsSwitch.setTrackTintList(ColorStateList.valueOf(greenColor).withAlpha(120));
                    views.exerciseStatsSwitch.setThumbTintList(ColorStateList.valueOf(greenColor));

                    views.restLayout.setVisibility(View.GONE);
                    views.weightLayout.setVisibility(View.GONE);
                    views.setsLayout.setVisibility(View.GONE);
                } else {
                    views.exerciseStatsSwitch.setTrackTintList(ColorStateList.valueOf(redColor).withAlpha(120));
                    views.exerciseStatsSwitch.setThumbTintList(ColorStateList.valueOf(redColor));

                    views.restLayout.setVisibility(View.VISIBLE);
                    views.weightLayout.setVisibility(View.VISIBLE);
                    views.setsLayout.setVisibility(View.VISIBLE);
                }
            });
            views.exCardText.setOnClickListener(v -> views.exerciseStatsSwitch.setChecked(false));
            TextView stCardText = views.stCardText;
            stCardText.setOnClickListener(v -> views.exerciseStatsSwitch.setChecked(true));

            c = new int[]{Color.red(greenColor), Color.green(greenColor), Color.blue(greenColor)};

        } else {
            // Hide card type chooser
            views.cardTypeChooser.setVisibility(View.GONE);

            c = new int[]{Color.red(obj.color), Color.green(obj.color), Color.blue(obj.color)};

            // Delete button
            views.deleteButton.setOnClickListener(v -> {
                Object[] askDelDialog = Help.editDialog(
                        this,
                        obj.color,
                        R.string.delete_card_info,
                        "",
                        null,
                        false
                );
                ((ImageButton)askDelDialog[1]).setOnClickListener(v1 -> {
                    ((Dialog)askDelDialog[0]).cancel();
                    db.deleteObj(oneID, isExercise);
                    // After deletion move to main activity
                    startActivity(new Intent(EditActivity.this, MainActivity.class));
                    finish();
                });
                ((Dialog)askDelDialog[0]).show();
            });

            // Weight switch
            withWeight = db.getIsWeightShowForMainStat(oneID + "_" + ((isExercise) ? "ex" : "st"));
            views.showWeight.setChecked(withWeight);
            if (isExercise) {
                views.showWeight.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    withWeight = isChecked;
                    views.showWeight.setChecked(isChecked);
                    db.setWeights(oneID + "_" + ((isExercise) ? "ex" : "st"), isChecked);
                    setSeekBarsColor(c);
                });
            } else {
                views.showWeight.setVisibility(View.GONE);
            }

            views.textName.setText(obj.name);
            if (isExercise) {
                views.textRest.setText(String.valueOf(obj.rest));
                views.textSets.setText(String.valueOf(obj.sets));
                views.textWeight.setText(String.valueOf(obj.weight));

                // Date picker button
                views.clickDate.setText(Help.dateFormat(this, targetDate));
                views.clickDate.setOnClickListener(v -> {
                    // Calendar dialog
                    Calendar c = Calendar.getInstance();
                    if (targetDate != null) c.setTime(targetDate);
                    DatePickerDialog dpd = new DatePickerDialog(
                            this,
                            R.style.DateDialog,
                            (view1, year1, monthOfYear, dayOfMonth) -> {
                                c.set(Calendar.YEAR, year1);
                                c.set(Calendar.MONTH, monthOfYear);
                                c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                Date newDate = c.getTime();
                                views.clickDate.setText(Help.dateFormat(this, newDate));
                                targetDate = newDate;
                            },
                            c.get(Calendar.YEAR),
                            c.get(Calendar.MONTH),
                            c.get(Calendar.DAY_OF_MONTH)
                    );

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
                });

            } else {
                views.clickDate.setVisibility(View.GONE);
                views.restLayout.setVisibility(View.GONE);
                views.weightLayout.setVisibility(View.GONE);
                views.setsLayout.setVisibility(View.GONE);
            }

            String nameStr = (isCardCreation) ? views.textName.getText().toString() : obj.name;
            String restStr = (isCardCreation) ? views.textRest.getText().toString() : String.valueOf(obj.rest);
            String setsStr = (isCardCreation) ? views.textSets.getText().toString() : String.valueOf(obj.sets);
            String weightStr = (isCardCreation) ? views.textWeight.getText().toString() : String.valueOf(obj.weight);

            // Text listeners
            for (EditText e: new EditText[] {views.textName, views.textRest, views.textWeight, views.textSets}) {
                if (e.getVisibility() == View.VISIBLE) {
                    e.addTextChangedListener(new TextWatcher() {
                        final ColorStateList defColor = e.getTextColors();
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {}
                        @Override
                        public void afterTextChanged(Editable s) {
                            if ((e == views.textName && !s.toString().equals(nameStr)) || (isExercise && (
                                    e == views.textRest && !s.toString().equals(restStr) ||
                                            e == views.textSets && !s.toString().equals(setsStr) ||
                                            e == views.textWeight && !s.toString().equals(weightStr)
                            ))) e.setTextColor(redColor);
                            else e.setTextColor(defColor);
                        }
                    });
                }
            }
        }

        // COLORS
        views.seekColorR.setMax(255);
        views.seekColorG.setMax(255);
        views.seekColorB.setMax(255);
        setSeekBarsColor(c);

        // Random color
        views.randomColor.setOnClickListener(v -> {
            Random r = new Random();
            c[0] = Color.red(Color.rgb(r.nextInt(256), 0, 0));
            c[1] = Color.green(Color.rgb(0, r.nextInt(256), 0));
            c[2] = Color.blue(Color.rgb(0, 0, r.nextInt(256)));
            setSeekBarsColor(c);
        });

        // Def color
        views.defaultColorButton.setOnClickListener(v -> {
            c[0] = Color.red((isCardCreation) ? greenColor : obj.color);
            c[1] = Color.green((isCardCreation) ? greenColor : obj.color);
            c[2] = Color.blue((isCardCreation) ? greenColor : obj.color);
            setSeekBarsColor(c);
        });

        // On color change
        for (SeekBar bar: new SeekBar[] {views.seekColorR, views.seekColorG, views.seekColorB}) {
            bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    ColorStateList sl;
                    if (seekBar == views.seekColorR) {
                        c[0] = progress;
                        sl = ColorStateList.valueOf(Color.rgb(c[0], 0, 0));
                    } else if (seekBar == views.seekColorG) {
                        c[1] = progress;
                        sl = ColorStateList.valueOf(Color.rgb(0, c[1], 0));
                    } else {
                        c[2] = progress;
                        sl = ColorStateList.valueOf(Color.rgb(0, 0, c[2]));
                    }
                    seekBar.setProgressTintList(sl);
                    seekBar.setThumbTintList(sl);
                    setSeekBarsColor(c);
                }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isCardCreation) {
            getMenuInflater().inflate(R.menu.save, menu);
            Help.setActionIconsColor(mainColor, menu, new int[] {R.id.actionSave});
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;

        } else if (item.getItemId() == R.id.actionSave) {
            // Create card
            String name = views.textName.getText().toString();
            if (!name.isBlank()) {
                if (views.exerciseStatsSwitch.isChecked()) {
                    // St
                    db.addStats(name, Color.rgb(c[0], c[1], c[2]));
                } else {
                    // Ex
                    int rest = (int) tryParseIntDouble(views.textRest.getText().toString(), true);
                    int sets = (int) tryParseIntDouble(views.textSets.getText().toString(), true);
                    double weight = (double) tryParseIntDouble(views.textWeight.getText().toString(), false);
                    db.addExercise(name, Math.max(rest, 1), Math.max(sets, 1), weight, Color.rgb(c[0], c[1], c[2]));
                }
                startActivity(new Intent(EditActivity.this, MainActivity.class));
                finish();
            } else {
                // Show error
                views.nameLayout.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.red)));
                views.nameErrorText.setVisibility(View.VISIBLE);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setSeekBarsColor(int[] c) {
        // Get color from bars
        views.seekColorR.setProgress(c[0]);
        views.seekColorG.setProgress(c[1]);
        views.seekColorB.setProgress(c[2]);
        views.seekColorR.setProgressTintList(ColorStateList.valueOf(Color.rgb(c[0], 0, 0)));
        views.seekColorG.setProgressTintList(ColorStateList.valueOf(Color.rgb(0, c[1], 0)));
        views.seekColorB.setProgressTintList(ColorStateList.valueOf(Color.rgb(0, 0, c[2])));
        views.seekColorR.setThumbTintList(ColorStateList.valueOf(Color.rgb(c[0], 0, 0)));
        views.seekColorG.setThumbTintList(ColorStateList.valueOf(Color.rgb(0, c[1], 0)));
        views.seekColorB.setThumbTintList(ColorStateList.valueOf(Color.rgb(0, 0, c[2])));
        int color = Color.rgb(c[0], c[1], c[2]);

        // Set color
        Help.setImageButtonsColor(color, new ImageButton[] {views.defaultColorButton, views.randomColor});
        Help.setButtonsTextColor(color, new Button[] {views.deleteButton, views.clickDate});
        TextViewCompat.setCompoundDrawableTintList(views.deleteButton, ColorStateList.valueOf(color));
        TextViewCompat.setCompoundDrawableTintList(views.clickDate, ColorStateList.valueOf(color));

        if (isExercise) {
            views.showWeight.setTextColor(color);
            if (views.showWeight.isChecked()) {
                ColorStateList itemColor = ColorStateList.valueOf(color);
                views.showWeight.setThumbTintList(itemColor);
                views.showWeight.setTrackTintList(itemColor.withAlpha(120));
            } else {
                int greyColor = getColor(R.color.grey);
                views.showWeight.setThumbTintList(ColorStateList.valueOf(ColorUtils.blendARGB(greyColor, Color.WHITE, 0.8f)));
                views.showWeight.setTrackTintList(ColorStateList.valueOf(ColorUtils.blendARGB(greyColor, Color.WHITE, 0.4f)));
            }
        }
        if (!isCardCreation) Help.setActionBackIconColor(this, color, actionBar);
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