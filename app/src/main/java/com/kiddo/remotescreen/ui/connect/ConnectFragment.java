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

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.kiddo.remotescreen.R;
import com.kiddo.remotescreen.model.DeviceStatus;
import com.kiddo.remotescreen.repository.DeviceRepository;
import com.kiddo.remotescreen.ui.connect.dialog.PasswordDialog;
import com.kiddo.remotescreen.ui.control.ControlFragment;
import com.kiddo.remotescreen.util.ConnectCooldownManager;
import com.kiddo.remotescreen.util.SessionManager;
import com.kiddo.remotescreen.util.signaling.SignalingClient;
import com.kiddo.remotescreen.util.signaling.SignalingObserver;
import com.kiddo.remotescreen.util.webrtc.WebRtcManager;

import org.json.JSONObject;

public class ConnectFragment extends Fragment {

    private MaterialAutoCompleteTextView autoCompletePcId;
    private View buttonConnect;
    private static final String SIGNALING_URL_BASE = "ws://vanjustkiddo.click:8080/auth/signaling/";

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

            DeviceRepository.getDeviceStatus(pcId, new DeviceRepository.DeviceStatusCallback() {
                @Override
                public void onSuccess(DeviceStatus status) {
                    if (!Boolean.TRUE.equals(status.getAllowRemote())) {
                        Toast.makeText(requireContext(), "Remote access is disabled on PC", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    PasswordDialog dialog = PasswordDialog.newInstance(pcId, new PasswordDialog.PasswordDialogListener() {
                        @Override
                        public void onPasswordConfirmed(String pcId, String password) {
                            String androidName = android.os.Build.MODEL;

                            DeviceRepository.connectToPc(pcId, password, androidName, new DeviceRepository.Callback() {
                                @Override
                                public void onSuccess(String deviceId, String deviceName) {
                                    requireActivity().runOnUiThread(() -> {
                                        Toast.makeText(requireContext(), "Connected!", Toast.LENGTH_SHORT).show();

                                        SessionManager.setConnectedPcId(deviceId);
                                        SessionManager.setConnectedPcName(deviceName);

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
                    });

                    dialog.show(getParentFragmentManager(), "PasswordDialog");
                }

                @Override
                public void onError(String error) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Can't find PC", Toast.LENGTH_SHORT).show()
                    );
                }
            });
        });
    }

    private void applyCooldownIfNeeded() {
        if (ConnectCooldownManager.isCooldownActive()) {
            long remaining = ConnectCooldownManager.getRemainingCooldownMs();
            buttonConnect.setEnabled(false);
            buttonConnect.setAlpha(0.4f);

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                buttonConnect.setEnabled(true);
                buttonConnect.setAlpha(1.0f);
            }, remaining);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        applyCooldownIfNeeded();
    }
}
