package com.yurhel.alex.afit.calendar;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import com.yurhel.alex.afit.MainActivity;
import com.yurhel.alex.afit.R;
import com.yurhel.alex.afit.core.DB;
import com.yurhel.alex.afit.core.Help;
import com.yurhel.alex.afit.core.Obj;
import com.yurhel.alex.afit.databinding.ActivityCalendarBinding;
import com.yurhel.alex.afit.databinding.DialogCalendarStatsBinding;
import com.yurhel.alex.afit.databinding.DialogStatsBinding;
import com.yurhel.alex.afit.databinding.RowStatsBinding;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;

public class CalendarActivity extends AppCompatActivity implements CalendarClick {
    int themeColor;
    Calendar calendar;
    Calendar today;
    ArrayList<Obj> data;
    int stepWidth;
    int stepHeight;
    int calendarViewWidth;
    androidx.appcompat.app.ActionBar actionBar;
    boolean isNight;
    int whiteColor, blackColor;
    ActivityCalendarBinding views;
    boolean isDialogOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        views = ActivityCalendarBinding.inflate(getLayoutInflater());
        setContentView(views.getRoot());

        // On back pressed
        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                startActivity(new Intent(CalendarActivity.this, MainActivity.class));
                finish();
            }
        });

        DB db = new DB(this);
        // Get & sort data
        data = db.getAll();
        LinkedHashMap<String, Integer> positions = db.getPositions();
        db.close();
        try {
            data.sort(
                    Comparator.comparing(obj1 -> positions.get(obj1.parentId + "_" + ((obj1.time != null/*isExercise*/) ? "ex" : "st")))
            );
        } catch (Exception ignored) {}

        themeColor = getColor(R.color.on_background);
        actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setTitle("");
        isNight = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        whiteColor = getColor(R.color.white);
        blackColor = getColor(R.color.dark);

        // Calendar
        today = Calendar.getInstance();
        calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        // Create upper header (days of week)
        views.calendarHeaderDays.post(() -> {
            Calendar c = Calendar.getInstance();
            int d = c.getFirstDayOfWeek();
            c.set(Calendar.DAY_OF_WEEK, d);
            stepHeight = (views.calendarRV.getHeight()) / 6;
            calendarViewWidth = views.calendarRV.getWidth();
            stepWidth = views.calendarHeaderDays.getWidth() / 7;
            for (int i = 1; i < 8; i += 1) {
                TextView tv = new TextView(this);
                tv.setWidth(stepWidth);
                tv.setGravity(Gravity.CENTER);
                tv.setText(DateFormat.format("EEE", c));
                d = (d+1 <= 7) ? d+1 : 1;
                c.set(Calendar.DAY_OF_WEEK, d);
                views.calendarHeaderDays.addView(tv);
            }

            updateCalendar();
        });

        // Scroll calendar settings
        views.calendarRV.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                try {
                    float pos = recyclerView.computeHorizontalScrollOffset() / (float) calendarViewWidth;
                    if (pos == 0) {
                        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH)-1);
                        updateCalendar();
                    } else if (pos == 2) {
                        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH)+1);
                        updateCalendar();
                    } else if (pos != 1) {
                        // Position in between - scroll to item (1 it's a middle position)
                        RecyclerView.SmoothScroller scroller = new LinearSmoothScroller(CalendarActivity.this) {
                            @Override protected int calculateTimeForScrolling(int dx) { return dx/3; }
                        };
                        if (pos < 1) scroller.setTargetPosition(0); // To left
                        else scroller.setTargetPosition(2); // To right

                        RecyclerView.LayoutManager lm = recyclerView.getLayoutManager();
                        if (lm != null) lm.startSmoothScroll(scroller);
                    }
                } catch (Exception ignored) {}
            }
        });

        // Bottom navigation
        Help.setupBottomNavigation(CalendarActivity.this, R.id.actionCalendar, views.navigation, this::finish);
    }

    public void updateCalendar() {
        String d = DateFormat.format(
                (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)) ? "LLLL" : "LLLL yyyy",
                calendar
        ).toString();
        actionBar.setTitle(d.substring(0, 1).toUpperCase() + d.substring(1));

        views.calendarRV.setAdapter(new CalendarAdapterBig(
                calendar,
                CalendarActivity.this,
                data,
                themeColor,
                stepWidth,
                stepHeight,
                isNight,
                whiteColor,
                blackColor,
                this
        ));
        views.calendarRV.setItemViewCacheSize(2);
        views.calendarRV.setHasFixedSize(true);
        views.calendarRV.scrollToPosition(1);
    }

    // TOOLBAR
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.calendar, menu);
        menu.findItem(R.id.actionDateNow).setTitle(Help.dateFormat(this, new Date()));
        Help.setActionIconsColor(themeColor, menu, new int[] {R.id.actionDateNow, R.id.actionSetDate});
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();

        } else if (item.getItemId() == R.id.actionDateNow) {
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            updateCalendar();
            return true;

        } else if (item.getItemId() == R.id.actionSetDate) {
            // Custom date picker dialog
            Dialog dialog = new Dialog(this);
            DialogStatsBinding dViews = DialogStatsBinding.inflate(getLayoutInflater());
            dialog.setContentView(dViews.getRoot());
            dViews.pickDateButton.setVisibility(View.GONE);

            Window window = dialog.getWindow();
            if (window != null) window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            // Get all months
            Calendar c = Calendar.getInstance();
            c.set(Calendar.DAY_OF_MONTH, 1);
            int max = c.getActualMaximum(Calendar.MONTH);
            String[] months = new String[max+1];
            for (int i = 0; i <= max; i += 1) {
                c.set(Calendar.MONTH, i);
                months[i] = String.valueOf(DateFormat.format("LLL", c));
            }

            // Month picker
            dViews.numberPicker0.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
            dViews.numberPicker0.setMaxValue(max);
            dViews.numberPicker0.setMinValue(0);
            dViews.numberPicker0.setDisplayedValues(months);
            dViews.numberPicker0.setWrapSelectorWheel(false);
            dViews.numberPicker0.setValue(calendar.get(Calendar.MONTH));

            // Year picker
            dViews.numberPicker1.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
            dViews.numberPicker1.setMaxValue(calendar.get(Calendar.YEAR)+10);
            dViews.numberPicker1.setMinValue(calendar.get(Calendar.YEAR)-10);
            dViews.numberPicker1.setWrapSelectorWheel(false);
            dViews.numberPicker1.setValue(calendar.get(Calendar.YEAR));
            dViews.numberPicker1.setOnValueChangedListener((picker, oldVal, newVal) -> {
                if (newVal == dViews.numberPicker1.getMaxValue()) dViews.numberPicker1.setMaxValue(newVal+5);
                else if (newVal == dViews.numberPicker1.getMinValue()) dViews.numberPicker1.setMinValue(newVal-5);
            });

            dViews.okButton.setColorFilter(themeColor);
            dViews.okButton.setOnClickListener(view -> {
                calendar.set(Calendar.MONTH, dViews.numberPicker0.getValue());
                calendar.set(Calendar.YEAR, dViews.numberPicker1.getValue());
                dialog.cancel();
                updateCalendar();
            });

            dViews.cancelButton.setColorFilter(themeColor);
            dViews.cancelButton.setOnClickListener(view -> dialog.cancel());

            dialog.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(Date cForLabel, ArrayList<Obj> data) {
        if (isDialogOpen) return;

        LayoutInflater inflater = getLayoutInflater();

        // Calendar stats dialog
        Dialog dialog = new Dialog(this);
        DialogCalendarStatsBinding dView = DialogCalendarStatsBinding.inflate(inflater);
        dialog.setContentView(dView.getRoot());
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        dView.label.setText(Help.dateFormat(this, cForLabel));

        // Calc training time
        int allTimeMin = 0;
        int allTimeSec = 0;

        int colorShadow = (isNight) ? whiteColor : blackColor;
        // Set all stats for day to dialog
        int itemsCount = 0;
        RowStatsBinding vs = null;
        for (Obj i: data) {
            itemsCount++;
            vs = RowStatsBinding.inflate(inflater, dView.itemsLayout, true);

            vs.mainValue.setTextColor(i.color);
            vs.mainValue.setShadowLayer(1,1,1, colorShadow);
            vs.addValue.setTextColor(i.color);
            vs.addValue.setShadowLayer(1,1,1, colorShadow);

            if (i.time != null/*Is exercise*/) {
                // Get training times
                String[] t = i.time.split(":");
                allTimeMin += Integer.parseInt(t[0]);
                allTimeSec += Integer.parseInt(t[1]);

                if (!i.allWeights.isEmpty()) {
                    vs.addValue2.setVisibility(View.VISIBLE);
                    vs.addValue2.setText(i.allWeights);
                    vs.addValue2.setTextColor(i.color);
                    vs.addValue2.setShadowLayer(1,1,1, colorShadow);
                } else {
                    vs.addValue2.setVisibility(View.GONE);
                }
                vs.time.setText(i.time);
                vs.time.setTextColor(i.color);
                vs.time.setShadowLayer(1,1,1, colorShadow);

                vs.addValue.setText(i.longerValue);
                vs.mainValue.setText(String.valueOf((int) i.mainValue));
            } else {
                vs.addValue.setText(i.longerValue);
                vs.mainValue.setText(String.valueOf(i.mainValue));
            }

            vs.rightValue.setText(i.name);
            vs.rightValue.setTextColor(i.color);
            vs.rightValue.setShadowLayer(1,1,1, colorShadow);
        }

        // Remove last item divider
        if (itemsCount >= 0 && vs != null) vs.getRoot().setBackground(null);

        // Empty text
        dView.emptyText.setVisibility((itemsCount == 0) ? View.VISIBLE : View.GONE);

        // Finish calc training time
        TextView allTime = dView.allTime;
        if (allTimeMin > 0 || allTimeSec > 0) {
            if (allTimeSec > 59) {
                allTimeMin += allTimeSec / 60;
                if (allTimeSec % 60 > 30) allTimeMin += 1;
            } else {
                if (allTimeSec > 30) allTimeMin += 1;
            }
            String allTimeText = allTimeMin + " " + getText(R.string.training_min);
            allTime.setText(allTimeText);
        } else {
            allTime.setVisibility(View.GONE);
        }

        // Make sure only one dialog opens ?
        dialog.setOnCancelListener(dialog1 -> isDialogOpen = false);
        isDialogOpen = true;

        dialog.show();
    }
}