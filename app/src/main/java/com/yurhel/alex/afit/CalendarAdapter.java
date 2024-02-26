package com.yurhel.alex.afit;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
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

import java.util.ArrayList;
import java.util.Calendar;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.MyViewHolder> {
    Context context;
    ArrayList<MyObject> data;
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
            ArrayList<MyObject> data,
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
        LayoutInflater inflater = LayoutInflater.from(context);
        return new MyViewHolder(inflater.inflate(R.layout.row_calendar_day, parent, false), stepWidth, stepHeight);
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
            ArrayList<MyObject> dataThisDay = new ArrayList<>();
            boolean stop = false;
            TextView last = null;
            for (MyObject i: data) {
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
            if (dataThisDay.size() > 0) {
                Calendar c1 = Calendar.getInstance();
                c1.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
                c1.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
                c1.set(Calendar.DAY_OF_MONTH, day);

                holder.itemView.setOnClickListener(v1 -> {
                    // Calendar stats dialog
                    Dialog dialog = new Dialog(context);
                    dialog.setContentView(R.layout.dialog_calendar_stats);
                    Window window = dialog.getWindow();
                    if (window != null) window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    LinearLayout ll = dialog.findViewById(R.id.itemsLayout);

                    TextView title = dialog.findViewById(R.id.label);
                    title.setText(Help.dateFormat(context, c1.getTime()));

                    // Calc training time
                    int allTimeMin = 0;
                    int allTimeSec = 0;

                    int colorShadow = (isNight) ? whiteColor : blackColor;
                    // Set all stats for day to dialog
                    LayoutInflater inflater = LayoutInflater.from(context);
                    for (MyObject i: dataThisDay) {
                        View v = inflater.inflate(R.layout.row_stats, ll, false);
                        TextView tvMain = v.findViewById(R.id.mainValue);
                        tvMain.setTextColor(i.color);
                        tvMain.setShadowLayer(1,1,1, colorShadow);
                        TextView tvResultL = v.findViewById(R.id.addValue);
                        tvResultL.setTextColor(i.color);
                        tvResultL.setShadowLayer(1,1,1, colorShadow);
                        if (i.time != null/*Is exercise*/) {
                            // Get training times
                            String[] t = i.time.split(":");
                            allTimeMin += Integer.parseInt(t[0]);
                            allTimeSec += Integer.parseInt(t[1]);

                            TextView tvTime = v.findViewById(R.id.time);
                            TextView tvWeights = v.findViewById(R.id.addValue2);
                            if (!i.allWeights.equals("")) {
                                tvWeights.setVisibility(View.VISIBLE);
                                tvWeights.setText(i.allWeights);
                                tvWeights.setTextColor(i.color);
                                tvWeights.setShadowLayer(1,1,1, colorShadow);
                            } else {
                                tvWeights.setVisibility(View.GONE);
                            }
                            tvMain.setText(String.valueOf((int) i.mainValue));
                            tvResultL.setText(i.longerValue);
                            tvTime.setText(i.time);
                            tvTime.setTextColor(i.color);
                            tvTime.setShadowLayer(1,1,1, colorShadow);
                        } else {
                            tvResultL.setText(i.longerValue);
                            tvMain.setText(String.valueOf(i.mainValue));
                        }
                        TextView tvName = v.findViewById(R.id.rightValue);
                        tvName.setText(i.name);
                        tvName.setTextColor(i.color);
                        tvName.setShadowLayer(1,1,1, colorShadow);
                        ll.addView(v);
                    }

                    // Finish calc training time
                    TextView allTime = dialog.findViewById(R.id.allTime);
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
            }
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

        public MyViewHolder(@NonNull View view, int stepWidth, int stepHeight) {
            super(view);
            day = view.findViewById(R.id.dayItem);
            layout = view.findViewById(R.id.dayItemsLayout);
            view.setMinimumHeight(stepHeight);
            view.setMinimumWidth(stepWidth);
        }
    }
}