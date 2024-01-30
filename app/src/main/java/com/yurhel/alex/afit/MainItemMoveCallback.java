package com.yurhel.alex.afit;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class MainItemMoveCallback extends ItemTouchHelper.Callback {
    private final ItemTouchHelperContract mAdapter;
    private int from = -1;
    private int to = -1;

    public MainItemMoveCallback(ItemTouchHelperContract adapter) {
        mAdapter = adapter;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {}

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        return makeMovementFlags(dragFlags, 0);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        from = viewHolder.getAdapterPosition();
        to = target.getAdapterPosition();
        return false;
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
            if (viewHolder instanceof MainAdapter.MyViewHolder) {
                MainAdapter.MyViewHolder myViewHolder = (MainAdapter.MyViewHolder) viewHolder;
                mAdapter.onRowSelected(myViewHolder);
            }
        } else {
            mAdapter.onRowMoved(from, to);
        }
        super.onSelectedChanged(viewHolder, actionState);
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);

        if (viewHolder instanceof MainAdapter.MyViewHolder) {
            MainAdapter.MyViewHolder myViewHolder = (MainAdapter.MyViewHolder) viewHolder;
            mAdapter.onRowClear(myViewHolder);
        }
    }

    public interface ItemTouchHelperContract {
        void onRowMoved(int fromPosition, int toPosition);
        void onRowSelected(MainAdapter.MyViewHolder myViewHolder);
        void onRowClear(MainAdapter.MyViewHolder myViewHolder);
    }
}
