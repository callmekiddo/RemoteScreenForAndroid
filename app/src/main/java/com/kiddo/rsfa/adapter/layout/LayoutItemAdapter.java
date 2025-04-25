package com.kiddo.rsfa.adapter.layout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kiddo.rsfa.R;
import com.kiddo.rsfa.model.LayoutItem;

import java.util.List;

public class LayoutItemAdapter extends RecyclerView.Adapter<LayoutItemAdapter.LayoutViewHolder> {

    public interface OnLayoutClickListener {
        void onLayoutClick(LayoutItem item);
    }

    private final List<LayoutItem> layoutList;
    private final OnLayoutClickListener listener;

    public LayoutItemAdapter(List<LayoutItem> layoutList, OnLayoutClickListener listener) {
        this.layoutList = layoutList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LayoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_layout_grid, parent, false);
        return new LayoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LayoutViewHolder holder, int position) {
        LayoutItem item = layoutList.get(position);
        holder.name.setText(item.getName());
        holder.icon.setImageResource(item.getIconRes());
        holder.itemView.setOnClickListener(v -> listener.onLayoutClick(item));
    }

    @Override
    public int getItemCount() {
        return layoutList.size();
    }

    public static class LayoutViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView name;

        public LayoutViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.layoutIcon);
            name = itemView.findViewById(R.id.layoutName);
        }
    }
}


