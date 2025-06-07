package com.kiddo.remotescreen.ui.control.remote;

import static com.kiddo.remotescreen.ui.control.remote.keyboard.KeyboardAction.PRESS;
import static com.kiddo.remotescreen.ui.control.remote.keyboard.KeyboardAction.RELEASE;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.kiddo.remotescreen.R;
import com.kiddo.remotescreen.model.ButtonData;
import com.kiddo.remotescreen.model.KeyFunction;
import com.kiddo.remotescreen.model.LayoutInfo;
import com.kiddo.remotescreen.repository.LayoutRepository;
import com.kiddo.remotescreen.ui.control.remote.dialog.SelectLayoutDialog;
import com.kiddo.remotescreen.ui.control.remote.mouse.MouseInputHandler;
import com.kiddo.remotescreen.util.CustomKeyboardView;
import com.kiddo.remotescreen.util.signaling.SignalingClient;
import com.kiddo.remotescreen.util.signaling.SignalingObserver;
import com.kiddo.remotescreen.util.webrtc.WebRtcManager;

import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.RendererCommon;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoTrack;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RemoteActivity extends AppCompatActivity {
    private static final String TAG = "RemoteActivity";

    private SurfaceViewRenderer remoteView;
    private WebRtcManager webRtcManager;

    private FrameLayout remoteRoot;
    private FrameLayout layoutOverlay;
    private ImageView handlePanel;
    private LinearLayout controlPanel;
    private LinearLayout infoPanel;
    private ImageView btnKeyboard;
    private ImageView btnLayout;
    private CustomKeyboardView keyboardOverlay;
    private TextView txtFps;
    private TextView txtBitrate;

    private final AtomicInteger frameCount = new AtomicInteger();
    private long previousBytes = 0;
    private long previousTimestamp = 0;

    private boolean isLayoutShown = false;
    private View layoutView;

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

    @SuppressLint({"ClickableViewAccessibility", "NewApi"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote);
        forceLandscapeFullscreen();

        remoteView = findViewById(R.id.remote_view);
        remoteRoot = findViewById(R.id.remote_root);
        layoutOverlay = findViewById(R.id.layout_overlay);

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
        btnLayout = findViewById(R.id.btn_layout);
        keyboardOverlay = findViewById(R.id.keyboard_overlay);
        txtFps = findViewById(R.id.txt_fps);
        txtBitrate = findViewById(R.id.txt_bitrate);

        handlePanel.setOnTouchListener(new View.OnTouchListener() {
            float initialX, dX;
            long downTime;
            boolean moved = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = event.getRawX();
                        dX = v.getX() - event.getRawX();
                        downTime = System.currentTimeMillis();
                        moved = false;
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float newX = event.getRawX() + dX;
                        v.setX(newX);
                        moved = true;
                        return true;

                    case MotionEvent.ACTION_UP:
                        long clickDuration = System.currentTimeMillis() - downTime;
                        if (!moved || clickDuration < 200) {
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
                                return true;
                            }
                            handlePanel.setLayoutParams(params);
                        }
                        return true;
                }
                return false;
            }
        });

        btnKeyboard.setOnClickListener(v -> {
            boolean isVisible = keyboardOverlay.getVisibility() == View.VISIBLE;
            keyboardOverlay.setVisibility(isVisible ? View.GONE : View.VISIBLE);
        });

        btnLayout.setOnClickListener(v -> {
            if (isLayoutShown) {
                layoutOverlay.removeAllViews();
                layoutView = null;
                isLayoutShown = false;
            } else {
                List<LayoutInfo> layouts = LayoutRepository.loadAll(getApplicationContext());
                new SelectLayoutDialog(layouts, selectedLayout -> {
                    layoutOverlay.removeAllViews();

                    FrameLayout layoutContainer = new FrameLayout(this);
                    layoutView = layoutContainer;
                    layoutOverlay.addView(layoutContainer);

                    for (ButtonData btn : selectedLayout.getButtons()) {
                        Button view = new Button(this);
                        view.setText(btn.getName());
                        view.setTextColor(getColor(R.color.colorPrimary));
                        view.setBackgroundResource(R.drawable.background_button);

                        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                                (int)(btn.getWidthRatio() * layoutOverlay.getWidth()),
                                (int)(btn.getHeightRatio() * layoutOverlay.getHeight())
                        );
                        params.leftMargin = (int)(btn.getLeftRatio() * layoutOverlay.getWidth());
                        params.topMargin = (int)(btn.getTopRatio() * layoutOverlay.getHeight());
                        view.setLayoutParams(params);

                        view.setOnClickListener(vv -> {
                            KeyFunction f = btn.getFunction(); // nếu bạn đang dùng 1 function
                            if (f != null) f.send(webRtcManager);
                        });

                        layoutContainer.addView(view);
                    }

                    isLayoutShown = true;
                }).show(getSupportFragmentManager(), "select_layout");
            }
        });


        keyboardOverlay.setRtcManager(webRtcManager);

        remoteView.setOnKeyListener((v, keyCode, event) -> {
            if (event.getDevice() != null && event.getDevice().isExternal()) {
                keyboardOverlay.setVisibility(View.GONE);
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    webRtcManager.sendKey(keyCode, PRESS);
                } else if (event.getAction() == KeyEvent.ACTION_UP) {
                    webRtcManager.sendKey(keyCode, RELEASE);
                }
                return true;
            }
            return false;
        });
        remoteView.setFocusableInTouchMode(true);
        remoteView.requestFocus();

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

        remoteView.postDelayed(statsRunnable, 1000);
        remoteView.postDelayed(bitrateRunnable, 1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (remoteView != null) remoteView.release();
        webRtcManager.clearRemoteRenderer();

        if (keyboardOverlay != null && keyboardOverlay.getKeyboardHandler() != null) {
            keyboardOverlay.getKeyboardHandler().releaseAllModifiers();
        }
    }

    private void forceLandscapeFullscreen() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }
}
