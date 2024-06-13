package com.yurhel.alex.afit.calendar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yurhel.alex.afit.core.Obj;
import com.yurhel.alex.afit.databinding.RowCalendarBigBinding;

import java.util.ArrayList;
import java.util.Calendar;

public class CalendarAdapterBig extends RecyclerView.Adapter<CalendarAdapterBig.MyViewHolder> {

    Calendar calBefore;
    Calendar calAfter;

    Calendar calendar;
    Context context;
    ArrayList<Obj> data;
    int themeColor;
    int stepWidth;
    int stepHeight;
    boolean isNight;
    int whiteColor;
    int blackColor;

    public CalendarAdapterBig(
            Calendar calendar,
            Context context,
            ArrayList<Obj> data,
            int themeColor,
            int stepWidth,
            int stepHeight,
            boolean isNight,
            int whiteColor,
            int blackColor
    ) {
        this.calBefore = Calendar.getInstance();
        this.calBefore.setTime(calendar.getTime());
        this.calBefore.set(Calendar.DAY_OF_MONTH, 1);
        this.calBefore.set(Calendar.MONTH, calBefore.get(Calendar.MONTH)-1);
        this.calAfter = Calendar.getInstance();
        this.calAfter.setTime(calendar.getTime());
        this.calAfter.set(Calendar.DAY_OF_MONTH, 1);
        this.calAfter.set(Calendar.MONTH, calAfter.get(Calendar.MONTH)+1);

        this.calendar = calendar;
        this.context = context;
        this.data = data;
        this.themeColor = themeColor;
        this.stepWidth = stepWidth;
        this.stepHeight = stepHeight;
        this.isNight = isNight;
        this.whiteColor = whiteColor;
        this.blackColor = blackColor;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(
                RowCalendarBigBinding.inflate(LayoutInflater.from(context), parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int pos) {
        Calendar c;
        if (pos == 0) c = calBefore;
        else if (pos == 2) c = calAfter;
        else c = calendar;
        holder.rv.setLayoutManager(new GridLayoutManager(context, 7));
        holder.rv.setHasFixedSize(true);
        holder.rv.setAdapter(new CalendarAdapter(
                context,
                data,
                c,
                themeColor,
                stepWidth,
                stepHeight,
                isNight,
                whiteColor,
                blackColor
        ));
    }

    @Override
    public int getItemCount() {
        return 3;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        RecyclerView rv;
        public MyViewHolder(@NonNull RowCalendarBigBinding views) {
            super(views.getRoot());
            rv = views.calendarItem;
        }
    }
}
