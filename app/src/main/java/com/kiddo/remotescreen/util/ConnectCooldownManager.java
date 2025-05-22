package com.kiddo.remotescreen.util;

public class ConnectCooldownManager {

    private static final long COOLDOWN_MS = 5000;
    private static long lastDisconnectTime = 0;

    private static Runnable onCooldownEnd;

    // Gọi khi vừa disconnect
    public static void setCooldown(Runnable onCooldownEndCallback) {
        lastDisconnectTime = System.currentTimeMillis();
        onCooldownEnd = onCooldownEndCallback;

        new Thread(() -> {
            try {
                Thread.sleep(COOLDOWN_MS);
                if (onCooldownEnd != null) {
                    onCooldownEnd.run();
                }
            } catch (InterruptedException ignored) {}
        }).start();
    }

    // Gọi nếu không cần callback
    public static void setCooldown() {
        setCooldown(null);
    }

    // Kiểm tra còn cooldown không
    public static boolean isCooldownActive() {
        return getRemainingCooldownMs() > 0;
    }

    // Lấy số mili giây còn lại
    public static long getRemainingCooldownMs() {
        long elapsed = System.currentTimeMillis() - lastDisconnectTime;
        return Math.max(0, COOLDOWN_MS - elapsed);
    }
}
