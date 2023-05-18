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
import android.provider.Settings;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

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
    TextView editWeight;
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
    int themeColor;
    MyObject obj;
    MediaPlayer sound;
    LinearLayout repsResultLayout;
    LinearLayout repsResultLayoutW;
    int progressOneSetPX;
    int progressHeight;
    int colorWhite;
    Intent notificationIntent;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training);

        db = new DB(TrainingActivity.this);
        exerciseID = getIntent().getIntExtra("ex_id", 0);
        obj = db.getOneInfo(exerciseID, true);

        themeColor = Help.getMainGreyColor(this);
        int[] objColor = db.getObjColor(exerciseID +"_ex");
        themeColor = (objColor[0] != 0) ? objColor[0]: themeColor;

        repsResultLayout = findViewById(R.id.tr_reps_result_layout);
        infoButton = findViewById(R.id.tr_info_button);
        progressHeight = getResources().getDimensionPixelSize(R.dimen.height_progress_result);
        colorWhite = getColor(R.color.white);
        doInfo = getText(R.string.do_exercise)+" "+obj.name;
        restInfo = getString(R.string.rest);

        requestPermissions(new String[] { "android.permission.POST_NOTIFICATIONS" }, 1);
        notificationIntent = new Intent(this, NotificationService.class);
        updateNotification(doInfo);

        actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(doInfo);
        Help.setActionBackIconColor(this, themeColor, actionBar);

        progressTime = findViewById(R.id.tr_progress_time);
        progressTime.setMax(obj.seconds);
        progressTime.setProgress(0);
        progressResult = findViewById(R.id.tr_progress_result);
        progressResult.setMax(obj.reps);
        progressResult.setProgress(0);
        progressResult.post(() -> progressOneSetPX = progressResult.getWidth() / obj.reps);
        setProgressColor(themeColor, new ProgressBar[] {progressTime, progressResult});

        // Up row with weight
        if (obj.weight > 0) {
            findViewById(R.id.tr_up_layout).setVisibility(View.VISIBLE);
            repsResultLayoutW = findViewById(R.id.tr_reps_result_layout_w);

            progressResultW = findViewById(R.id.tr_progress_result_w);
            progressResultW.setMax(obj.reps);
            progressResultW.setProgress(0);
            progressResultW.setVisibility(View.VISIBLE);
            setProgressColor(themeColor, new ProgressBar[] {progressResultW});

            editWeight = findViewById(R.id.tr_text_weight);
            editWeight.setText(String.valueOf(obj.weight));
            editWeight.setOnClickListener(v -> {
                // Edit weight dialog
                Object[] d = Help.editTextDialog(this, themeColor, null, editWeight.getText().toString(), true, false);
                Dialog dialog1 = (Dialog)d[0];
                ((Button)d[1]).setOnClickListener(v1 -> {
                    String t = ((EditText)d[2]).getText().toString();
                    if (!t.equals("")) {
                        editWeight.setText(t);
                        dialog1.cancel();
                    }
                });
                dialog1.show();
            });

            ImageButton buttonPlusW = findViewById(R.id.tr_button_plus_w);
            buttonPlusW.setOnClickListener(view -> {
                double i = Double.parseDouble(editWeight.getText().toString());
                editWeight.setText(String.valueOf(i + 1));
            });
            ImageButton buttonMinusW = findViewById(R.id.tr_button_minus_w);
            buttonMinusW.setOnClickListener(view -> {
                double i = Double.parseDouble(editWeight.getText().toString());
                if (i > 0)
                    editWeight.setText(String.valueOf(i - 1));
            });
            Help.setImageButtonsColor(themeColor, new ImageButton[] {buttonMinusW, buttonPlusW});
        }

        ImageButton buttonMinus = findViewById(R.id.tr_button_minus);
        buttonMinus.setOnClickListener(view -> {
            if (thread == null) {
                int i = Integer.parseInt(buttonSets.getText().toString());
                if (i > 0) {
                    buttonSets.setText(String.valueOf(i - 1));
                }
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
        ImageButton buttonPlus = findViewById(R.id.tr_button_plus);
        buttonPlus.setOnClickListener(view -> {
            if (thread == null) {
                int i = Integer.parseInt(buttonSets.getText().toString());
                buttonSets.setText(String.valueOf(i + 1));
            } else {
                seconds += 30;
                progressTime.setMax(progressTime.getMax() + 30);
            }
        });
        Help.setImageButtonsColor(themeColor, new ImageButton[] {buttonMinus, buttonPlus});

        buttonTime = findViewById(R.id.tr_button_time);
        buttonTime.setTextColor(themeColor);
        buttonTime.setOnTouchListener(this::checkTouchCorners);
        buttonTime.setOnClickListener(view -> stopThread(false));

        buttonSets = findViewById(R.id.tr_button_sets);
        Drawable dr = AppCompatResources.getDrawable(this, R.drawable.circle);
        assert dr != null;
        dr.setTint(themeColor);
        buttonSets.setBackground(dr);
        buttonSets.setText(String.valueOf(obj.first));
        buttonSets.setOnTouchListener(this::checkTouchCorners);
        buttonSets.setOnClickListener(view -> {
            textToProgress(repsResults, buttonSets.getText().toString(), repsResultLayout);
            if (obj.weight > 0)
                textToProgress(repsWeights, editWeight.getText().toString(), repsResultLayoutW);
            if (repsResults.size() >= obj.reps) {
                exitSaveResults();
            } else {
                buttonSets.setVisibility(View.GONE);
                buttonTime.setVisibility(View.VISIBLE);
                seconds = progressTime.getMax();
                progressResult.incrementProgressBy(1);
                if (obj.weight > 0)
                    progressResultW.incrementProgressBy(1);
                sound = MediaPlayer.create(this, Settings.System.DEFAULT_NOTIFICATION_URI);
                actionBar.setTitle(restInfo);
                infoButton.setText(R.string.stop);
                infoButton.setTextColor(themeColor);
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
        if (themeColor == colorWhite)
            buttonSets.setTextColor(getColor(R.color.black));
    }

    public void updateNotification(String msg) {
        notificationIntent.putExtra("msg", msg);
        startService(notificationIntent);
    }

    public void stopThread(boolean closeActivity) {
        if (thread != null) {
            sound.release();
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
            stopService(notificationIntent);
            progressResult.setProgress(0);
            if (obj.weight > 0)
                progressResultW.setProgress(0);
        } else {
            updateNotification(doInfo);
        }
    }

    // NAV
    public void exitSaveResults() {
        stopThread(true);
        int resultShort = 0;
        for (String i: repsResults)
            resultShort += Integer.parseInt(i);
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
        db.addExStats(exerciseID, resultShort, resultFull, trainingTime, date, resultWeights);
        finish();
        startActivity(new Intent(TrainingActivity.this, StatsActivity.class).putExtra("ex_id", exerciseID));
    }

    public void exitWithoutSave() {
        stopThread(true);
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
            Object[] d = Help.editTextDialog(this, themeColor, R.string.save_results, "", null, true);
            Dialog dialog1 = (Dialog)d[0];
            ((Button)d[1]).setOnClickListener(v1 -> {
                dialog1.cancel();
                exitSaveResults();
            });
            Button no = (Button)d[2];
            no.setText(R.string.no);
            no.setOnClickListener(v1 -> {
                dialog1.cancel();
                exitWithoutSave();
            });
            dialog1.show();
        } else {
            exitWithoutSave();
            super.onBackPressed();
        }
    }

    // TOOLBAR
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mi = getMenuInflater();
        mi.inflate(R.menu.training, menu);
        Help.setActionIconsColor(themeColor, menu, new int[] {R.id.action_save_training});
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            exitWithoutSave();
            return true;

        } else if (item.getItemId() == R.id.action_save_training) {
            if (repsResults.size() > 0)
                exitSaveResults();
            return true;
        }
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
        if (text.equals(""))
            text = "0.0";

        results.add(text);
        TextView tv = new TextView(this);
        tv.setText(text);
        if (themeColor == colorWhite)
            tv.setTextColor(getColor(R.color.black));
        else
            tv.setTextColor(colorWhite);
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