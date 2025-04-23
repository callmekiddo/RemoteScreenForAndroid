package com.kiddo.rsfa.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kiddo.rsfa.R;
import com.kiddo.rsfa.model.PcHistoryItem;

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
        holder.textStatus.setText(item.isOnline() ? "Status: Online" : "Status: Offline");
        holder.buttonConnect.setEnabled(item.isOnline());
        holder.buttonConnect.setOnClickListener(v -> listener.onConnectClick(item));
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
