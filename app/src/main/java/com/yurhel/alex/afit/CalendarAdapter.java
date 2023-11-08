package com.yurhel.alex.afit;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Objects;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.MyViewHolder> {
    Context context;
    LinkedHashMap<MyObject, MyObject> data;
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

    public CalendarAdapter(Context context, LinkedHashMap<MyObject, MyObject> data, Calendar calendar, int color, int stepWidth, int stepHeight) {
        this.context = context;
        this.data = data;
        this.themeColor = color;
        this.calendar = calendar;
        this.stepWidth = stepWidth;
        this.stepHeight = stepHeight;
        this.days = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        this.calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH)-1);
        this.day1 = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        this.calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH)+1);
        this.whiteColor = context.getColor(R.color.white);
        this.blackColor = context.getColor(R.color.dark);
        this.isNight = (context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        return new MyViewHolder(inflater.inflate(R.layout.row_calendar, parent, false), stepWidth, stepHeight);
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
            // Today
            if (c.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) && c.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) && c.get(Calendar.DAY_OF_MONTH) == day) {
                holder.day.setBackground(AppCompatResources.getDrawable(context, R.drawable.rectangle_calendar));
                holder.day.setBackgroundTintList(ColorStateList.valueOf(themeColor));
                holder.day.setTextColor((isNight)? blackColor: whiteColor);
            }
            // Set short data for day
            LinkedHashMap<MyObject, String> dataR = new LinkedHashMap<>();
            Date date = null;
            boolean stop = false;
            TextView last = null;
            for (MyObject entry: data.keySet()) {
                MyObject objUp = Objects.requireNonNull(data.get(entry));
                c.setTimeInMillis(entry.date);
                if (c.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) && c.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) && c.get(Calendar.DAY_OF_MONTH) == day) {
                    if (!stop) {
                        // Set short
                        TextView t = new TextView(context);
                        t.setBackground(AppCompatResources.getDrawable(context, R.drawable.rectangle_calendar));
                        t.setBackgroundTintList(ColorStateList.valueOf(objUp.color));
                        if (objUp.color == whiteColor) {
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
                            String text = (entry.time != null/*Is exercise*/) ? ""+(int)entry.mainValue: ""+entry.mainValue;
                            t.setText((text.length() > 4)?text.substring(0,4): text);
                            last = t;
                        }
                    }
                    dataR.put(entry, objUp.name);
                    date = c.getTime();
                }
            }
            // Set full data for day
            Date dateDay = date;
            holder.itemView.setOnClickListener(view1 -> {
                if (dataR.size() > 0) {
                    // Calendar stats dialog
                    Dialog dialog = new Dialog(context);
                    dialog.setContentView(R.layout.dialog_calendar_stats);
                    Window window = dialog.getWindow();
                    if (window != null) window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    ImageButton back = dialog.findViewById(R.id.buttonBack);
                    back.setOnClickListener(view -> dialog.cancel());
                    back.setColorFilter(themeColor);
                    LinearLayout ll = dialog.findViewById(R.id.itemsLayout);
                    TextView title = dialog.findViewById(R.id.label);
                    title.setText(Help.dateFormat(context, dateDay));
                    // Add all stats for day
                    LayoutInflater inflater = LayoutInflater.from(context);
                    for (MyObject obj: dataR.keySet()) {
                        View v = inflater.inflate(R.layout.row_stats, ll, false);
                        TextView tvMain, tvResultL, tvTime, tvWeights, tvName;
                        tvMain = v.findViewById(R.id.mainValue);
                        tvName = v.findViewById(R.id.rightValue);
                        tvResultL = v.findViewById(R.id.addValue);
                        if (obj.time != null/*Is exercise*/) {
                            tvTime = v.findViewById(R.id.time);
                            tvWeights = v.findViewById(R.id.addValue2);
                            if (!obj.allWeights.equals("")) {
                                tvWeights.setVisibility(View.VISIBLE);
                                tvWeights.setText(obj.allWeights);
                            } else {
                                tvWeights.setVisibility(View.GONE);
                            }
                            tvMain.setText(String.valueOf((int) obj.mainValue));
                            tvResultL.setText(obj.longerValue);
                            tvTime.setText(obj.time);
                        } else {
                            tvResultL.setText(obj.longerValue);
                            tvMain.setText(String.valueOf(obj.mainValue));
                        }
                        tvName.setText(dataR.get(obj));
                        ll.addView(v);
                    }
                    dialog.show();
                }
            });
        } else { // Set last/next month day
            if (day < 1) {
                holder.day.setText(String.valueOf(day1 + day));
            } else {
                if (day1 > 15)
                    day1 = 0;
                day1 += 1;
                holder.day.setText(String.valueOf(day1));
            }
            if (isNight)
                holder.day.setTextColor(context.getColor(R.color.grey));
            else
                holder.day.setEnabled(false);
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