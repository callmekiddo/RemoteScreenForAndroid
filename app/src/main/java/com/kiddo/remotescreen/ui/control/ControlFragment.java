package com.kiddo.remotescreen.ui.control;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.kiddo.remotescreen.R;

public class ControlFragment extends Fragment {

    public ControlFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_control, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        LinearLayout connectionLayout = view.findViewById(R.id.connectionInfoLayout);
        TextView textPcName = view.findViewById(R.id.textPcName);
        TextView buttonDisconnect = view.findViewById(R.id.buttonDisconnect);
        MaterialButton buttonRemote = view.findViewById(R.id.buttonRemote);
        View divider = view.findViewById(R.id.viewDivider);

        boolean isConnected = true; // Cập nhật từ dữ liệu thật nếu cần
        String connectedPc = "DESKTOP-1";

        if (isConnected) {
            connectionLayout.setVisibility(View.VISIBLE);
            buttonRemote.setVisibility(View.VISIBLE);
            textPcName.setText(connectedPc);
        } else {
            connectionLayout.setVisibility(View.GONE);
            buttonRemote.setVisibility(View.GONE);
        }

        buttonDisconnect.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Disconnected", Toast.LENGTH_SHORT).show();
            connectionLayout.setVisibility(View.GONE);
            divider.setVisibility(View.GONE);
            buttonRemote.setEnabled(false);
            buttonRemote.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.gray));
            buttonRemote.setTextColor(Color.DKGRAY);
        });

        buttonRemote.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Starting Remote Control...", Toast.LENGTH_SHORT).show();
            // TODO: Điều hướng sang RemoteControlFragment (màn 3)
        });
    }


}
