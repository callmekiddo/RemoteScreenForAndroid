package com.kiddo.remotescreen.util.webrtc;

import android.content.Context;
import android.util.Log;

import com.kiddo.remotescreen.util.signaling.SignalingClient;

import org.webrtc.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
        Log.d(TAG, "✅ WebRtcManager initialized");
    }

    public boolean isInitialized() {
        return initialized;
    }

    public EglBase.Context getEglBaseContext() {
        return eglBase != null ? eglBase.getEglBaseContext() : null;
    }

    public void setRemoteRenderer(SurfaceViewRenderer renderer) {
        this.remoteRenderer = renderer;
        if (peerConnection != null) {
            Log.d(TAG, "✅ Rebinding remote renderer if video track exists");
        }
    }

    public void clearRemoteRenderer() {
        if (remoteRenderer != null) {
            remoteRenderer.clearImage();
        }
    }

    public void startRemoteFlow(String androidName) {
        if (!hasStartedFlow && signalingClient != null && signalingClient.isConnected()) {
            signalingClient.sendHello(androidName);
            Log.d(TAG, "📤 Sent HELLO");
            hasStartedFlow = true;
        } else {
            Log.d(TAG, "⚠️ Skipped HELLO — already sent or not connected");
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
                        (candidate.sdp.contains("typ host") || candidate.sdp.contains("typ srflx"))) {

                    sentCandidates.add(candidate.sdp);
                    signalingClient.sendIceCandidate(remoteDeviceId, candidate);
                    Log.d(TAG, "📤 Sent ICE candidate: " + candidate.sdp);
                } else {
                    Log.d(TAG, "⛔ Skipped ICE candidate: " + candidate.sdp);
                }
            }

            @Override
            public void onTrack(RtpTransceiver transceiver) {
                MediaStreamTrack track = transceiver.getReceiver().track();
                if (track instanceof VideoTrack && remoteRenderer != null) {
                    Log.d(TAG, "🎥 Received video track");
                    VideoTrack videoTrack = (VideoTrack) track;
                    videoTrack.setEnabled(true);
                    videoTrack.addSink(remoteRenderer);
                }
            }

            @Override
            public void onConnectionChange(PeerConnection.PeerConnectionState newState) {
                Log.d(TAG, "🔄 PeerConnection state changed: " + newState);
                if (newState == PeerConnection.PeerConnectionState.CONNECTED) {
                    peerConnected = true;
                } else if (newState == PeerConnection.PeerConnectionState.DISCONNECTED ||
                        newState == PeerConnection.PeerConnectionState.FAILED ||
                        newState == PeerConnection.PeerConnectionState.CLOSED) {
                    peerConnected = false;
                }
            }

            @Override public void onSignalingChange(PeerConnection.SignalingState newState) {}
            @Override public void onIceConnectionChange(PeerConnection.IceConnectionState newState) {}
            @Override public void onIceGatheringChange(PeerConnection.IceGatheringState newState) {}
            @Override public void onIceCandidatesRemoved(IceCandidate[] candidates) {}
            @Override public void onAddStream(MediaStream stream) {}
            @Override public void onRemoveStream(MediaStream stream) {}
            @Override public void onDataChannel(DataChannel dc) {}
            @Override public void onRenegotiationNeeded() {}
            @Override public void onIceConnectionReceivingChange(boolean b) {}
        });

        peerConnection.createDataChannel("dummy", new DataChannel.Init());
    }

    public PeerConnection getPeerConnection() {
        return peerConnection;
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
                        Log.d(TAG, "✅ Local SDP set");
                    }
                }, answer);

                signalingClient.sendAnswer(remoteDeviceId, answer.description);
                Log.d(TAG, "📤 Sent ANSWER");
            }
        }, new MediaConstraints());
    }

    public void addIceCandidate(IceCandidate candidate) {
        if (peerConnection != null) {
            peerConnection.addIceCandidate(candidate);
        }
    }

    public void disconnect() {
        Log.d(TAG, "🧹 Cleaning up WebRtcManager (disconnect only)");
        if (peerConnection != null) {
            peerConnection.close();
            peerConnection.dispose();
            peerConnection = null;
        }
        peerConnected = false;
        hasStartedFlow = false;
        sentCandidates.clear();
        clearRemoteRenderer();
    }

    public void reset() {
        Log.d(TAG, "🧨 Full WebRtcManager reset");
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

    private static abstract class SdpObserverAdapter implements SdpObserver {
        @Override public void onCreateSuccess(SessionDescription sdp) {}
        @Override public void onSetSuccess() {}
        @Override public void onCreateFailure(String error) { Log.e(TAG, "SDP create failed: " + error); }
        @Override public void onSetFailure(String error) { Log.e(TAG, "SDP set failed: " + error); }
    }
}
