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

    public StatsAdapter(Context context, ArrayList<MyObject> data, ClickInterface clickInterface) {
        this.context = context;
        this.data = data;
        this.clickInterface = clickInterface;
    }

    @NonNull
    @Override
    public StatsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new StatsAdapter.MyViewHolder(
                LayoutInflater.from(context).inflate(R.layout.row_stats, parent, false), clickInterface
        );
    }

    @Override
    public void onBindViewHolder(@NonNull StatsAdapter.MyViewHolder holder, int pos) {
        MyObject obj = data.get(pos);
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

        public MyViewHolder(@NonNull View view, ClickInterface clickInterface) {
            super(view);
            tvMain = view.findViewById(R.id.mainValue);
            tvDate = view.findViewById(R.id.rightValue);
            tvResult = view.findViewById(R.id.addValue);
            tvTime = view.findViewById(R.id.time);
            tvWeights = view.findViewById(R.id.addValue2);

            view.setOnLongClickListener(v1 -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) clickInterface.onClickItem(pos, "");
                return true;
            });
        }
    }
}
