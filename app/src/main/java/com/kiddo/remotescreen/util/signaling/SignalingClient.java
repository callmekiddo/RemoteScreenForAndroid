package com.kiddo.remotescreen.util.signaling;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONObject;
import org.webrtc.IceCandidate;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class SignalingClient {
    private static final String TAG = "SignalingClient";
    private static SignalingClient instance;

    private final OkHttpClient client;
    private WebSocket webSocket;
    private SignalingObserver listener;

    private final Handler timeoutHandler = new Handler(Looper.getMainLooper());
    private final long ANSWER_TIMEOUT_MS = 10000;

    private boolean waitingForAnswer = false;
    private boolean connected = false;

    private SignalingClient(SignalingObserver listener) {
        this.listener = listener;
        this.client = new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .build();
    }

    public static SignalingClient getInstance(SignalingObserver listener) {
        if (instance == null) {
            instance = new SignalingClient(listener);
        } else {
            instance.listener = listener;
        }
        return instance;
    }

    public static SignalingClient getInstance() {
        return instance;
    }

    public void connect(String url) {
        Log.d(TAG, "📡 Connecting to WebSocket URL: " + url);
        Request request = new Request.Builder().url(url).build();
        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
                connected = true;
                Log.d(TAG, "✅ Connected to signaling server");
                listener.onConnected();
            }

            @Override
            public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
                try {
                    JSONObject json = new JSONObject(text);
                    String type = json.getString("type");
                    String fromUser = json.getString("fromUser");

                    switch (type) {
                        case "offer" -> listener.onOfferReceived(fromUser, json.getString("sdp"));
                        case "answer" -> {
                            waitingForAnswer = false;
                            timeoutHandler.removeCallbacksAndMessages(null);
                            listener.onAnswerReceived(fromUser, json.getString("sdp"));
                        }
                        case "ice_candidate" -> listener.onIceCandidateReceived(fromUser, json.getJSONObject("candidate"));
                        case "hello" -> Log.d(TAG, "👋 Received HELLO from: " + fromUser); // optional
                        default -> Log.w(TAG, "⚠ Unknown message type: " + type);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "❌ Failed to parse signaling message", e);
                }
            }

            @Override
            public void onClosing(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                Log.d(TAG, "🔌 Closing: " + reason);
            }

            @Override
            public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                connected = false;
                Log.d(TAG, "🔌 Closed: " + reason);
                listener.onDisconnected();
            }

            @Override
            public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, Response response) {
                connected = false;
                Log.e(TAG, "❌ WebSocket failure: " + t.getMessage());
                if (response != null) {
                    Log.e(TAG, "Status: " + response.code() + " → " + response.message());
                }
                listener.onDisconnected();
            }
        });
    }

    public boolean isConnected() {
        return connected;
    }

    public void sendMessage(JSONObject message) {
        if (webSocket != null) {
            webSocket.send(message.toString());
        }
    }

    public void sendOffer(String toUser, String sdp) {
        try {
            JSONObject json = new JSONObject();
            json.put("type", "offer");
            json.put("toUser", toUser);
            json.put("fromUser", getSelfId());
            json.put("sdp", sdp);
            sendMessage(json);

            waitingForAnswer = true;
            timeoutHandler.postDelayed(() -> {
                if (waitingForAnswer) {
                    Log.e(TAG, "❌ No answer received within timeout");
                    listener.onDisconnected();
                    close();
                }
            }, ANSWER_TIMEOUT_MS);
        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to send offer", e);
        }
    }

    public void sendAnswer(String toUser, String sdp) {
        try {
            JSONObject json = new JSONObject();
            json.put("type", "answer");
            json.put("toUser", toUser);
            json.put("fromUser", getSelfId());
            json.put("sdp", sdp);
            sendMessage(json);
        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to send answer", e);
        }
    }

    public void sendIceCandidate(String toUser, IceCandidate candidate) {
        try {
            JSONObject json = new JSONObject();
            json.put("type", "ice_candidate");
            json.put("toUser", toUser);
            json.put("fromUser", getSelfId());

            JSONObject candidateJson = new JSONObject();
            candidateJson.put("sdpMid", candidate.sdpMid);
            candidateJson.put("sdpMLineIndex", candidate.sdpMLineIndex);
            candidateJson.put("candidate", candidate.sdp);

            json.put("candidate", candidateJson);
            sendMessage(json);
        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to send ICE candidate", e);
        }
    }

    public void sendHello(String fromUser) {
        try {
            JSONObject json = new JSONObject();
            json.put("type", "hello");
            json.put("fromUser", fromUser);
            sendMessage(json);
            Log.d(TAG, "📤 Sent HELLO to server");
        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to send hello", e);
        }
    }

    public void close() {
        if (webSocket != null) {
            webSocket.close(1000, "Normal closure");
            webSocket = null;
        }
        connected = false;
        waitingForAnswer = false;
        timeoutHandler.removeCallbacksAndMessages(null);
    }

    public void reset() {
        close();
    }

    private String getSelfId() {
        return android.os.Build.MODEL;
    }

    public void setObserver(SignalingObserver observer) {
        this.listener = observer;
    }
}
