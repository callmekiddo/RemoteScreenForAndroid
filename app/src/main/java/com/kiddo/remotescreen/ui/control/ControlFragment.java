package com.kiddo.remotescreen.ui.control;

import android.content.Intent;
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
import com.kiddo.remotescreen.repository.DeviceRepository;
import com.kiddo.remotescreen.ui.control.remote.RemoteActivity;
import com.kiddo.remotescreen.util.ConnectCooldownManager;
import com.kiddo.remotescreen.util.SessionManager;
import com.kiddo.remotescreen.util.signaling.SignalingClient;
import com.kiddo.remotescreen.util.webrtc.WebRtcManager;

public class ControlFragment extends Fragment {

    public ControlFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_control, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        LinearLayout connectionLayout = view.findViewById(R.id.connectionInfoLayout);
        TextView textPcName = view.findViewById(R.id.textPcName);
        TextView buttonDisconnect = view.findViewById(R.id.buttonDisconnect);
        MaterialButton buttonRemote = view.findViewById(R.id.buttonRemote);
        View divider = view.findViewById(R.id.viewDivider);

        String pcName = SessionManager.getConnectedPcName();
        String pcId = SessionManager.getConnectedPcId();
        boolean isConnected = pcId != null && !pcId.isEmpty();

        if (isConnected) {
            connectionLayout.setVisibility(View.VISIBLE);
            divider.setVisibility(View.VISIBLE);
            buttonRemote.setVisibility(View.VISIBLE);
            buttonRemote.setEnabled(true);
            textPcName.setText(pcName != null && !pcName.isEmpty() ? pcName : pcId);
            buttonRemote.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.colorPrimary));
            buttonRemote.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorOnPrimary));
        } else {
            connectionLayout.setVisibility(View.GONE);
            divider.setVisibility(View.GONE);
            buttonRemote.setVisibility(View.VISIBLE);
            buttonRemote.setEnabled(false);
            buttonRemote.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.gray));
            buttonRemote.setTextColor(Color.DKGRAY);
        }

        buttonDisconnect.setOnClickListener(v -> {
            if (!isConnected) {
                Toast.makeText(getContext(), "No device to disconnect", Toast.LENGTH_SHORT).show();
                return;
            }

            DeviceRepository.disconnectAndroid(pcId, new DeviceRepository.SimpleCallback() {
                @Override
                public void onSuccess() {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Disconnected from PC", Toast.LENGTH_SHORT).show();

                        // ❌ Xoá session
                        SessionManager.clearConnectedPc();

                        // ❌ Ngắt signaling + WebRTC
                        SignalingClient.getInstance().close();
                        WebRtcManager.getInstance().disconnect();

                        // ❌ Cập nhật giao diện
                        connectionLayout.setVisibility(View.GONE);
                        divider.setVisibility(View.GONE);
                        buttonRemote.setEnabled(false);
                        buttonRemote.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.gray));
                        buttonRemote.setTextColor(Color.DKGRAY);

                        // ✅ Thiết lập cooldown
                        ConnectCooldownManager.setCooldown();
                    });
                }

                @Override
                public void onError(String error) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Failed to disconnect: " + error, Toast.LENGTH_LONG).show()
                    );
                }
            });
        });

        buttonRemote.setOnClickListener(v -> {
            if (!isConnected) {
                Toast.makeText(getContext(), "Not connected to PC", Toast.LENGTH_SHORT).show();
                return;
            }

            String androidName = android.os.Build.MODEL;
            WebRtcManager webrtc = WebRtcManager.getInstance();
            SignalingClient signaling = SignalingClient.getInstance();

            if (!webrtc.isPeerConnected() && !webrtc.hasStartedFlow()) {
                webrtc.connectPeer(pcId, signaling);
                webrtc.startRemoteFlow(androidName);
            }

            Intent intent = new Intent(requireContext(), RemoteActivity.class);
            startActivity(intent);
        });
    }
}
