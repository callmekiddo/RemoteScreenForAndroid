package com.kiddo.remotescreen.ui.control.remote;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.kiddo.remotescreen.R;
import com.kiddo.remotescreen.ui.control.remote.keyboard.KeyboardInputHandler;
import com.kiddo.remotescreen.ui.control.remote.mouse.MouseInputHandler;
import com.kiddo.remotescreen.util.signaling.SignalingClient;
import com.kiddo.remotescreen.util.signaling.SignalingObserver;
import com.kiddo.remotescreen.util.webrtc.WebRtcManager;

import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.RendererCommon;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoTrack;

import java.util.concurrent.atomic.AtomicInteger;

public class RemoteActivity extends AppCompatActivity {
    private static final String TAG = "RemoteActivity";

    private SurfaceViewRenderer remoteView;
    private WebRtcManager webRtcManager;

    private FrameLayout remoteRoot;
    private ImageView handlePanel;
    private LinearLayout controlPanel;
    private LinearLayout infoPanel;
    private ImageView btnKeyboard;
    private EditText hiddenInput;
    private TextView txtFps;
    private TextView txtBitrate;

    private final AtomicInteger frameCount = new AtomicInteger();
    private long previousBytes = 0;
    private long previousTimestamp = 0;

    private final Runnable statsRunnable = () -> {
        int fps = frameCount.getAndSet(0);
        txtFps.setText("FPS: " + fps);
        remoteView.postDelayed(this.statsRunnable, 1000);
    };

    private final Runnable bitrateRunnable = () -> {
        if (webRtcManager.getPeerConnection() != null) {
            webRtcManager.getPeerConnection().getStats(rtcStatsReport -> {
                for (org.webrtc.RTCStats stats : rtcStatsReport.getStatsMap().values()) {
                    if ("inbound-rtp".equals(stats.getType()) || "outbound-rtp".equals(stats.getType())) {
                        Object bytesObj = stats.getMembers().get("bytesReceived");
                        if (bytesObj == null) {
                            bytesObj = stats.getMembers().get("bytesSent");
                        }

                        if (bytesObj instanceof Number) {
                            long bytes = ((Number) bytesObj).longValue();
                            double timestamp = stats.getTimestampUs() / 1000.0;
                            long timeNow = (long) timestamp;

                            if (previousTimestamp > 0) {
                                long timeDiff = timeNow - previousTimestamp;
                                long byteDiff = bytes - previousBytes;

                                if (timeDiff > 0) {
                                    double bitrateKbps = (byteDiff * 8.0) / timeDiff;
                                    runOnUiThread(() ->
                                            txtBitrate.setText("Bitrate: " + String.format("%.1f kbps", bitrateKbps))
                                    );
                                }
                            }

                            previousBytes = bytes;
                            previousTimestamp = timeNow;
                            break;
                        }
                    }
                }
            });
        }

        remoteView.postDelayed(this.bitrateRunnable, 1000);
    };

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote);
        forceLandscapeFullscreen();

        remoteView = findViewById(R.id.remote_view);
        remoteRoot = findViewById(R.id.remote_root);

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

        remoteView.addFrameListener(bitmap -> {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            webRtcManager.setRenderedVideoSize(width, height);
        }, 1.0f, null);

        SignalingClient signaling = SignalingClient.getInstance();
        signaling.setObserver(new SignalingObserver() {
            @Override public void onOfferReceived(String from, String sdp) {
                SessionDescription offer = new SessionDescription(SessionDescription.Type.OFFER, sdp);
                webRtcManager.setRemoteSdp(offer, webRtcManager::createAndSendAnswer);
            }

            @Override public void onIceCandidateReceived(String from, JSONObject json) {
                try {
                    IceCandidate candidate = new IceCandidate(
                            json.getString("sdpMid"),
                            json.getInt("sdpMLineIndex"),
                            json.getString("candidate")
                    );
                    webRtcManager.addIceCandidate(candidate);
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
        }

        VideoTrack track = webRtcManager.getRemoteVideoTrack();
        if (track != null) {
            track.addSink(remoteView);
            track.addSink(frame -> frameCount.incrementAndGet());
        } else {
            webRtcManager.setOnRemoteTrackCallback(t -> {
                t.addSink(remoteView);
                t.addSink(frame -> frameCount.incrementAndGet());
            });
        }

        handlePanel = findViewById(R.id.handle_panel);
        controlPanel = findViewById(R.id.control_panel);
        infoPanel = findViewById(R.id.info_panel);
        btnKeyboard = findViewById(R.id.btn_keyboard);
        hiddenInput = findViewById(R.id.hidden_input);
        txtFps = findViewById(R.id.txt_fps);
        txtBitrate = findViewById(R.id.txt_bitrate);

        handlePanel.setOnClickListener(v -> {
            boolean isVisible = controlPanel.getVisibility() == View.VISIBLE;
            controlPanel.setVisibility(isVisible ? View.GONE : View.VISIBLE);
            infoPanel.setVisibility(isVisible ? View.GONE : View.VISIBLE);
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

        MouseInputHandler gestureHandler = new MouseInputHandler(webRtcManager);

        remoteView.setOnTouchListener((v, event) -> {
            if (!webRtcManager.isDataChannelReady()) return false;

            int videoWidth = webRtcManager.getRenderedVideoWidth();
            int videoHeight = webRtcManager.getRenderedVideoHeight();
            int viewWidth = remoteView.getWidth();
            int viewHeight = remoteView.getHeight();

            gestureHandler.onTouch(event, videoWidth, videoHeight, viewWidth, viewHeight);
            return true;
        });

        new KeyboardInputHandler(webRtcManager).attach(hiddenInput);

        remoteView.postDelayed(statsRunnable, 1000);
        remoteView.postDelayed(bitrateRunnable, 1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (remoteView != null) remoteView.release();
        webRtcManager.clearRemoteRenderer();
    }

    private void forceLandscapeFullscreen() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }
}
