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

public class StatsAdapter extends RecyclerView.Adapter<StatsAdapter.MyViewHolder> {
    private final ClickInterface clickInterface;
    Context context;
    ArrayList<MyObject> data;
    int dataSize;
    boolean exercise;

    public StatsAdapter(Context context, ArrayList<MyObject> data, boolean exercise, ClickInterface clickInterface) {
        this.context = context;
        this.data = data;
        this.exercise = exercise;
        this.clickInterface = clickInterface;
        this.dataSize = data.size();
    }

    @NonNull
    @Override
    public StatsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new StatsAdapter.MyViewHolder(
                LayoutInflater.from(context).inflate(R.layout.row_stats, parent, false), clickInterface, exercise
        );
    }

    @Override
    public void onBindViewHolder(@NonNull StatsAdapter.MyViewHolder holder, int pos) {
        MyObject obj = data.get(pos);
        if (exercise) {
            if (!obj.weights.equals("")) {
                holder.tvWeights.setVisibility(View.VISIBLE);
                holder.tvWeights.setText(obj.weights);
            }
            holder.tvMain.setText(obj.result_s);
            holder.tvResult.setText(obj.result_l);
            holder.tvTime.setText(obj.time);
        } else {
            holder.tvResult.setText(obj.notes);
            holder.tvMain.setText(String.valueOf(obj.value));
        }
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(obj.date);
        String date = DateFormat.format("EEE", c) + ", " + Help.dateFormat(context, c.getTime());
        holder.tvDate.setText(date);
    }

    @Override
    public int getItemCount() {
        return dataSize;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvMain, tvResult, tvTime, tvDate, tvWeights;

        public MyViewHolder(@NonNull View view, ClickInterface clickInterface, boolean exercise) {
            super(view);
            tvMain = view.findViewById(R.id.r_view_result_s);
            tvDate = view.findViewById(R.id.r_view_date);
            tvResult = view.findViewById(R.id.r_view_result_l);
            if (exercise) {
                tvTime = view.findViewById(R.id.r_view_time);
                tvWeights = view.findViewById(R.id.r_view_result_weight);
            } else {
                view.setOnClickListener(view1 -> {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION)
                        clickInterface.onClickItem(pos, "");
                });
            }
        }
    }
}
