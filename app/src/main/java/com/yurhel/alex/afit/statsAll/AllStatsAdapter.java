package com.yurhel.alex.afit.statsAll;

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

public class AllStatsAdapter extends RecyclerView.Adapter<AllStatsAdapter.MyViewHolder> {
    private final Click click;
    Context context;
    ArrayList<Obj> data;
    int colorShadow;

    public AllStatsAdapter(
            Context context,
            Click click,
            ArrayList<Obj> data,
            int colorShadow
    ) {
        this.context = context;
        this.data = data;
        this.click = click;
        this.colorShadow = colorShadow;
    }

    @NonNull
    @Override
    public AllStatsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AllStatsAdapter.MyViewHolder(RowStatsBinding.inflate(LayoutInflater.from(context), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AllStatsAdapter.MyViewHolder holder, int pos) {
        Obj obj = data.get(pos);
        if (obj.time != null/*Is exercise*/) {
            if (!obj.allWeights.equals("")) {
                holder.tvWeights.setVisibility(View.VISIBLE);
                holder.tvWeights.setText(obj.allWeights);
                holder.tvWeights.setTextColor(obj.color);
                holder.tvWeights.setShadowLayer(1,1,1, colorShadow);
            } else {
                holder.tvWeights.setVisibility(View.GONE);
            }
            holder.tvTime.setText(obj.time);
            holder.tvTime.setTextColor(obj.color);
            holder.tvTime.setShadowLayer(1,1,1, colorShadow);
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
        holder.tvDate.setShadowLayer(1,1,1, colorShadow);
        holder.tvResult.setText(obj.longerValue);
        holder.tvResult.setTextColor(obj.color);
        holder.tvResult.setShadowLayer(1,1,1, colorShadow);
        holder.tvMain.setTextColor(obj.color);
        holder.tvMain.setShadowLayer(1,1,1, colorShadow);

        holder.itemView.setOnLongClickListener(v1 -> {
            if (pos != RecyclerView.NO_POSITION) click.onClickItem(pos, (obj.time != null/*Is exercise*/) ? "ex" : "st");
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvMain, tvResult, tvTime, tvDate, tvWeights;
        public MyViewHolder(@NonNull RowStatsBinding views) {
            super(views.getRoot());
            tvMain = views.mainValue;
            tvDate = views.rightValue;
            tvResult = views.addValue;
            tvTime = views.time;
            tvWeights = views.addValue2;
        }
    }
}
