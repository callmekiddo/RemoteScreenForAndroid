package com.kiddo.remotescreen.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "user_session";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_TOKEN = "auth_token";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_EXPIRES_IN = "token_expires";

    private static String connectedPcId;
    private static String connectedPcName;

    private static SessionManager instance;
    private final SharedPreferences prefs;

    private SessionManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context);
        }
        return instance;
    }

    // === Đăng nhập ===

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void setLoggedIn(boolean loggedIn) {
        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, loggedIn).apply();
    }

    public void saveUserSession(String token, String fullName, long expiresIn, String email) {
        prefs.edit()
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .putString(KEY_TOKEN, token)
                .putString(KEY_FULL_NAME, fullName)
                .putLong(KEY_EXPIRES_IN, System.currentTimeMillis() + expiresIn)
                .putString(KEY_EMAIL, email)
                .apply();
    }

    public void clearSession() {
        prefs.edit().clear().apply();
    }

    // === Thông tin user ===

    public String getAuthToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public String getUserFullName() {
        return prefs.getString(KEY_FULL_NAME, null);
    }

    public String getUserEmail() {
        return prefs.getString(KEY_EMAIL, null);
    }

    public long getTokenExpiryTime() {
        return prefs.getLong(KEY_EXPIRES_IN, 0);
    }

    public boolean isTokenExpired() {
        return System.currentTimeMillis() > getTokenExpiryTime();
    }

    // === PC kết nối ===

    public static void setConnectedPcId(String pcId) {
        connectedPcId = pcId;
    }

    public static String getConnectedPcId() {
        return connectedPcId;
    }

    public static void setConnectedPcName(String name) {
        connectedPcName = name;
    }

    public static String getConnectedPcName() {
        return connectedPcName;
    }

    public static void clearConnectedPc() {
        connectedPcId = null;
        connectedPcName = null;
    }
}
