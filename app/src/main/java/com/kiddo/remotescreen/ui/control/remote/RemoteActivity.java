package com.kiddo.remotescreen.ui.control.remote;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.kiddo.remotescreen.R;
import com.kiddo.remotescreen.ui.control.remote.keyboard.KeyboardInputHandler;
import com.kiddo.remotescreen.ui.control.remote.mouse.MouseInputHandler;
import com.kiddo.remotescreen.util.SessionManager;
import com.kiddo.remotescreen.util.signaling.SignalingClient;
import com.kiddo.remotescreen.util.signaling.SignalingObserver;
import com.kiddo.remotescreen.util.webrtc.WebRtcManager;

import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.RendererCommon;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoTrack;

public class RemoteActivity extends AppCompatActivity {
    private static final String TAG = "RemoteActivity";

    private SurfaceViewRenderer remoteView;
    private WebRtcManager webRtcManager;

    private ImageView handlePanel;
    private LinearLayout controlPanel;
    private ImageView btnKeyboard;
    private EditText hiddenInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote);
        forceLandscapeFullscreen();

        remoteView = findViewById(R.id.remote_view);
        remoteView.setZOrderMediaOverlay(true);
        remoteView.setEnableHardwareScaler(true);
        remoteView.setMirror(false);
        remoteView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);

        webRtcManager = WebRtcManager.getInstance();

        if (!webRtcManager.isInitialized() || webRtcManager.getPeerConnection() == null) {
            Log.e(TAG, "WebRtcManager chưa được khởi tạo hoặc chưa có peerConnection");
            finish();
            return;
        }

        remoteView.init(webRtcManager.getEglBaseContext(), null);
        webRtcManager.setRemoteRenderer(remoteView);

        SignalingClient signaling = SignalingClient.getInstance();
        signaling.setObserver(new SignalingObserver() {
            @Override
            public void onOfferReceived(String from, String sdp) {
                Log.d(TAG, "Received OFFER from: " + from);
                SessionDescription offer = new SessionDescription(SessionDescription.Type.OFFER, sdp);
                webRtcManager.setRemoteSdp(offer, () -> webRtcManager.createAndSendAnswer());
            }

            @Override
            public void onIceCandidateReceived(String from, JSONObject json) {
                try {
                    IceCandidate candidate = new IceCandidate(
                            json.getString("sdpMid"),
                            json.getInt("sdpMLineIndex"),
                            json.getString("candidate")
                    );
                    webRtcManager.addIceCandidate(candidate);
                    Log.d(TAG, "Added ICE candidate");
                } catch (Exception e) {
                    Log.e(TAG, "Failed to parse ICE candidate", e);
                }
            }

            @Override public void onConnected() {}
            @Override public void onDisconnected() {}
            @Override public void onAnswerReceived(String from, String sdp) {}
        });

        if (!webRtcManager.isPeerConnected() && !webRtcManager.hasStartedFlow()) {
            signaling.sendHello(android.os.Build.MODEL);
            Log.d(TAG, "Gửi HELLO từ RemoteActivity: " + android.os.Build.MODEL);
        } else {
            Log.d(TAG, "Đã có kết nối P2P — không gửi HELLO");
        }

        VideoTrack track = webRtcManager.getRemoteVideoTrack();
        if (track != null) {
            Log.d(TAG, "Gắn lại video track");
            track.addSink(remoteView);
        }

        Log.d(TAG, "Vào RemoteActivity với PC ID: " + SessionManager.getConnectedPcId());

        // --- Toggle control panel + move handle ---
        handlePanel = findViewById(R.id.handle_panel);
        controlPanel = findViewById(R.id.control_panel);
        btnKeyboard = findViewById(R.id.btn_keyboard);
        hiddenInput = findViewById(R.id.hidden_input);

        handlePanel.setOnClickListener(v -> {
            boolean isVisible = controlPanel.getVisibility() == View.VISIBLE;

            controlPanel.setVisibility(isVisible ? View.GONE : View.VISIBLE);
            handlePanel.setImageResource(isVisible ? R.drawable.ic_down : R.drawable.ic_up);

            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) handlePanel.getLayoutParams();
            if (isVisible) {
                params.topMargin = 0;
            } else {
                controlPanel.post(() -> {
                    params.topMargin = controlPanel.getHeight();
                    handlePanel.setLayoutParams(params);
                });
                return;
            }
            handlePanel.setLayoutParams(params);
        });

        // --- Show soft keyboard when btnKeyboard is pressed ---
        btnKeyboard.setOnClickListener(v -> {
            hiddenInput.setVisibility(View.VISIBLE);
            hiddenInput.requestFocus();

            hiddenInput.postDelayed(() -> {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(hiddenInput, InputMethodManager.SHOW_IMPLICIT);
                }
            }, 100);
        });

        MouseInputHandler mouseHandler = new MouseInputHandler(webRtcManager);
        remoteView.setOnTouchListener(mouseHandler);

        KeyboardInputHandler keyboardHandler = new KeyboardInputHandler(webRtcManager);
        keyboardHandler.attach(hiddenInput);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (remoteView != null) remoteView.release();
        webRtcManager.clearRemoteRenderer();
        Log.d(TAG, "Remote renderer cleared");
    }

    private void forceLandscapeFullscreen() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }
}
