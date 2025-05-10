package com.kiddo.remotescreen.adapter.connect;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.kiddo.remotescreen.R;
import com.kiddo.remotescreen.model.PcHistoryItem;

import java.util.List;

public class PcHistoryAdapter extends RecyclerView.Adapter<PcHistoryAdapter.PcViewHolder> {

    public interface OnConnectClickListener {
        void onConnectClick(PcHistoryItem item);
    }

    private final List<PcHistoryItem> pcList;
    private final OnConnectClickListener listener;

    public PcHistoryAdapter(List<PcHistoryItem> pcList, OnConnectClickListener listener) {
        this.pcList = pcList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PcViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pc_history, parent, false);
        return new PcViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PcViewHolder holder, int position) {
        PcHistoryItem item = pcList.get(position);

        holder.textPcName.setText(item.getName());
        String statusText = itemViewContext(holder).getString(
                R.string.status_format,
                itemViewContext(holder).getString(item.isOnline() ? R.string.status_online : R.string.status_offline)
        );
        holder.textStatus.setText(statusText);

        holder.buttonConnect.setText(R.string.connect);
        if (item.isOnline()) {
            holder.buttonConnect.setEnabled(true);
            holder.buttonConnect.setBackgroundTintList(ContextCompat.getColorStateList(
                    holder.itemView.getContext(), R.color.colorSecondary
            ));
            holder.buttonConnect.setTextColor(Color.WHITE);
        } else {
            holder.buttonConnect.setEnabled(false);
            holder.buttonConnect.setBackgroundTintList(ContextCompat.getColorStateList(
                    holder.itemView.getContext(), R.color.gray
            ));
            holder.buttonConnect.setTextColor(Color.DKGRAY);
        }

        holder.buttonConnect.setOnClickListener(v -> {
            if (item.isOnline()) {
                listener.onConnectClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return pcList.size();
    }

    static class PcViewHolder extends RecyclerView.ViewHolder {
        TextView textPcName, textStatus;
        Button buttonConnect;

        PcViewHolder(@NonNull View itemView) {
            super(itemView);
            textPcName = itemView.findViewById(R.id.textPcName);
            textStatus = itemView.findViewById(R.id.textPcStatus);
            buttonConnect = itemView.findViewById(R.id.buttonConnect);
        }
    }

    private static android.content.Context itemViewContext(PcViewHolder holder) {
        return holder.itemView.getContext();
    }
}
