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

import java.util.List;
import java.util.Objects;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.MyViewHolder> implements MainItemMoveCallback.ItemTouchHelperContract {
    private final ClickInterface clickInterface;
    MainCallback callBack;
    Context context;
    List<MyObject> data;
    int blackColor;
    int whiteColor;

    public MainAdapter(Context context, List<MyObject> data, ClickInterface clickInterface, MainCallback callBack) {
        this.context = context;
        this.data = data;
        this.clickInterface = clickInterface;
        this.blackColor = context.getColor(R.color.dark);
        this.whiteColor = context.getColor(R.color.white);
        this.callBack = callBack;
    }

    @NonNull
    @Override
    public MainAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MainAdapter.MyViewHolder(LayoutInflater.from(context).inflate(R.layout.row_main, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MainAdapter.MyViewHolder holder, int pos) {
        MyObject obj = data.get(pos);
        holder.name_icon.setBackgroundColor(obj.color);
        holder.name_icon.setText(obj.name);
        Drawable d = Objects.requireNonNull(AppCompatResources.getDrawable(
                context, (obj.sets != 0/*Is exercise*/) ? R.drawable.ic_rv_exercise: R.drawable.ic_rv_stats
        ));
        if (obj.color == whiteColor) {
            holder.name_icon.setTextColor(blackColor);
            d.setTint(blackColor);
        } else {
            holder.name_icon.setShadowLayer(14,1,1,blackColor);
        }
        holder.name_icon.setCompoundDrawablesWithIntrinsicBounds(null, null, d, null);
        holder.name_icon.setOnClickListener(view1 -> clickInterface.onClickItem(pos, (obj.sets != 0/*Is exercise*/) ? "ex_id": "st_id"));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }


    // Drag & drop change position
    @Override
    public void onRowMoved(int fromPosition, int toPosition) {
        if (fromPosition != toPosition) {
            data.add(toPosition, data.remove(fromPosition));
            callBack.onItemMoved();
        }
    }

    @Override
    public void onRowSelected(MyViewHolder holder) {}

    @Override
    public void onRowClear(MyViewHolder holder) {}


    public static class MyViewHolder extends RecyclerView.ViewHolder {
        Button name_icon;

        public MyViewHolder(@NonNull View view) {
            super(view);
            name_icon = view.findViewById(R.id.mainButton);
        }
    }
}
