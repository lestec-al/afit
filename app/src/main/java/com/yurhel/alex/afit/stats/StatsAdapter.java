package com.yurhel.alex.afit.stats;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yurhel.alex.afit.core.Click;
import com.yurhel.alex.afit.core.Help;
import com.yurhel.alex.afit.core.Obj;
import com.yurhel.alex.afit.databinding.RowStatsBinding;

import java.util.ArrayList;
import java.util.Calendar;

public class StatsAdapter extends RecyclerView.Adapter<StatsAdapter.MyViewHolder> {
    private final Click click;
    Context context;
    ArrayList<Obj> data;

    public StatsAdapter(Context context, ArrayList<Obj> data, Click click) {
        this.context = context;
        this.data = data;
        this.click = click;
    }

    @NonNull
    @Override
    public StatsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new StatsAdapter.MyViewHolder(
                RowStatsBinding.inflate(LayoutInflater.from(context), parent, false), click
        );
    }

    @Override
    public void onBindViewHolder(@NonNull StatsAdapter.MyViewHolder holder, int pos) {
        Obj obj = data.get(pos);
        if (obj.time != null/*Is exercise*/) {
            if (!obj.allWeights.equals("")) {
                holder.tvWeights.setVisibility(View.VISIBLE);
                holder.tvWeights.setText(obj.allWeights);
            } else {
                holder.tvWeights.setVisibility(View.GONE);
            }
            holder.tvTime.setText(obj.time);
            holder.tvMain.setText(String.valueOf((int)obj.mainValue));
        } else {
            holder.tvMain.setText(String.valueOf(obj.mainValue));
        }
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(obj.date);
        String date = DateFormat.format("EEE", c) + ", " + Help.dateFormat(context, c.getTime());
        holder.tvDate.setText(date);
        holder.tvResult.setText(obj.longerValue);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvMain, tvResult, tvTime, tvDate, tvWeights;

        public MyViewHolder(@NonNull RowStatsBinding views, Click click) {
            super(views.getRoot());
            tvMain = views.mainValue;
            tvDate = views.rightValue;
            tvResult = views.addValue;
            tvTime = views.time;
            tvWeights = views.addValue2;

            views.getRoot().setOnLongClickListener(v1 -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) click.onClickItem(pos, "");
                return true;
            });
        }
    }
}
