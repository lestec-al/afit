package com.yurhel.alex.afit.training;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import com.yurhel.alex.afit.R;
import com.yurhel.alex.afit.core.DB;
import com.yurhel.alex.afit.core.Help;
import com.yurhel.alex.afit.core.Obj;
import com.yurhel.alex.afit.databinding.ActivityTrainingBinding;
import com.yurhel.alex.afit.stats.StatsActivity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class TrainingActivity extends AppCompatActivity {
    androidx.appcompat.app.ActionBar actionBar;
    ArrayList<String> repsResults = new ArrayList<>();
    ArrayList<String> repsWeights = new ArrayList<>();
    DB db;
    int seconds;
    boolean stop = false;
    int exerciseID;
    String doInfo;
    String restInfo;
    LocalTime startTime = LocalTime.now();
    Thread thread;
    Obj obj;
    MediaPlayer sound;
    int progressOneSetPX;
    int progressHeight;
    int colorWhite;
    Intent notificationIntent;
    PowerManager.WakeLock wakeLock;
    Boolean withWeight;
    ActivityTrainingBinding views;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        views = ActivityTrainingBinding.inflate(getLayoutInflater());
        setContentView(views.getRoot());

        // On back pressed
        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (!repsResults.isEmpty()) {
                    // Save/not dialog
                    Object[] d = Help.editDialog(
                            TrainingActivity.this,
                            obj.color,
                            R.string.save_workout,
                            "",
                            null,
                            true
                    );
                    ((ImageButton)d[1]).setOnClickListener(v1 -> {
                        ((Dialog)d[0]).cancel();
                        exit(true);
                    });
                    ((ImageButton)d[2]).setOnClickListener(v1 -> {
                        ((Dialog)d[0]).cancel();
                        exit(false);
                    });
                    ((Dialog)d[0]).show();
                } else {
                    exit(false);
                }
            }
        });

        db = new DB(TrainingActivity.this);
        exerciseID = getIntent().getIntExtra("ex_id", 0);
        obj = db.getOneMainObj(exerciseID, true);

        views.infoOnButton.setShadowLayer(1,1,1,getColor(R.color.dark));
        progressHeight = getResources().getDimensionPixelSize(R.dimen.height_progress_result);
        colorWhite = getColor(R.color.white);
        doInfo = getText(R.string.do_exercise).toString();
        restInfo = getString(R.string.rest);

        requestPermissions(new String[] { "android.permission.POST_NOTIFICATIONS" }, 1);
        notificationIntent = new Intent(this, Notifications.class);
        updateNotification(doInfo + " " + obj.reps + " " + obj.name);

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AFit::WakelockKeepTag");
        wakeLock.acquire(3600000L /*1 hour*/);

        sound = MediaPlayer.create(this, Settings.System.DEFAULT_NOTIFICATION_URI);

        actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(doInfo + " " + obj.reps + " " + obj.name);
        Help.setActionBackIconColor(this, obj.color, actionBar);

        views.progressTime.setMax(obj.rest);
        views.progressTime.setProgress(0);

        views.progressResult.setMax(obj.sets);
        views.progressResult.setProgress(0);
        views.progressResult.post(() -> progressOneSetPX = views.progressResult.getWidth() / obj.sets);
        setProgressColor(obj.color, new ProgressBar[] {views.progressTime, views.progressResult});

        // Up row with weight
        withWeight = getIntent().getBooleanExtra("withWeight", false);
        if (withWeight) {
            views.upLayout.setVisibility(View.VISIBLE);
            views.repsResultLayoutW.setVisibility(View.VISIBLE);

            views.progressResultW.setMax(obj.sets);
            views.progressResultW.setProgress(0);
            views.progressResultW.setVisibility(View.VISIBLE);
            setProgressColor(obj.color, new ProgressBar[] {views.progressResultW});

            views.textWeightTraining.setBackgroundColor(obj.color);
            views.textWeightTraining.setText(String.valueOf(obj.weight));
            views.textWeightTraining.setOnClickListener(v -> {
                // Edit weight dialog
                Object[] d = Help.editDialog(
                        this,
                        obj.color,
                        null,
                        views.textWeightTraining.getText().toString(),
                        true,
                        false
                );
                ((ImageButton)d[1]).setOnClickListener(v1 -> {
                    String t = ((EditText)d[2]).getText().toString();
                    if (!t.isEmpty()) {
                        views.textWeightTraining.setText(t);
                        ((Dialog)d[0]).cancel();
                    }
                });
                ((Dialog)d[0]).show();
            });

            views.buttonPlusW.setOnClickListener(view -> {
                double i = Double.parseDouble(views.textWeightTraining.getText().toString());
                views.textWeightTraining.setText(String.valueOf(i + 1));
            });
            views.buttonMinusW.setOnClickListener(view -> {
                double i = Double.parseDouble(views.textWeightTraining.getText().toString());
                if (i > 0)
                    views.textWeightTraining.setText((i < 1)? Double.toString(0.0): new BigDecimal(i - 1).setScale(1, RoundingMode.HALF_UP).toString());
            });
            Help.setImageButtonsColor(obj.color, new ImageButton[] {views.buttonMinusW, views.buttonPlusW});
        }

        views.buttonMinus.setOnClickListener(view -> {
            if (thread == null) {
                int i = Integer.parseInt(views.buttonSets.getText().toString());
                if (i > 0) views.buttonSets.setText(String.valueOf(i - 1));
            } else {
                if (seconds - 30 <= 0) {
                    seconds = 0;
                    views.progressTime.setProgress(views.progressTime.getMax());
                } else {
                    seconds -= 30;
                    views.progressTime.setMax(views.progressTime.getMax() - 30);
                }
            }
        });
        views.buttonPlus.setOnClickListener(view -> {
            if (thread == null) {
                int i = Integer.parseInt(views.buttonSets.getText().toString());
                views.buttonSets.setText(String.valueOf(i + 1));
            } else {
                seconds += 30;
                views.progressTime.setMax(views.progressTime.getMax() + 30);
            }
        });
        Help.setImageButtonsColor(obj.color, new ImageButton[] {views.buttonMinus, views.buttonPlus});

        views.buttonTime.setTextColor(obj.color);
        views.buttonTime.setOnTouchListener(this::checkTouchCorners);
        views.buttonTime.setOnClickListener(view -> stopThread(false));

        Drawable dr = AppCompatResources.getDrawable(this, R.drawable.circle);
        if (dr != null) dr.setTint(obj.color);
        views.buttonSets.setBackground(dr);
        views.buttonSets.setShadowLayer(14,1,1,getColor(R.color.dark));
        views.buttonSets.setText(String.valueOf(obj.reps));
        views.buttonSets.setOnTouchListener(this::checkTouchCorners);
        views.buttonSets.setOnClickListener(view -> {
            textToProgress(repsResults, views.buttonSets.getText().toString(), views.repsResultLayout);
            if (withWeight)
                textToProgress(repsWeights, views.textWeightTraining.getText().toString(), views.repsResultLayoutW);
            if (repsResults.size() >= obj.sets) {
                exit(true);
            } else {
                views.buttonSets.setVisibility(View.GONE);
                views.buttonTime.setVisibility(View.VISIBLE);
                seconds = views.progressTime.getMax();
                views.progressResult.incrementProgressBy(1);
                if (withWeight) views.progressResultW.incrementProgressBy(1);
                actionBar.setTitle(restInfo);
                views.infoOnButton.setText(R.string.stop);
                views.infoOnButton.setTextColor(obj.color);
                thread = new Thread(() -> {
                    while (seconds > 0 && !stop) {
                        runOnUiThread(() -> {
                            updateNotification(restInfo +": "+seconds);
                            views.buttonTime.setText(String.valueOf(seconds));
                            views.progressTime.incrementProgressBy(1);
                            if (seconds == 1)
                                sound.start();
                        });
                        try {
                            TimeUnit.SECONDS.sleep(1);
                        } catch (InterruptedException ignored) {}
                        seconds -= 1;
                    }
                    if (!stop)
                        runOnUiThread(() -> stopThread(false));
                });
                thread.start();
            }
        });
        if (obj.color == colorWhite) views.buttonSets.setTextColor(getColor(R.color.dark));
    }

    public void updateNotification(String msg) {
        notificationIntent.putExtra("msg", msg);
        startService(notificationIntent);
    }

    public void stopThread(boolean closeActivity) {
        if (thread != null) {
            stop = true;
            try {
                thread.join();
            } catch (InterruptedException ignored) {}
            views.progressTime.setProgress(0);
            views.buttonSets.setVisibility(View.VISIBLE);
            views.buttonTime.setVisibility(View.GONE);
            actionBar.setTitle(doInfo + " " + views.buttonSets.getText().toString() + " " + obj.name);
            views.infoOnButton.setText(R.string.done);
            views.infoOnButton.setTextColor(colorWhite);
            thread = null;
            stop = false;
        }
        if (closeActivity) {
            if (wakeLock.isHeld()) wakeLock.release();
            stopService(notificationIntent);
            views.progressResult.setProgress(0);
            sound.release();
            if (withWeight) views.progressResultW.setProgress(0);
        } else {
            updateNotification(doInfo + " " + views.buttonSets.getText().toString() + " " + obj.name);
        }
    }

    // NAV
    public void exit(Boolean isSaveResults) {
        stopThread(true);
        if (isSaveResults) {
            int resultShort = 0;
            for (String i: repsResults) {
                resultShort += Integer.parseInt(i);
            }
            String resultFull = String.join(" + ", repsResults);
            String resultWeights = String.join(" + ", repsWeights);
            String trainingTime;
            int timeSec = (int) Duration.between(startTime, LocalTime.now()).getSeconds();
            if (timeSec >= 60) {
                int min = timeSec / 60;
                int sec = timeSec % 60;
                trainingTime = min+":"+((sec < 10)? "0"+sec: String.valueOf(sec));
            } else {
                trainingTime = "0:"+((timeSec < 10)? "0"+timeSec: String.valueOf(timeSec));
            }
            String date = String.valueOf(getIntent().getLongExtra("date", 0));
            db.addExerciseEntry(exerciseID, resultShort, resultFull, trainingTime, date, resultWeights);
        }
        startActivity(new Intent(TrainingActivity.this, StatsActivity.class)
                .putExtra("ex_id", exerciseID)
                .putExtra("workoutIsOver", isSaveResults)
        );
        finish();
    }

    @Override
    protected void onDestroy() {
        stopThread(true);
        super.onDestroy();
    }

    // TOOLBAR
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save, menu);
        Help.setActionIconsColor(obj.color, menu, new int[] {R.id.actionSave});
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) exit(false);
        else if (item.getItemId() == R.id.actionSave && !repsResults.isEmpty()) exit(true);
        return super.onOptionsItemSelected(item);
    }

    // HELPS
    public boolean checkTouchCorners(View view, MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            int half = view.getWidth() / 2;
            double ed = Math.sqrt(Math.pow(event.getX()-half, 2) + Math.pow(event.getY()-half, 2));
            return ed > half;
        }
        return false;
    }

    public void textToProgress(ArrayList<String> results, String text, LinearLayout layout) {
        if (text.isEmpty()) text = "0.0";
        results.add(text);
        TextView tv = new TextView(this);
        tv.setText(text);
        if (obj.color == colorWhite) {
            tv.setTextColor(getColor(R.color.dark));
        } else {
            tv.setTextColor(colorWhite);
            tv.setShadowLayer(14,1,1,getColor(R.color.dark));
        }
        tv.setHeight(progressHeight);
        tv.setWidth(progressOneSetPX);
        tv.setGravity(Gravity.CENTER);
        layout.addView(tv);
    }

    private void setProgressColor(int color, ProgressBar[] progressBars) {
        ColorStateList c = ColorStateList.valueOf(color);
        for (ProgressBar progress: progressBars) {
            progress.setProgressTintList(c);
        }
    }
}