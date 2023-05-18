package com.yurhel.alex.afit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Objects;

public class CalendarActivity extends AppCompatActivity {
    int themeColor;
    Calendar calendar;
    RecyclerView calendarView;
    LinkedHashMap<MyObject, MyObject> data;
    LinkedHashMap<String, Integer> colors;
    int stepWidth;
    int stepHeight;
    int calendarViewWidth;
    androidx.appcompat.app.ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        themeColor = Help.getMainGreyColor(this);
        DB db = new DB(this);
        data = db.getAll();
        colors = db.getAllObjColors();
        calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendarView = findViewById(R.id.calendar_grid);
        // Styling
        actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        Help.setActionBackIconColor(this, themeColor, actionBar);
        // Create days of week, update calendar
        LinearLayout headerDays = findViewById(R.id.calendar_header_days);
        headerDays.post(() -> {
            Calendar c = Calendar.getInstance();
            int d = c.getFirstDayOfWeek();
            c.set(Calendar.DAY_OF_WEEK, d);
            stepHeight = (calendarView.getHeight()) / 6;
            calendarViewWidth = calendarView.getWidth();
            stepWidth = headerDays.getWidth() / 7;
            for (int i = 1; i < 8; i += 1) {
                TextView tv = new TextView(this);
                tv.setWidth(stepWidth);
                tv.setGravity(Gravity.CENTER);
                tv.setText(DateFormat.format("EEE", c));
                d = (d+1 <= 7) ? d+1 : 1;
                c.set(Calendar.DAY_OF_WEEK, d);
                headerDays.addView(tv);
            }
            updateCalendar();
        });
        // Scroll settings
        calendarView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            int pos = 1;
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                try {
                    int offset = recyclerView.computeHorizontalScrollOffset();
                    if (offset % calendarViewWidth == 0) { // Item in position - update calendar
                        pos = offset / calendarViewWidth;
                        if (pos == 0) {
                            calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH)-1);
                            updateCalendar();
                        } else if (pos == 2) {
                            calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH)+1);
                            updateCalendar();
                        }
                    } else { // Position between - scroll to item
                        RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(CalendarActivity.this) {
                            @Override
                            protected int calculateTimeForScrolling(int dx) {
                                return dx/2;
                            }
                        };
                        int scrollNumber = offset - calendarViewWidth;
                        if (scrollNumber < 0) {
                            if (scrollNumber <= -stepWidth *2)
                                smoothScroller.setTargetPosition(pos-1);
                            else
                                smoothScroller.setTargetPosition(pos);
                        } else {
                            if (scrollNumber >= stepWidth *2)
                                smoothScroller.setTargetPosition(pos+1);
                            else
                                smoothScroller.setTargetPosition(pos);
                        }
                        Objects.requireNonNull(calendarView.getLayoutManager()).startSmoothScroll(smoothScroller);
                    }
                } catch (Exception ignored) {}
            }
        });
    }

    public void updateCalendar() {
        actionBar.setTitle(DateFormat.format("MMM, yyyy", calendar));
        calendarView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        calendarView.setHasFixedSize(true);
        calendarView.setAdapter(new CalendarAdapterBig());
        calendarView.scrollToPosition(1);
    }

    // TOOLBAR
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mi = getMenuInflater();
        mi.inflate(R.menu.calendar, menu);
        menu.findItem(R.id.action_date_now).setTitle(Help.dateFormat(this, new Date()));
        Help.setActionIconsColor(themeColor, menu, new int[] {R.id.action_date_now, R.id.action_set_date});
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            onBackPressed();
            return true;

        } else if (item.getItemId() == R.id.action_date_now) {
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            updateCalendar();
            return true;

        } else if (item.getItemId() == R.id.action_set_date) {
            // Custom date picker dialog
            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.dialog_add_stats);
            Button ok = dialog.findViewById(R.id.button_create_stats_dialog);
            Button cancel = dialog.findViewById(R.id.button_cancel_stats_dialog);
            Help.setButtonsTextColor(themeColor, new Button[] {ok, cancel});
            dialog.findViewById(R.id.button_pick_date_stats_dialog).setVisibility(View.GONE);
            NumberPicker pickerMonth = dialog.findViewById(R.id.number_picker_0);
            NumberPicker pickerYear = dialog.findViewById(R.id.number_picker_1);
            // Get all months
            Calendar c = Calendar.getInstance();
            c.set(Calendar.DAY_OF_MONTH, 1);
            int max = c.getActualMaximum(Calendar.MONTH);
            String[] months = new String[max+1];
            for (int i = 0; i <= max; i += 1) {
                c.set(Calendar.MONTH, i);
                months[i] = String.valueOf(DateFormat.format("MMM", c));
            }
            pickerMonth.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
            pickerMonth.setMaxValue(max);
            pickerMonth.setMinValue(0);
            pickerMonth.setDisplayedValues(months);
            pickerMonth.setWrapSelectorWheel(false);
            pickerMonth.setValue(calendar.get(Calendar.MONTH));
            pickerYear.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
            pickerYear.setMaxValue(calendar.get(Calendar.YEAR)+10);
            pickerYear.setMinValue(calendar.get(Calendar.YEAR)-10);
            pickerYear.setWrapSelectorWheel(false);
            pickerYear.setValue(calendar.get(Calendar.YEAR));
            pickerYear.setOnValueChangedListener((picker, oldVal, newVal) -> {
                if (newVal == pickerYear.getMaxValue())
                    pickerYear.setMaxValue(newVal+5);
                else if (newVal == pickerYear.getMinValue())
                    pickerYear.setMinValue(newVal-5);
            });
            ok.setOnClickListener(view -> {
                calendar.set(Calendar.MONTH, pickerMonth.getValue());
                calendar.set(Calendar.YEAR, pickerYear.getValue());
                dialog.cancel();
                updateCalendar();
            });
            cancel.setOnClickListener(view -> dialog.cancel());
            dialog.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class CalendarAdapterBig extends RecyclerView.Adapter<CalendarAdapterBig.MyViewHolder> {
        Calendar cal;
        public CalendarAdapterBig() {
            this.cal = Calendar.getInstance();
            this.cal.setTime(calendar.getTime());
        }
        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(CalendarActivity.this);
            return new MyViewHolder(inflater.inflate(R.layout.row_calendar_rv, parent, false));
        }
        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int pos) {
            if (pos == 0)
                cal.set(Calendar.MONTH, cal.get(Calendar.MONTH)-1);
            else if (pos == 1)
                cal.set(Calendar.MONTH, cal.get(Calendar.MONTH));
            else if (pos == 2)
                cal.set(Calendar.MONTH, cal.get(Calendar.MONTH)+1);
            holder.rv.setLayoutManager(new GridLayoutManager(CalendarActivity.this, 7));
            holder.rv.setHasFixedSize(true);
            holder.rv.setAdapter(new CalendarAdapter(CalendarActivity.this, data, cal, themeColor, stepWidth, stepHeight, colors));
        }
        @Override
        public int getItemCount() {
            return 3;
        }
        public class MyViewHolder extends RecyclerView.ViewHolder {
            RecyclerView rv;
            public MyViewHolder(@NonNull View view) {
                super(view);
                rv = view.findViewById(R.id.calendar_grid_row);
            }
        }
    }
}