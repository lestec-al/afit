package com.yurhel.alex.afit;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.MyViewHolder> {
    private final ClickInterface clickInterface;
    Context context;
    List<MyObject> data;
    boolean exercise;
    int color;
    LinkedHashMap<String, Integer> colors;
    int blackColor;
    int whiteColor;


    public MainAdapter(Context context, List<MyObject> data, boolean exercise, ClickInterface clickInterface, int color, LinkedHashMap<String, Integer> colors) {
        this.context = context;
        this.data = data;
        this.exercise = exercise;
        this.clickInterface = clickInterface;
        this.color = color;
        this.colors = colors;
        this.blackColor = context.getColor(R.color.dark);
        this.whiteColor = context.getColor(R.color.white);
    }

    @NonNull
    @Override
    public MainAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        return new MainAdapter.MyViewHolder(inflater.inflate(R.layout.row_main, parent, false), clickInterface, exercise);
    }

    @Override
    public void onBindViewHolder(@NonNull MainAdapter.MyViewHolder holder, int pos) {
        MyObject obj = data.get(pos);
        Integer objColor = colors.get((exercise) ? obj.id+"_ex": obj.id+"_st");
        int backgroundColor = (objColor != null && objColor != 0) ? objColor: color;
        holder.name_icon.setBackgroundColor(backgroundColor);
        holder.name_icon.setText(obj.name);
        Drawable d;
        if (exercise)
            d = Objects.requireNonNull(AppCompatResources.getDrawable(context, R.drawable.ic_rv_exercise));
        else
            d = Objects.requireNonNull(AppCompatResources.getDrawable(context, R.drawable.ic_rv_stats));
        if (backgroundColor == whiteColor) {
            holder.name_icon.setTextColor(blackColor);
            d.setTint(blackColor);
        } else {
            d.setTint(whiteColor);
        }
        holder.name_icon.setCompoundDrawablesWithIntrinsicBounds(null, null, d, null);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        Button name_icon;

        public MyViewHolder(@NonNull View view, ClickInterface clickInterface, boolean exercise_adapter) {
            super(view);
            name_icon = view.findViewById(R.id.rv_main_name_icon);
            name_icon.setOnClickListener(view1 -> {
                if (clickInterface != null) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        if (exercise_adapter)
                            clickInterface.onClickItem(pos, "ex");
                        else
                            clickInterface.onClickItem(pos, "st");
                    }
                }
            });
        }
    }
}
