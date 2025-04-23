package com.kiddo.rsfa.ui.control;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.kiddo.rsfa.R;

public class ControlFragment extends Fragment {

    private boolean isConnected = true; // giả sử đã kết nối – bạn thay bằng dữ liệu thật
    private String connectedPcName = "DESKTOP-KIDDO"; // dữ liệu mẫu

    public ControlFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_control, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LinearLayout connectionLayout = view.findViewById(R.id.connectionStatusLayout);
        TextView textPcName = view.findViewById(R.id.textPcName);
        TextView textDisconnect = view.findViewById(R.id.textDisconnect);

        boolean isConnected = true; // thay bằng logic thật
        String connectedName = "KIDDO";

        if (isConnected) {
            connectionLayout.setVisibility(View.VISIBLE);
            textPcName.setText(connectedName);
        } else {
            connectionLayout.setVisibility(View.GONE);
        }

        textDisconnect.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Disconnected", Toast.LENGTH_SHORT).show();
            // TODO: gọi hàm ngắt kết nối thật
            connectionLayout.setVisibility(View.GONE);
        });
    }

}
