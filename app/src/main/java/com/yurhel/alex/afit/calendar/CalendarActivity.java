package com.yurhel.alex.afit.calendar;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import com.yurhel.alex.afit.databinding.DialogStatsBinding;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Objects;

public class CalendarActivity extends AppCompatActivity {
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
            int pos = 1;
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                try {
                    int offset = recyclerView.computeHorizontalScrollOffset();
                    if (offset % calendarViewWidth == 0) {
                        // Item in position - update calendar
                        pos = offset / calendarViewWidth;
                        if (pos == 0) {
                            calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH)-1);
                            updateCalendar();
                        } else if (pos == 2) {
                            calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH)+1);
                            updateCalendar();
                        }
                    } else {
                        // Position in between - scroll to item
                        RecyclerView.SmoothScroller smoothScroller = getSmoothScroller(offset, pos);
                        Objects.requireNonNull(views.calendarRV.getLayoutManager()).startSmoothScroll(smoothScroller);
                    }
                } catch (Exception ignored) {}
            }
        });

        // Bottom navigation
        Help.setupBottomNavigation(CalendarActivity.this, R.id.actionCalendar, views.navigation, this::finish);
    }

    @NonNull
    private RecyclerView.SmoothScroller getSmoothScroller(int offset, int pos) {
        RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(CalendarActivity.this) {
            @Override
            protected int calculateTimeForScrolling(int dx) {
                return dx/3;
            }
        };
        int scrollNumber = offset - calendarViewWidth;
        if (scrollNumber < 0) {
            if (scrollNumber <= -stepWidth *2) smoothScroller.setTargetPosition(pos-1);
            else smoothScroller.setTargetPosition(pos);
        } else {
            if (scrollNumber >= stepWidth *2) smoothScroller.setTargetPosition(pos+1);
            else smoothScroller.setTargetPosition(pos);
        }
        return smoothScroller;
    }

    public void updateCalendar() {
        String d;
        if (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)) d = String.valueOf(DateFormat.format("LLLL", calendar));
        else d = String.valueOf(DateFormat.format("LLLL yyyy", calendar));
        actionBar.setTitle(d.substring(0, 1).toUpperCase() + d.substring(1));
        views.calendarRV.setHasFixedSize(true);
        views.calendarRV.setAdapter(new CalendarAdapterBig(
                calendar,
                CalendarActivity.this,
                data,
                themeColor,
                stepWidth,
                stepHeight,
                isNight,
                whiteColor,
                blackColor
        ));
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
}