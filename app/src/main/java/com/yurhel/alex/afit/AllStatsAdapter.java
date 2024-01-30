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

public class AllStatsAdapter extends RecyclerView.Adapter<AllStatsAdapter.MyViewHolder> {
    private final ClickInterface clickInterface;
    Context context;
    ArrayList<MyObject> data;

    public AllStatsAdapter(Context context, ArrayList<MyObject> data, ClickInterface clickInterface) {
        this.context = context;
        this.data = data;
        this.clickInterface = clickInterface;
    }

    @NonNull
    @Override
    public AllStatsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AllStatsAdapter.MyViewHolder(LayoutInflater.from(context).inflate(R.layout.row_stats, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AllStatsAdapter.MyViewHolder holder, int pos) {
        MyObject obj = data.get(pos);
        if (obj.time != null/*Is exercise*/) {
            if (!obj.allWeights.equals("")) {
                holder.tvWeights.setVisibility(View.VISIBLE);
                holder.tvWeights.setText(obj.allWeights);
                holder.tvWeights.setTextColor(obj.color);
            } else {
                holder.tvWeights.setVisibility(View.GONE);
            }
            holder.tvTime.setText(obj.time);
            holder.tvTime.setTextColor(obj.color);
            holder.tvTime.setVisibility(View.VISIBLE);
            holder.tvMain.setText(String.valueOf((int)obj.mainValue));
        } else {
            holder.tvTime.setVisibility(View.GONE);
            holder.tvMain.setText(String.valueOf(obj.mainValue));
        }
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(obj.date);
        String date = DateFormat.format("EEE", c) + ", " + Help.dateFormat(context, c.getTime());
        holder.tvDate.setText(date);
        holder.tvDate.setTextColor(obj.color);
        holder.tvResult.setText(obj.longerValue);
        holder.tvResult.setTextColor(obj.color);
        holder.tvMain.setTextColor(obj.color);

        holder.itemView.setOnLongClickListener(v1 -> {
            if (pos != RecyclerView.NO_POSITION) clickInterface.onClickItem(pos, (obj.time != null/*Is exercise*/) ? "ex" : "st");
            return true;
        });
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
