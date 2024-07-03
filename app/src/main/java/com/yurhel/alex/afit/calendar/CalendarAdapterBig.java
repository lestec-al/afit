package com.yurhel.alex.afit.calendar;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.yurhel.alex.afit.R;
import com.yurhel.alex.afit.core.Obj;
import com.yurhel.alex.afit.databinding.RowCalendarBigBinding;
import com.yurhel.alex.afit.databinding.RowCalendarDayBinding;

import java.util.ArrayList;
import java.util.Calendar;

public class CalendarAdapterBig extends RecyclerView.Adapter<CalendarAdapterBig.MyViewHolder> {

    Calendar calBefore;
    Calendar calAfter;
    Calendar calEdit;
    Calendar calToday;
    Context context;
    ArrayList<Obj> data;
    int themeColor;
    int stepHeight;
    boolean isNight;
    int whiteColor;
    int blackColor;
    LayoutInflater ll;
    CalendarClick click;
    ViewGroup.LayoutParams layoutParams;

    public CalendarAdapterBig(
            Calendar calendar,
            Context context,
            ArrayList<Obj> data,
            int themeColor,
            int stepWidth,
            int stepHeight,
            boolean isNight,
            int whiteColor,
            int blackColor,
            CalendarClick click
    ) {
        this.calBefore = Calendar.getInstance();
        this.calBefore.setTime(calendar.getTime());
        this.calBefore.set(Calendar.DAY_OF_MONTH, 1);
        this.calBefore.set(Calendar.HOUR_OF_DAY, 0);
        this.calBefore.set(Calendar.MONTH, calBefore.get(Calendar.MONTH)-1);
        this.calAfter = Calendar.getInstance();
        this.calAfter.setTime(calendar.getTime());
        this.calAfter.set(Calendar.DAY_OF_MONTH, 1);
        this.calAfter.set(Calendar.MONTH, calAfter.get(Calendar.MONTH)+1);
        Calendar calAfterPlus = Calendar.getInstance();
        calAfterPlus.setTime(calendar.getTime());
        calAfterPlus.set(Calendar.DAY_OF_MONTH, 1);
        calAfterPlus.set(Calendar.MONTH, calAfterPlus.get(Calendar.MONTH)+2);

        this.calEdit = calendar;
        this.context = context;
        this.themeColor = themeColor;
        this.stepHeight = stepHeight;
        this.isNight = isNight;
        this.whiteColor = whiteColor;
        this.blackColor = blackColor;

        this.ll = LayoutInflater.from(context);
        this.click = click;
        this.calToday = Calendar.getInstance();
        this.layoutParams = new ViewGroup.LayoutParams(stepWidth, stepHeight);

        // Get data for 3 months ?
        ArrayList<Obj> dataFor3Months = new ArrayList<>();
        Calendar c = Calendar.getInstance();
        for (Obj i: data) {
            c.setTimeInMillis(i.date);
            if (c.after(calBefore) && c.before(calAfterPlus)) dataFor3Months.add(i);
        }
        this.data = dataFor3Months;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(
                RowCalendarBigBinding.inflate(LayoutInflater.from(context), parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holderCalendar, int posCalendar) {
        Calendar calendarChosen;
        if (posCalendar == 0) calendarChosen = calBefore;
        else if (posCalendar == 2) calendarChosen = calAfter;
        else calendarChosen = calEdit;

        // Calc the remaining days for the previous month
        int day = 1;
        int calendarWeekDay = calendarChosen.getFirstDayOfWeek();
        int firstMonthWeekDay = calendarChosen.get(Calendar.DAY_OF_WEEK);
        if (calendarWeekDay != firstMonthWeekDay) {
            for (int i = 1; i < 8; i += 1) {
                calendarWeekDay += 1;
                if (calendarWeekDay > 7) calendarWeekDay = 1;
                if (calendarWeekDay == firstMonthWeekDay) {
                    day -= i;
                    break;
                }
            }
        }

        // Get data for 1 month ?
        ArrayList<Obj> dataForMonth = new ArrayList<>();
        Calendar c = Calendar.getInstance();
        for (Obj i: data) {
            c.setTimeInMillis(i.date);
            if (
                    c.get(Calendar.YEAR) == calendarChosen.get(Calendar.YEAR) &&
                    c.get(Calendar.MONTH) == calendarChosen.get(Calendar.MONTH)
            ) dataForMonth.add(i);
        }

        // Setup days
        for (int pos = 0; pos < 42; pos++) {
            RowCalendarDayBinding v = RowCalendarDayBinding.inflate(ll);
            FrameLayout root = v.getRoot();
            root.setLayoutParams(layoutParams);
            holderCalendar.calendarItem.addView(root);

            Calendar cForLabel = Calendar.getInstance();
            cForLabel.set(Calendar.YEAR, calendarChosen.get(Calendar.YEAR));
            cForLabel.set(Calendar.MONTH, calendarChosen.get(Calendar.MONTH));
            cForLabel.set(Calendar.DAY_OF_MONTH, day); // Day may be negative

            if (cForLabel.get(Calendar.MONTH) == calendarChosen.get(Calendar.MONTH)) {
                // Set day
                v.dayItem.setText(String.valueOf(day));
                // Change color for today
                if (
                        calToday.get(Calendar.YEAR) == cForLabel.get(Calendar.YEAR) &&
                        calToday.get(Calendar.MONTH) == cForLabel.get(Calendar.MONTH) &&
                        calToday.get(Calendar.DAY_OF_MONTH) == day
                ) {
                    v.dayItem.setBackground(AppCompatResources.getDrawable(context, R.drawable.rectangle_frame_filled));
                    v.dayItem.setBackgroundTintList(ColorStateList.valueOf(themeColor));
                    v.dayItem.setTextColor((isNight) ? blackColor : whiteColor);
                }

                ArrayList<Obj> dataThisDay = new ArrayList<>();
                boolean stop = false;
                TextView last = null;
                for (Obj i: dataForMonth) {
                    c.setTimeInMillis(i.date);
                    // If day from position == day from data
                    if (c.get(Calendar.DAY_OF_MONTH) == cForLabel.get(Calendar.DAY_OF_MONTH)) {
                        // If enough space in day tile is remained
                        if (!stop) {
                            // Set short data for day
                            TextView t = new TextView(context);
                            t.setBackground(AppCompatResources.getDrawable(context, R.drawable.rectangle_frame_filled));
                            t.setBackgroundTintList(ColorStateList.valueOf(i.color));
                            if (i.color == whiteColor) t.setTextColor(blackColor);
                            else {
                                t.setTextColor(whiteColor);
                                t.setShadowLayer(14,1,1,blackColor);
                            }
                            t.setGravity(Gravity.CENTER);
                            v.dayItemsLayout.addView(t);
                            // Get height
                            root.measure(0,0);
                            if (root.getMeasuredHeight() > stepHeight) {
                                v.dayItemsLayout.removeViewAt(v.dayItemsLayout.getChildCount()-1);
                                stop = true;
                                if (last != null) last.setText("+");
                            }
                            else {
                                String text = (i.time != null/*Is exercise*/) ?
                                        String.valueOf((int) i.mainValue) :
                                        String.valueOf(i.mainValue);
                                t.setText((text.length() > 4) ? text.substring(0,4) : text);
                                last = t;
                            }
                        }
                        dataThisDay.add(i);
                    }
                }
                // Click on item
                if (posCalendar == 1 && !dataThisDay.isEmpty())
                    root.setOnClickListener(v1 -> click.onClick(cForLabel.getTime(), dataThisDay));
            }
            else {
                // Set last/next month day
                v.dayItem.setText(String.valueOf(cForLabel.get(Calendar.DAY_OF_MONTH)));
                if (isNight) v.dayItem.setTextColor(context.getColor(R.color.grey));
                else v.dayItem.setEnabled(false);
            }

            day++;
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        GridLayout calendarItem;
        public MyViewHolder(@NonNull RowCalendarBigBinding views) {
            super(views.getRoot());
            calendarItem = views.getRoot();
        }
    }
}
