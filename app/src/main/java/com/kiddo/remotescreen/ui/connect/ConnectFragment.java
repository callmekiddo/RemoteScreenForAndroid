package com.kiddo.remotescreen.ui.connect;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.kiddo.remotescreen.R;
import com.kiddo.remotescreen.adapter.connect.PcHistoryAdapter;
import com.kiddo.remotescreen.model.DeviceStatus;
import com.kiddo.remotescreen.model.PcHistoryItem;
import com.kiddo.remotescreen.repository.DeviceRepository;
import com.kiddo.remotescreen.ui.connect.dialog.PasswordDialog;
import com.kiddo.remotescreen.ui.control.ControlFragment;
import com.kiddo.remotescreen.util.ConnectCooldownManager;
import com.kiddo.remotescreen.util.SessionManager;
import com.kiddo.remotescreen.util.signaling.SignalingClient;
import com.kiddo.remotescreen.util.signaling.SignalingObserver;
import com.kiddo.remotescreen.util.webrtc.WebRtcManager;

import org.json.JSONObject;

import java.util.List;

public class ConnectFragment extends Fragment {

    private MaterialAutoCompleteTextView autoCompletePcId;
    private View buttonConnect;
    private RecyclerView recyclerViewHistory;
    private static final String SIGNALING_URL_BASE = "ws://vanjustkiddo.click:8080/auth/signaling/";

    private Handler statusRefreshHandler;
    private Runnable statusRefreshRunnable;
    private static final long REFRESH_INTERVAL_MS = 1000;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_connect, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        autoCompletePcId = view.findViewById(R.id.autoCompletePcId);
        buttonConnect = view.findViewById(R.id.buttonConnect);
        recyclerViewHistory = view.findViewById(R.id.recyclerViewHistory);

        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(requireContext()));

        setupPcHistory();
        applyCooldownIfNeeded();

        buttonConnect.setOnClickListener(v -> {
            if (ConnectCooldownManager.isCooldownActive()) {
                Toast.makeText(requireContext(), "Please wait before trying again", Toast.LENGTH_SHORT).show();
                return;
            }

            String pcId = autoCompletePcId.getText().toString().trim();
            if (pcId.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter PC ID", Toast.LENGTH_SHORT).show();
                return;
            }

            showPasswordDialogForPcId(pcId);
        });
    }

    private void showPasswordDialogForPcId(String pcId) {
        DeviceRepository.getDeviceStatus(pcId, new DeviceRepository.DeviceStatusCallback() {
            @Override
            public void onSuccess(DeviceStatus status) {
                requireActivity().runOnUiThread(() -> {
                    if (!Boolean.TRUE.equals(status.getAllowRemote())) {
                        Toast.makeText(requireContext(), "Remote access is disabled on PC", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String connected = status.getConnectedAndroid();
                    if (connected != null && !connected.equalsIgnoreCase("null") && !connected.isEmpty()) {
                        String currentDevice = android.os.Build.MODEL;
                        if (!currentDevice.equals(connected)) {
                            Toast.makeText(requireContext(),
                                    "This PC is currently connected to another Android device: " + connected,
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                    }

                    PasswordDialog dialog = PasswordDialog.newInstance(pcId, (confirmedPcId, password) -> connectToPc(confirmedPcId, password));
                    dialog.show(getParentFragmentManager(), "PasswordDialog");
                });
            }

            @Override
            public void onError(String error) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Can't find PC", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void connectToPc(String pcId, String password) {
        String androidName = android.os.Build.MODEL;

        DeviceRepository.connectToPc(pcId, password, androidName, new DeviceRepository.Callback() {
            @Override
            public void onSuccess(String deviceId, String deviceName) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Connected!", Toast.LENGTH_SHORT).show();

                    SessionManager.setConnectedPcId(deviceId);
                    SessionManager.setConnectedPcName(deviceName);

                    String status = "enable";
                    PcHistoryItem historyItem = new PcHistoryItem(deviceId, deviceName, status);
                    PcHistoryStorage.add(requireContext(), historyItem);
                    setupPcHistory();

                    WebRtcManager webrtc = WebRtcManager.getInstance();
                    webrtc.init(requireContext());

                    String signalingUrl = SIGNALING_URL_BASE + androidName;
                    SignalingClient signalingClient = SignalingClient.getInstance(new SignalingObserver() {
                        @Override
                        public void onConnected() {
                            Log.d("ConnectFragment", "✅ Signaling connected");
                        }

                        @Override
                        public void onDisconnected() {
                            Log.d("ConnectFragment", "🔌 Signaling disconnected");
                        }

                        @Override
                        public void onOfferReceived(String from, String sdp) {}

                        @Override
                        public void onAnswerReceived(String from, String sdp) {}

                        @Override
                        public void onIceCandidateReceived(String from, JSONObject json) {}
                    });

                    if (!signalingClient.isConnected()) {
                        signalingClient.connect(signalingUrl);
                    }

                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, new ControlFragment())
                            .addToBackStack(null)
                            .commit();

                    BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottom_nav);
                    bottomNav.setSelectedItemId(R.id.nav_control);
                });
            }

            @Override
            public void onError(String error) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Connection failed: " + error, Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void setupPcHistory() {
        List<PcHistoryItem> historyList = PcHistoryStorage.load(requireContext());
        boolean cooldown = ConnectCooldownManager.isCooldownActive();
        PcHistoryAdapter adapter = new PcHistoryAdapter(historyList, item -> {
            autoCompletePcId.setText(item.getId());
            showPasswordDialogForPcId(item.getId());
        }, cooldown);
        recyclerViewHistory.setAdapter(adapter);
        refreshPcHistoryStatus(historyList, adapter);
        startAutoRefreshStatus(historyList, adapter);
    }

    private void refreshPcHistoryStatus(List<PcHistoryItem> historyList, PcHistoryAdapter adapter) {
        for (PcHistoryItem item : historyList) {
            DeviceRepository.getDeviceStatus(item.getId(), new DeviceRepository.DeviceStatusCallback() {
                @Override
                public void onSuccess(DeviceStatus status) {
                    String currentDevice = android.os.Build.MODEL;
                    String connected = status.getConnectedAndroid();
                    String resultStatus;

                    if (!status.getAllowRemote()) {
                        resultStatus = "disable";
                    } else if (connected == null || connected.isEmpty()) {
                        resultStatus = "enable";
                    } else {
                        resultStatus = "busy";
                    }

                    Log.d("PcHistoryStatus", "PC: " + item.getName() + ", allowRemote=" + status.getAllowRemote() +
                            ", connectedAndroid=" + connected + ", device=" + currentDevice +
                            " → status=" + resultStatus);

                    item.setStatus(resultStatus);
                    requireActivity().runOnUiThread(adapter::notifyDataSetChanged);
                }

                @Override
                public void onError(String error) {
                    Log.e("PcHistoryStatus", "Failed to fetch status for " + item.getId() + ": " + error);
                }
            });
        }
    }

    private void applyCooldownIfNeeded() {
        if (ConnectCooldownManager.isCooldownActive()) {
            long remaining = ConnectCooldownManager.getRemainingCooldownMs();
            buttonConnect.setEnabled(false);
            buttonConnect.setAlpha(0.4f);

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                buttonConnect.setEnabled(true);
                buttonConnect.setAlpha(1.0f);
                setupPcHistory();
            }, remaining);
        }
    }

    private void startAutoRefreshStatus(List<PcHistoryItem> historyList, PcHistoryAdapter adapter) {
        stopAutoRefreshStatus();
        statusRefreshHandler = new Handler(Looper.getMainLooper());
        statusRefreshRunnable = new Runnable() {
            @Override
            public void run() {
                refreshPcHistoryStatus(historyList, adapter);
                statusRefreshHandler.postDelayed(this, REFRESH_INTERVAL_MS);
            }
        };
        statusRefreshHandler.post(statusRefreshRunnable);
    }

    private void stopAutoRefreshStatus() {
        if (statusRefreshHandler != null && statusRefreshRunnable != null) {
            statusRefreshHandler.removeCallbacks(statusRefreshRunnable);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        applyCooldownIfNeeded();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopAutoRefreshStatus();
    }
}
