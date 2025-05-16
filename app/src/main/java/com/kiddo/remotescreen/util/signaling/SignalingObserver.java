package com.kiddo.remotescreen.util.signaling;

import org.json.JSONObject;

public interface SignalingObserver {
    void onAnswerReceived(String from, String sdp);
    void onIceCandidateReceived(String from, JSONObject candidate);
    void onConnected();
    void onDisconnected();

    // ✅ Thêm dòng này để hỗ trợ nhận offer
    default void onOfferReceived(String from, String sdp) {}
}
