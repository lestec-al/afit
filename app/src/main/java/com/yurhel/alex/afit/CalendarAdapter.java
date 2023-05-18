package com.yurhel.alex.afit;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    LinkedHashMap<String, Integer> colors;
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

    public CalendarAdapter(Context context, LinkedHashMap<MyObject, MyObject> data, Calendar calendar, int color, int stepWidth, int stepHeight, LinkedHashMap<String, Integer> colors) {
        this.context = context;
        this.data = data;
        this.themeColor = color;
        this.calendar = calendar;
        this.stepWidth = stepWidth;
        this.stepHeight = stepHeight;
        this.colors = colors;
        this.days = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        this.calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH)-1);
        this.day1 = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        this.calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH)+1);
        this.whiteColor = context.getColor(R.color.white);
        this.blackColor = context.getColor(R.color.black);
        this.isNight = Help.isNightMode(context);
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
                if (isNight)
                    holder.day.setTextColor(blackColor);
                else
                    holder.day.setTextColor(whiteColor);
            }
            // Set short data for day
            LinkedHashMap<MyObject, String> dataR = new LinkedHashMap<>();
            Date d = null;
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
                        Integer itemColor = colors.get((entry.result_s == null) ? objUp.id+"_st": objUp.id+"_ex");
                        int backgroundColor = (itemColor != null && itemColor != 0) ? itemColor: themeColor;
                        t.setBackgroundTintList(ColorStateList.valueOf(backgroundColor));
                        if (backgroundColor == whiteColor)
                            t.setTextColor(blackColor);
                        else
                            t.setTextColor(whiteColor);
                        t.setGravity(Gravity.CENTER);
                        holder.layout.addView(t);
                        // Get height
                        holder.itemView.measure(0,0);
                        int h = holder.itemView.getMeasuredHeight();
                        if (h > stepHeight) {
                            holder.layout.removeViewAt(holder.layout.getChildCount()-1);
                            stop = true;
                            assert last != null;
                            last.setText("+");
                        } else {
                            t.setText((entry.result_s == null) ? ""+entry.value: ""+entry.result_s);
                            last = t;
                        }
                    }
                    dataR.put(entry, objUp.name);
                    d = c.getTime();
                }
            }
            // Set full data for day
            if (dataR.size() > 0) {
                Date d1 = d;
                holder.itemView.setOnClickListener(view1 -> {
                    // Calendar stats dialog
                    Dialog dialog = new Dialog(context);
                    dialog.setContentView(R.layout.dialog_calendar_stats);
                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    ImageButton back = dialog.findViewById(R.id.button_back_calendar_stats_dialog);
                    back.setOnClickListener(view -> dialog.cancel());
                    Help.setImageButtonsColor(themeColor, new ImageButton[] {back});
                    LinearLayout ll = dialog.findViewById(R.id.stats_layout_calendar_dialog);
                    TextView title = dialog.findViewById(R.id.tv_label_calendar_stats_dialog);
                    title.setText(Help.dateFormat(context, d1));
                    // Add all stats for day
                    LayoutInflater inflater = LayoutInflater.from(context);
                    for (MyObject obj: dataR.keySet()) {
                        View v = inflater.inflate(R.layout.row_stats, ll, false);
                        TextView tvMain, tvResultL, tvTime, tvWeights, tvName;
                        tvMain = v.findViewById(R.id.r_view_result_s);
                        tvName = v.findViewById(R.id.r_view_date);
                        if (obj.result_s != null) {
                            tvResultL = v.findViewById(R.id.r_view_result_l);
                            tvTime = v.findViewById(R.id.r_view_time);
                            tvWeights = v.findViewById(R.id.r_view_result_weight);
                            if (!obj.weights.equals("")) {
                                tvResultL.setGravity(Gravity.CENTER);
                                tvWeights.setVisibility(View.VISIBLE);
                                tvWeights.setText(obj.weights);
                            }
                            tvMain.setText(obj.result_s);
                            tvResultL.setText(obj.result_l);
                            tvTime.setText(obj.time);
                        } else {
                            tvMain.setText(String.valueOf(obj.value));
                        }
                        tvName.setText(dataR.get(obj));
                        ll.addView(v);
                    }
                    dialog.show();
                });
            }
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
            day = view.findViewById(R.id.text_day_calendar_row);
            layout = view.findViewById(R.id.info_layout_calendar_row);
            view.setMinimumHeight(stepHeight);
            view.setMinimumWidth(stepWidth);
        }
    }
}