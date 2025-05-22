package com.kiddo.remotescreen.adapter.connect;

import android.graphics.Color;
import android.util.Log;
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
    private final boolean isCooldown;

    public PcHistoryAdapter(List<PcHistoryItem> pcList, OnConnectClickListener listener, boolean isCooldown) {
        this.pcList = pcList;
        this.listener = listener;
        this.isCooldown = isCooldown;
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

        Log.d("PcHistoryAdapter", "Bind item: ID=" + item.getId() + ", Name=" + item.getName() + ", Status=" + item.getStatus());

        holder.textPcName.setText(item.getName());

        String statusText;
        int statusColor;

        switch (item.getStatus()) {
            case "enable":
                statusText = "Available";
                statusColor = R.color.green;
                holder.buttonConnect.setEnabled(!isCooldown);
                break;
            case "busy":
                statusText = "Busy";
                statusColor = R.color.red;
                holder.buttonConnect.setEnabled(false);
                break;
            case "disable":
            default:
                statusText = "Remote disabled";
                statusColor = R.color.red;
                holder.buttonConnect.setEnabled(false);
                break;
        }

        holder.textStatus.setText("Status: " + statusText);
        holder.textStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), statusColor));

        holder.buttonConnect.setText(R.string.connect);
        holder.buttonConnect.setBackgroundTintList(ContextCompat.getColorStateList(
                holder.itemView.getContext(), holder.buttonConnect.isEnabled() ? R.color.colorSecondary : R.color.gray
        ));
        holder.buttonConnect.setTextColor(holder.buttonConnect.isEnabled() ? Color.WHITE : Color.DKGRAY);

        holder.buttonConnect.setOnClickListener(v -> {
            if (holder.buttonConnect.isEnabled()) {
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
}
