package com.kiddo.remotescreen.ui.control;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.kiddo.remotescreen.R;
import com.kiddo.remotescreen.model.DeviceStatus;
import com.kiddo.remotescreen.repository.DeviceRepository;
import com.kiddo.remotescreen.ui.control.remote.RemoteActivity;
import com.kiddo.remotescreen.util.ConnectCooldownManager;
import com.kiddo.remotescreen.util.SessionManager;
import com.kiddo.remotescreen.util.signaling.SignalingClient;
import com.kiddo.remotescreen.util.webrtc.WebRtcManager;

public class ControlFragment extends Fragment {

    private Handler statusHandler;
    private Runnable statusRunnable;
    private static final long STATUS_INTERVAL_MS = 1000;

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
        Button buttonRemote = view.findViewById(R.id.buttonRemote);
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
//            if (isAdded()) {
//                buttonRemote.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.colorPrimary));
//                buttonRemote.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorOnPrimary));
//            }

            startAutoStatusCheck(pcId, connectionLayout, divider, buttonRemote);
        } else {
            connectionLayout.setVisibility(View.GONE);
            divider.setVisibility(View.GONE);
            buttonRemote.setVisibility(View.VISIBLE);
            buttonRemote.setEnabled(false);
            buttonRemote.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.gray));
            buttonRemote.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorOnBackground));
        }

        buttonDisconnect.setOnClickListener(v -> {
            if (!isConnected) {
                Toast.makeText(getContext(), "No device to disconnect", Toast.LENGTH_SHORT).show();
                return;
            }
            disconnectFromPc(pcId, connectionLayout, divider, buttonRemote);
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

    private void startAutoStatusCheck(String pcId, View connectionLayout, View divider, Button buttonRemote) {
        stopAutoStatusCheck();

        statusHandler = new Handler();
        statusRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isAdded()) return;

                DeviceRepository.getDeviceStatus(pcId, new DeviceRepository.DeviceStatusCallback() {
                    @Override
                    public void onSuccess(DeviceStatus status) {
                        if (!status.getAllowRemote()) {
                            autoDisconnect(pcId, connectionLayout, divider, buttonRemote);
                            stopAutoStatusCheck();
                        } else {
                            // ✅ Sử dụng lại statusRunnable đúng cách
                            statusHandler.postDelayed(statusRunnable, STATUS_INTERVAL_MS);
                        }
                    }

                    @Override
                    public void onError(String error) {
                        // Retry dù có lỗi
                        statusHandler.postDelayed(statusRunnable, STATUS_INTERVAL_MS);
                    }
                });
            }
        };

        // ✅ Khởi động lần đầu
        statusHandler.post(statusRunnable);
    }


    private void stopAutoStatusCheck() {
        if (statusHandler != null && statusRunnable != null) {
            statusHandler.removeCallbacks(statusRunnable);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopAutoStatusCheck();
    }

    private void autoDisconnect(String pcId, View connectionLayout, View divider, Button buttonRemote) {
        DeviceRepository.disconnectAndroid(pcId, new DeviceRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    if (!isAdded()) return;

                    Toast.makeText(getContext(), "Remote access disabled. Disconnected automatically.", Toast.LENGTH_SHORT).show();

                    SessionManager.clearConnectedPc();
                    SignalingClient.getInstance().close();
                    WebRtcManager.getInstance().disconnect();

                    connectionLayout.setVisibility(View.GONE);
                    divider.setVisibility(View.GONE);
                    buttonRemote.setEnabled(false);
                    buttonRemote.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.gray));
                    buttonRemote.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorOnBackground));

                    ConnectCooldownManager.setCooldown();
                });
            }

            @Override
            public void onError(String error) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Auto-disconnect failed: " + error, Toast.LENGTH_LONG).show()
                );
            }
        });
    }

    private void disconnectFromPc(String pcId, View connectionLayout, View divider, Button buttonRemote) {
        DeviceRepository.disconnectAndroid(pcId, new DeviceRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    if (!isAdded()) return;

                    Toast.makeText(getContext(), "Disconnected from PC", Toast.LENGTH_SHORT).show();

                    SessionManager.clearConnectedPc();
                    SignalingClient.getInstance().close();
                    WebRtcManager.getInstance().disconnect();

                    connectionLayout.setVisibility(View.GONE);
                    divider.setVisibility(View.GONE);
                    buttonRemote.setEnabled(false);
                    buttonRemote.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.gray));
                    buttonRemote.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorOnBackground));

                    ConnectCooldownManager.setCooldown();
                });
            }

            @Override
            public void onError(String error) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Failed to disconnect: " + error, Toast.LENGTH_LONG).show()
                );
            }
        });
    }
}