package com.yurhel.alex.afit;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AllStatsSmallAdapter extends RecyclerView.Adapter<AllStatsSmallAdapter.MyViewHolder> {
    Context context;
    ArrayList<MyObject> data;
    AllStatsClick clickInterface;
    int allDataSize;

    public AllStatsSmallAdapter(
            Context context,
            ArrayList<MyObject> data,
            int allDataSize,
            AllStatsClick clickInterface
    ) {
        this.context = context;
        this.data = data;
        this.allDataSize = allDataSize;
        this.clickInterface = clickInterface;
    }

    @NonNull
    @Override
    public AllStatsSmallAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AllStatsSmallAdapter.MyViewHolder(
                LayoutInflater.from(context).inflate(R.layout.row_stats_small, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull AllStatsSmallAdapter.MyViewHolder holder, int pos) {
        MyObject obj = data.get(pos);

        // Check box
        holder.itemCheck.setChecked(false);
        holder.itemCheck.setButtonTintList(ColorStateList.valueOf(obj.color));
        if (obj.entriesQuantity != -1) holder.itemCheck.setChecked(true);
        holder.itemCheck.setOnCheckedChangeListener(
                (buttonView, isChecked) -> clickInterface.onClickCheckBox(obj.id, (obj.sets != 0)? 1: 0, !isChecked)
        );

        // Texts
        holder.itemTextName.setText(obj.name);
        if (holder.itemCheck.isChecked()) {
            holder.itemTextAdditional.setVisibility(View.VISIBLE);
            String text = obj.entriesQuantity + " (" + (int) ((obj.entriesQuantity / (float) allDataSize) * 100) + "%)";
            holder.itemTextAdditional.setText(text);
        } else {
            holder.itemTextAdditional.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        CheckBox itemCheck;
        TextView itemTextName, itemTextAdditional;
        public MyViewHolder(@NonNull View view) {
            super(view);
            itemCheck = view.findViewById(R.id.itemCheck);
            itemTextName = view.findViewById(R.id.itemTextName);
            itemTextAdditional = view.findViewById(R.id.itemTextAdditional);
        }
    }
}
