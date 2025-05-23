package com.kiddo.remotescreen.adapter.layout;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kiddo.remotescreen.R;
import com.kiddo.remotescreen.model.LayoutItem;

import java.io.File;
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

        String path = item.getIconPath();
        if ("layout.png".equals(item.getIconPath())) {
            holder.icon.setImageResource(R.drawable.layout);

            int background = holder.itemView.getContext()
                    .getResources().getConfiguration().uiMode
                    & android.content.res.Configuration.UI_MODE_NIGHT_MASK;

            if (background == android.content.res.Configuration.UI_MODE_NIGHT_YES) {
                holder.icon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
            } else {
                holder.icon.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
            }
        } else {
            File iconFile = new File(holder.itemView.getContext().getFilesDir(), "icons/" + path);
            if (iconFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(iconFile.getAbsolutePath());
                holder.icon.clearColorFilter();
                holder.icon.setImageBitmap(bitmap);
            } else {
                holder.icon.setImageResource(R.drawable.layout);
                holder.icon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
            }
        }

        // Optional: set click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onLayoutClick(item);
        });
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


