package com.yurhel.alex.afit;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;

public class ManyStatsAdapter extends RecyclerView.Adapter<ManyStatsAdapter.MyViewHolder> {
    Context context;
    ArrayList<MyObject> data;

    public ManyStatsAdapter(Context context, ArrayList<MyObject> data) {
        this.context = context;
        this.data = data;
    }

    @NonNull
    @Override
    public ManyStatsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ManyStatsAdapter.MyViewHolder(LayoutInflater.from(context).inflate(R.layout.row_stats, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ManyStatsAdapter.MyViewHolder holder, int pos) {
        MyObject obj = data.get(pos);
        if (obj.time != null/*Is exercise*/) {
            if (!obj.allWeights.equals("")) {
                holder.tvWeights.setText(obj.allWeights);
                holder.tvWeights.setTextColor(obj.color);
                holder.tvWeights.setVisibility(View.VISIBLE);
            } else {
                holder.tvWeights.setVisibility(View.GONE);
            }
            holder.tvTime.setText(obj.time);
            holder.tvTime.setTextColor(obj.color);
            holder.tvTime.setVisibility(View.VISIBLE);
        } else {
            holder.tvTime.setVisibility(View.GONE);
        }
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(obj.date);
        String date = DateFormat.format("EEE", c) + ", " + Help.dateFormat(context, c.getTime());
        holder.tvDate.setText(date);
        holder.tvDate.setTextColor(obj.color);
        holder.tvResult.setText(obj.longerValue);
        holder.tvResult.setTextColor(obj.color);
        holder.tvMain.setText((obj.time != null/*Is exercise*/) ? ""+(int)obj.mainValue: ""+obj.mainValue);
        holder.tvMain.setTextColor(obj.color);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvMain, tvResult, tvTime, tvDate, tvWeights;
        public MyViewHolder(@NonNull View view) {
            super(view);
            tvMain = view.findViewById(R.id.mainValue);
            tvDate = view.findViewById(R.id.rightValue);
            tvResult = view.findViewById(R.id.addValue);
            tvTime = view.findViewById(R.id.time);
            tvWeights = view.findViewById(R.id.addValue2);
        }
    }
}
