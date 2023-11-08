package com.yurhel.alex.afit;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

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
    TextView infoButton;
    Button buttonSets;
    Button buttonTime;
    Button editWeight;
    ProgressBar progressResult;
    ProgressBar progressResultW;
    ProgressBar progressTime;
    DB db;
    int seconds;
    boolean stop = false;
    int exerciseID;
    String doInfo;
    String restInfo;
    LocalTime startTime = LocalTime.now();
    Thread thread;
    MyObject obj;
    MediaPlayer sound;
    LinearLayout repsResultLayout;
    LinearLayout repsResultLayoutW;
    int progressOneSetPX;
    int progressHeight;
    int colorWhite;
    Intent notificationIntent;
    PowerManager.WakeLock wakeLock;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training);

        db = new DB(TrainingActivity.this);
        exerciseID = getIntent().getIntExtra("ex_id", 0);
        obj = db.getOneMainObj(exerciseID, true);

        repsResultLayout = findViewById(R.id.repsResultLayout);
        infoButton = findViewById(R.id.infoOnButton);
        infoButton.setShadowLayer(14,1,1,getColor(R.color.dark));
        progressHeight = getResources().getDimensionPixelSize(R.dimen.height_progress_result);
        colorWhite = getColor(R.color.white);
        doInfo = getText(R.string.do_exercise)+" "+obj.name;
        restInfo = getString(R.string.rest);

        requestPermissions(new String[] { "android.permission.POST_NOTIFICATIONS" }, 1);
        notificationIntent = new Intent(this, NotificationService.class);
        updateNotification(doInfo);

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AFit::WakelockKeepTag");
        wakeLock.acquire(3600000L /*1 hour*/);

        sound = MediaPlayer.create(this, Settings.System.DEFAULT_NOTIFICATION_URI);

        actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(doInfo);
        Help.setActionBackIconColor(this, obj.color, actionBar);

        progressTime = findViewById(R.id.progressTime);
        progressTime.setMax(obj.rest);
        progressTime.setProgress(0);
        progressResult = findViewById(R.id.progressResult);
        progressResult.setMax(obj.sets);
        progressResult.setProgress(0);
        progressResult.post(() -> progressOneSetPX = progressResult.getWidth() / obj.sets);
        setProgressColor(obj.color, new ProgressBar[] {progressTime, progressResult});

        // Up row with weight
        if (obj.weight > 0) {
            findViewById(R.id.upLayout).setVisibility(View.VISIBLE);
            repsResultLayoutW = findViewById(R.id.repsResultLayoutW);
            repsResultLayoutW.setVisibility(View.VISIBLE);

            progressResultW = findViewById(R.id.progressResultW);
            progressResultW.setMax(obj.sets);
            progressResultW.setProgress(0);
            progressResultW.setVisibility(View.VISIBLE);
            setProgressColor(obj.color, new ProgressBar[] {progressResultW});

            editWeight = findViewById(R.id.textWeightTraining);
            editWeight.setBackgroundColor(obj.color);
            editWeight.setText(String.valueOf(obj.weight));
            editWeight.setOnClickListener(v -> {
                // Edit weight dialog
                Object[] d = Help.editTextDialog(this, obj.color, null, editWeight.getText().toString(), true, false);
                ((Button)d[1]).setOnClickListener(v1 -> {
                    String t = ((EditText)d[2]).getText().toString();
                    if (!t.equals("")) {
                        editWeight.setText(t);
                        ((Dialog)d[0]).cancel();
                    }
                });
                ((Dialog)d[0]).show();
            });

            ImageButton buttonPlusW = findViewById(R.id.buttonPlusW);
            buttonPlusW.setOnClickListener(view -> {
                double i = Double.parseDouble(editWeight.getText().toString());
                editWeight.setText(String.valueOf(i + 1));
            });
            ImageButton buttonMinusW = findViewById(R.id.buttonMinusW);
            buttonMinusW.setOnClickListener(view -> {
                double i = Double.parseDouble(editWeight.getText().toString());
                if (i > 0)
                    editWeight.setText((i < 1)? Double.toString(0.0): new BigDecimal(i - 1).setScale(1, RoundingMode.HALF_UP).toString());
            });
            Help.setImageButtonsColor(obj.color, new ImageButton[] {buttonMinusW, buttonPlusW});
        }

        ImageButton buttonMinus = findViewById(R.id.buttonMinus);
        buttonMinus.setOnClickListener(view -> {
            if (thread == null) {
                int i = Integer.parseInt(buttonSets.getText().toString());
                if (i > 0)
                    buttonSets.setText(String.valueOf(i - 1));
            } else {
                if (seconds - 30 <= 0) {
                    seconds = 0;
                    progressTime.setProgress(progressTime.getMax());
                } else {
                    seconds -= 30;
                    progressTime.setMax(progressTime.getMax() - 30);
                }
            }
        });
        ImageButton buttonPlus = findViewById(R.id.buttonPlus);
        buttonPlus.setOnClickListener(view -> {
            if (thread == null) {
                int i = Integer.parseInt(buttonSets.getText().toString());
                buttonSets.setText(String.valueOf(i + 1));
            } else {
                seconds += 30;
                progressTime.setMax(progressTime.getMax() + 30);
            }
        });
        Help.setImageButtonsColor(obj.color, new ImageButton[] {buttonMinus, buttonPlus});

        buttonTime = findViewById(R.id.buttonTime);
        buttonTime.setTextColor(obj.color);
        buttonTime.setOnTouchListener(this::checkTouchCorners);
        buttonTime.setOnClickListener(view -> stopThread(false));

        buttonSets = findViewById(R.id.buttonSets);
        Drawable dr = AppCompatResources.getDrawable(this, R.drawable.circle);
        if (dr != null)
            dr.setTint(obj.color);
        buttonSets.setBackground(dr);
        buttonSets.setShadowLayer(14,1,1,getColor(R.color.dark));
        buttonSets.setText(String.valueOf(obj.reps));
        buttonSets.setOnTouchListener(this::checkTouchCorners);
        buttonSets.setOnClickListener(view -> {
            textToProgress(repsResults, buttonSets.getText().toString(), repsResultLayout);
            if (obj.weight > 0)
                textToProgress(repsWeights, editWeight.getText().toString(), repsResultLayoutW);
            if (repsResults.size() >= obj.sets) {
                exit(true);
            } else {
                buttonSets.setVisibility(View.GONE);
                buttonTime.setVisibility(View.VISIBLE);
                seconds = progressTime.getMax();
                progressResult.incrementProgressBy(1);
                if (obj.weight > 0)
                    progressResultW.incrementProgressBy(1);
                actionBar.setTitle(restInfo);
                infoButton.setText(R.string.stop);
                infoButton.setTextColor(obj.color);
                thread = new Thread(() -> {
                    while (seconds > 0 && !stop) {
                        runOnUiThread(() -> {
                            updateNotification(restInfo +" "+seconds);
                            buttonTime.setText(String.valueOf(seconds));
                            progressTime.incrementProgressBy(1);
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
        if (obj.color == colorWhite)
            buttonSets.setTextColor(getColor(R.color.dark));
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
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            progressTime.setProgress(0);
            buttonSets.setVisibility(View.VISIBLE);
            buttonTime.setVisibility(View.GONE);
            actionBar.setTitle(doInfo);
            infoButton.setText(R.string.ok);
            infoButton.setTextColor(colorWhite);
            thread = null;
            stop = false;
        }
        if (closeActivity) {
            if (wakeLock.isHeld())
                wakeLock.release();
            stopService(notificationIntent);
            progressResult.setProgress(0);
            sound.release();
            if (obj.weight > 0)
                progressResultW.setProgress(0);
        } else {
            updateNotification(doInfo);
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
                trainingTime = min+":"+((sec < 10)? "0"+sec: ""+sec);
            } else {
                trainingTime = "0:"+((timeSec < 10)? "0"+timeSec: ""+timeSec);
            }
            String date = String.valueOf(getIntent().getLongExtra("date", 0));
            db.addExerciseEntry(exerciseID, resultShort, resultFull, trainingTime, date, resultWeights);
        }
        startActivity(new Intent(TrainingActivity.this, StatsActivity.class).putExtra("ex_id", exerciseID));
        finish();
    }

    @Override
    protected void onDestroy() {
        stopThread(true);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (repsResults.size() > 0) {
            // Save/not dialog
            Object[] d = Help.editTextDialog(this, obj.color, R.string.save_results, "", null, true);
            ((Button)d[1]).setOnClickListener(v1 -> {
                ((Dialog)d[0]).cancel();
                exit(true);
            });
            ((Button)d[2]).setText(R.string.no);
            ((Button)d[2]).setOnClickListener(v1 -> {
                ((Dialog)d[0]).cancel();
                exit(false);
            });
            ((Dialog)d[0]).show();
        } else {
            exit(false);
        }
    }

    // TOOLBAR
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.training, menu);
        Help.setActionIconsColor(obj.color, menu, new int[] {R.id.actionSaveTraining});
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) exit(false);
        else if (item.getItemId() == R.id.actionSaveTraining && repsResults.size() > 0) exit(true);
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
        if (text.equals("")) text = "0.0";
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
        for (ProgressBar progress: progressBars)
            progress.setProgressTintList(c);
    }
}