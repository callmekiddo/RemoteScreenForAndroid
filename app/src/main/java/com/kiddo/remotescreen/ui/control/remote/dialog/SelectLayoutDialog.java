package com.kiddo.remotescreen.ui.control.remote.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kiddo.remotescreen.R;
import com.kiddo.remotescreen.model.LayoutInfo;

import java.util.List;
import java.util.function.Consumer;

public class SelectLayoutDialog extends DialogFragment {

    private final List<LayoutInfo> layoutList;
    private final Consumer<LayoutInfo> onSelected;

    public SelectLayoutDialog(List<LayoutInfo> layoutList, Consumer<LayoutInfo> onSelected) {
        this.layoutList = layoutList;
        this.onSelected = onSelected;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            Window window = dialog.getWindow();
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawableResource(R.drawable.background_dialog);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_select_layout, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerLayoutChoices);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        recyclerView.setAdapter(new LayoutAdapter());
        return view;
    }

    private class LayoutAdapter extends RecyclerView.Adapter<LayoutAdapter.ViewHolder> {

        class ViewHolder extends RecyclerView.ViewHolder {
            final ImageView icon;
            final TextView name;

            ViewHolder(View view) {
                super(view);
                icon = view.findViewById(R.id.layoutIcon);
                name = view.findViewById(R.id.layoutName);
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_layout_grid, parent, false);
            return new ViewHolder(v);
        }

        @SuppressLint("NewApi")
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            LayoutInfo layout = layoutList.get(position);
            holder.name.setText(layout.getName());

            Bitmap bitmap = layout.getIconBitmap(holder.itemView.getContext());
            if (bitmap != null) {
                holder.icon.setImageBitmap(bitmap);
            } else {
                holder.icon.setImageResource(R.drawable.layout);
            }

            holder.itemView.setOnClickListener(v -> {
                if (onSelected != null) onSelected.accept(layout);
                dismiss();
            });
        }

        @Override
        public int getItemCount() {
            return layoutList.size();
        }
    }
}
