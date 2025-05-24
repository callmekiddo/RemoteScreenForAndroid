package com.kiddo.remotescreen.util.webrtc;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.kiddo.remotescreen.util.signaling.SignalingClient;

import org.webrtc.*;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class WebRtcManager {
    private static final String TAG = "WebRtcManager";
    private static WebRtcManager instance;

    private EglBase eglBase;
    private PeerConnectionFactory factory;
    private PeerConnection peerConnection;
    private SurfaceViewRenderer remoteRenderer;
    private boolean initialized = false;

    private String remoteDeviceId;
    private SignalingClient signalingClient;

    private boolean hasStartedFlow = false;
    private boolean peerConnected = false;

    private static final int MAX_ICE_CANDIDATES = 20;
    private final Set<String> sentCandidates = new HashSet<>();

    private DataChannel dataChannel;
    private boolean dataChannelReady = false;

    private int remoteScreenWidth = 1920;
    private int remoteScreenHeight = 1080;

    private int renderedVideoWidth = 1920;
    private int renderedVideoHeight = 1080;

    private Consumer<VideoTrack> onRemoteTrackCallback;

    private WebRtcManager() {}

    public static synchronized WebRtcManager getInstance() {
        if (instance == null) {
            instance = new WebRtcManager();
        }
        return instance;
    }

    public void init(Context context) {
        if (initialized) return;

        eglBase = EglBase.create();

        PeerConnectionFactory.initialize(
                PeerConnectionFactory.InitializationOptions.builder(context)
                        .setEnableInternalTracer(true)
                        .createInitializationOptions()
        );

        factory = PeerConnectionFactory.builder()
                .setVideoDecoderFactory(new DefaultVideoDecoderFactory(eglBase.getEglBaseContext()))
                .setVideoEncoderFactory(new DefaultVideoEncoderFactory(eglBase.getEglBaseContext(), true, true))
                .createPeerConnectionFactory();

        initialized = true;
        Log.d(TAG, "WebRtcManager initialized");
    }

    public boolean isInitialized() {
        return initialized;
    }

    public EglBase.Context getEglBaseContext() {
        return eglBase != null ? eglBase.getEglBaseContext() : null;
    }

    public void setRemoteRenderer(SurfaceViewRenderer renderer) {
        this.remoteRenderer = renderer;
    }

    public void clearRemoteRenderer() {
        if (remoteRenderer != null) {
            remoteRenderer.clearImage();
        }
    }

    public void setOnRemoteTrackCallback(Consumer<VideoTrack> callback) {
        this.onRemoteTrackCallback = callback;
    }

    public void setRenderedVideoSize(int width, int height) {
        this.renderedVideoWidth = width;
        this.renderedVideoHeight = height;
    }

    public int getRenderedVideoWidth() {
        return renderedVideoWidth;
    }

    public int getRenderedVideoHeight() {
        return renderedVideoHeight;
    }

    public void startRemoteFlow(String androidName) {
        if (!hasStartedFlow && signalingClient != null && signalingClient.isConnected()) {
            signalingClient.sendHello(androidName);
            Log.d(TAG, "Sent HELLO");
            hasStartedFlow = true;
        }
    }

    public void connectPeer(String remoteDeviceId, SignalingClient signalingClient) {
        this.remoteDeviceId = remoteDeviceId;
        this.signalingClient = signalingClient;
        this.peerConnected = false;
        this.hasStartedFlow = false;
        this.sentCandidates.clear();

        PeerConnection.IceServer stun = PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer();
        PeerConnection.IceServer turn = PeerConnection.IceServer.builder("turn:54.251.133.245:3478")
                .setUsername("kiddo")
                .setPassword("callmekiddo")
                .createIceServer();

        PeerConnection.RTCConfiguration config = new PeerConnection.RTCConfiguration(Arrays.asList(stun, turn));

        peerConnection = factory.createPeerConnection(config, new PeerConnection.Observer() {
            @Override
            public void onIceCandidate(IceCandidate candidate) {
                if (sentCandidates.size() < MAX_ICE_CANDIDATES &&
                        !sentCandidates.contains(candidate.sdp) &&
                        (candidate.sdp.contains("typ host") || candidate.sdp.contains("typ srflx") || candidate.sdp.contains("typ relay"))) {

                    sentCandidates.add(candidate.sdp);
                    signalingClient.sendIceCandidate(remoteDeviceId, candidate);
                    Log.d(TAG, "Sent ICE candidate: " + candidate.sdp);
                }
            }

            @SuppressLint("NewApi")
            @Override
            public void onTrack(RtpTransceiver transceiver) {
                MediaStreamTrack track = transceiver.getReceiver().track();
                if (track instanceof VideoTrack) {
                    VideoTrack videoTrack = (VideoTrack) track;
                    videoTrack.setEnabled(true);

                    if (remoteRenderer != null) {
                        videoTrack.addSink(remoteRenderer);
                    }

                    if (onRemoteTrackCallback != null) {
                        onRemoteTrackCallback.accept(videoTrack);
                    }

                    Log.d(TAG, "Video track received and callback invoked");
                }
            }

            @Override
            public void onConnectionChange(PeerConnection.PeerConnectionState newState) {
                Log.d(TAG, "PeerConnection state changed: " + newState);
                peerConnected = (newState == PeerConnection.PeerConnectionState.CONNECTED);
            }

            @Override
            public void onDataChannel(DataChannel dc) {
                dataChannel = dc;
                dataChannel.registerObserver(new DataChannel.Observer() {
                    @Override public void onBufferedAmountChange(long l) {}
                    @Override public void onStateChange() {
                        dataChannelReady = (dataChannel.state() == DataChannel.State.OPEN);
                        Log.d(TAG, "DataChannel state: " + dataChannel.state());
                    }
                    @Override
                    public void onMessage(DataChannel.Buffer buffer) {
                        if (!buffer.binary) {
                            String msg = StandardCharsets.UTF_8.decode(buffer.data).toString();
                            if (msg.startsWith("screen-info:")) {
                                try {
                                    String[] parts = msg.substring("screen-info:".length()).split("x");
                                    int width = Integer.parseInt(parts[0]);
                                    int height = Integer.parseInt(parts[1]);
                                    setRemoteScreenSize(width, height);
                                    Log.d(TAG, "Received screen-info: " + width + "x" + height);
                                } catch (Exception e) {
                                    Log.e(TAG, "Invalid screen-info format", e);
                                }
                            }
                        }
                    }
                });
            }

            @Override public void onSignalingChange(PeerConnection.SignalingState newState) {}
            @Override public void onIceConnectionChange(PeerConnection.IceConnectionState newState) {}
            @Override public void onIceGatheringChange(PeerConnection.IceGatheringState newState) {}
            @Override public void onIceCandidatesRemoved(IceCandidate[] candidates) {}
            @Override public void onAddStream(MediaStream stream) {}
            @Override public void onRemoveStream(MediaStream stream) {}
            @Override public void onRenegotiationNeeded() {}
            @Override public void onIceConnectionReceivingChange(boolean b) {}
        });

        peerConnection.createDataChannel("keyboard", new DataChannel.Init());
    }

    public PeerConnection getPeerConnection() {
        return peerConnection;
    }

    public boolean isDataChannelReady() {
        return dataChannelReady;
    }

    public void sendKey(int keyCode, String action) {
        if (!dataChannelReady || dataChannel == null) return;
        String msg = "keycode:" + keyCode + "," + action;
        ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes(StandardCharsets.UTF_8));
        dataChannel.send(new DataChannel.Buffer(buffer, false));
        Log.d(TAG, "Sent keycode: " + msg);
    }

    public void sendKeyInstant(int keyCode) {
        sendKey(keyCode, "press");
        sendKey(keyCode, "release");
    }

    public void sendMouseCommand(int x, int y, String action) {
        if (!dataChannelReady || dataChannel == null) return;
        String msg = String.format("mouse:%d,%d,%s", x, y, action);
        ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes(StandardCharsets.UTF_8));
        dataChannel.send(new DataChannel.Buffer(buffer, false));
        Log.d(TAG, "Sent mouse command: " + msg);
    }

    public void sendMouseMove(int x, int y) {
        sendMouseCommand(x, y, "move");
    }

    public void setRemoteSdp(SessionDescription sdp, Runnable onSuccess) {
        if (peerConnection == null) return;
        peerConnection.setRemoteDescription(new SdpObserverAdapter() {
            @Override
            public void onSetSuccess() {
                onSuccess.run();
            }
        }, sdp);
    }

    public void createAndSendAnswer() {
        if (peerConnection == null) return;
        peerConnection.createAnswer(new SdpObserverAdapter() {
            @Override
            public void onCreateSuccess(SessionDescription answer) {
                peerConnection.setLocalDescription(new SdpObserverAdapter() {
                    @Override
                    public void onSetSuccess() {
                        Log.d(TAG, "Local SDP set");
                    }
                }, answer);
                signalingClient.sendAnswer(remoteDeviceId, answer.description);
                Log.d(TAG, "Sent ANSWER");
            }
        }, new MediaConstraints());
    }

    public void addIceCandidate(IceCandidate candidate) {
        if (peerConnection != null) {
            peerConnection.addIceCandidate(candidate);
        }
    }

    public void disconnect() {
        Log.d(TAG, "Cleaning up WebRtcManager (disconnect only)");
        if (peerConnection != null) {
            peerConnection.close();
            peerConnection.dispose();
            peerConnection = null;
        }
        dataChannelReady = false;
        dataChannel = null;
        peerConnected = false;
        hasStartedFlow = false;
        sentCandidates.clear();
        clearRemoteRenderer();
    }

    public void reset() {
        Log.d(TAG, "Full WebRtcManager reset");
        disconnect();
        if (factory != null) {
            factory.dispose();
            factory = null;
        }
        if (eglBase != null) {
            eglBase.release();
            eglBase = null;
        }
        initialized = false;
    }

    public boolean isPeerConnected() {
        return peerConnected;
    }

    public boolean hasStartedFlow() {
        return hasStartedFlow;
    }

    public VideoTrack getRemoteVideoTrack() {
        if (peerConnection == null) return null;
        for (RtpTransceiver transceiver : peerConnection.getTransceivers()) {
            MediaStreamTrack track = transceiver.getReceiver().track();
            if (track instanceof VideoTrack) {
                return (VideoTrack) track;
            }
        }
        return null;
    }

    public int getRemoteScreenWidth() {
        return remoteScreenWidth > 0 ? remoteScreenWidth : 1920;
    }

    public int getRemoteScreenHeight() {
        return remoteScreenHeight > 0 ? remoteScreenHeight : 1080;
    }

    public void setRemoteScreenSize(int width, int height) {
        this.remoteScreenWidth = width;
        this.remoteScreenHeight = height;
    }

    private static abstract class SdpObserverAdapter implements SdpObserver {
        @Override public void onCreateSuccess(SessionDescription sdp) {}
        @Override public void onSetSuccess() {}
        @Override public void onCreateFailure(String error) { Log.e(TAG, "SDP create failed: " + error); }
        @Override public void onSetFailure(String error) { Log.e(TAG, "SDP set failed: " + error); }
    }
}
