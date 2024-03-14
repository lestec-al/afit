package com.yurhel.alex.afit.calendar;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.yurhel.alex.afit.core.Help;
import com.yurhel.alex.afit.core.Obj;
import com.yurhel.alex.afit.R;
import com.yurhel.alex.afit.databinding.DialogCalendarStatsBinding;
import com.yurhel.alex.afit.databinding.RowCalendarDayBinding;
import com.yurhel.alex.afit.databinding.RowStatsBinding;

import java.util.ArrayList;
import java.util.Calendar;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.MyViewHolder> {
    Context context;
    ArrayList<Obj> data;
    int themeColor;
    Calendar calendar;
    int days;
    int day = 1;
    int day1;
    int stepWidth;
    int stepHeight;
    int whiteColor;
    int blackColor;
    boolean isNight;

    public CalendarAdapter(
            Context context,
            ArrayList<Obj> data,
            Calendar calendar,
            int color,
            int stepWidth,
            int stepHeight,
            boolean isNight,
            int whiteColor,
            int blackColor
    ) {
        this.context = context;
        this.data = data;
        this.themeColor = color;
        this.calendar = calendar;
        this.stepWidth = stepWidth;
        this.stepHeight = stepHeight;
        this.whiteColor = whiteColor;
        this.blackColor = blackColor;
        this.isNight = isNight;
        // Get days in this & previous months
        this.days = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        this.calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH)-1);
        this.day1 = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        this.calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH)+1);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(
                RowCalendarDayBinding.inflate(LayoutInflater.from(context), parent, false), stepWidth, stepHeight
        );
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int pos) {
        if (pos == 0) {
            int calendarWeekDay = calendar.getFirstDayOfWeek();
            int firstMonthWeekDay = calendar.get(Calendar.DAY_OF_WEEK);
            if (calendarWeekDay != firstMonthWeekDay) {
                for (int i = 1; i < 8; i += 1) {
                    calendarWeekDay += 1;
                    if (calendarWeekDay > 7)
                        calendarWeekDay = 1;
                    if (calendarWeekDay == firstMonthWeekDay) {
                        day -= i;
                        break;
                    }
                }
            }
        }
        if (day > 0 && day <= days) {
            holder.day.setText(String.valueOf(day));
            Calendar c = Calendar.getInstance();

            // Change color for today
            if (c.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) && c.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) && c.get(Calendar.DAY_OF_MONTH) == day) {
                holder.day.setBackground(AppCompatResources.getDrawable(context, R.drawable.rectangle_calendar));
                holder.day.setBackgroundTintList(ColorStateList.valueOf(themeColor));
                holder.day.setTextColor((isNight)? blackColor: whiteColor);
            }

            // Get all stats for day
            ArrayList<Obj> dataThisDay = new ArrayList<>();
            boolean stop = false;
            TextView last = null;
            for (Obj i: data) {
                c.setTimeInMillis(i.date);
                // If day from position == day from data
                if (c.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) && c.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) && c.get(Calendar.DAY_OF_MONTH) == day) {
                    // If enough space in day tile is remained
                    if (!stop) {
                        // Set short data for day
                        TextView t = new TextView(context);
                        t.setBackground(AppCompatResources.getDrawable(context, R.drawable.rectangle_calendar));
                        t.setBackgroundTintList(ColorStateList.valueOf(i.color));
                        if (i.color == whiteColor) {
                            t.setTextColor(blackColor);
                        } else {
                            t.setTextColor(whiteColor);
                            t.setShadowLayer(14,1,1,blackColor);
                        }
                        t.setGravity(Gravity.CENTER);
                        holder.layout.addView(t);
                        // Get height
                        holder.itemView.measure(0,0);
                        int h = holder.itemView.getMeasuredHeight();
                        if (h > stepHeight) {
                            holder.layout.removeViewAt(holder.layout.getChildCount()-1);
                            stop = true;
                            if (last != null)
                                last.setText("+");
                        } else {
                            String text = (i.time != null/*Is exercise*/) ? String.valueOf((int) i.mainValue) : String.valueOf(i.mainValue);
                            t.setText((text.length() > 4)?text.substring(0,4): text);
                            last = t;
                        }
                    }
                    dataThisDay.add(i);
                }
            }

            // Click on item
            Calendar c1 = Calendar.getInstance();
            c1.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
            c1.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
            c1.set(Calendar.DAY_OF_MONTH, day);

            holder.itemView.setOnClickListener(v1 -> {
                LayoutInflater inflater = LayoutInflater.from(context);

                // Calendar stats dialog
                Dialog dialog = new Dialog(context);
                DialogCalendarStatsBinding dView = DialogCalendarStatsBinding.inflate(inflater);
                dialog.setContentView(dView.getRoot());
                Window window = dialog.getWindow();
                if (window != null) {
                    window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                }
                dView.label.setText(Help.dateFormat(context, c1.getTime()));

                // Calc training time
                int allTimeMin = 0;
                int allTimeSec = 0;

                int colorShadow = (isNight) ? whiteColor : blackColor;
                // Set all stats for day to dialog
                int itemsCount = 0;
                RowStatsBinding vs = null;
                for (Obj i: dataThisDay) {
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
                    String allTimeText = allTimeMin + " " + context.getText(R.string.training_min);
                    allTime.setText(allTimeText);
                } else {
                    allTime.setVisibility(View.GONE);
                }

                dialog.show();
            });
        } else {
            // Set last/next month day
            if (day < 1) {
                holder.day.setText(String.valueOf(day1 + day));
            } else {
                if (day1 > 15)
                    day1 = 0;
                day1 += 1;
                holder.day.setText(String.valueOf(day1));
            }
            if (isNight) holder.day.setTextColor(context.getColor(R.color.grey));
            else holder.day.setEnabled(false);
        }
        day += 1;
    }

    @Override
    public int getItemCount() {
        return 42;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView day;
        LinearLayout layout;

        public MyViewHolder(@NonNull RowCalendarDayBinding views, int stepWidth, int stepHeight) {
            super(views.getRoot());
            day = views.dayItem;
            layout = views.dayItemsLayout;
            View root = views.getRoot();
            root.setMinimumHeight(stepHeight);
            root.setMinimumWidth(stepWidth);
        }
    }
}