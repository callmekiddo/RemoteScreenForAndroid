package com.kiddo.remotescreen.model;

import android.view.KeyEvent;

import com.kiddo.remotescreen.util.webrtc.WebRtcManager;

public class KeyFunction {

    public enum Type {
        KEYBOARD, MOUSE
    }

    private Type type;
    private int keyCode;        // Dành cho phím
    private String mouseAction; // Dành cho chuột

    // Constructor mặc định cho Gson
    public KeyFunction() {}

    //  Tạo phím
    public static KeyFunction forKey(int keyCode) {
        KeyFunction f = new KeyFunction();
        f.type = Type.KEYBOARD;
        f.keyCode = keyCode;
        return f;
    }

    // Tạo chuột
    public static KeyFunction forMouse(String action) {
        KeyFunction f = new KeyFunction();
        f.type = Type.MOUSE;
        f.mouseAction = action;
        return f;
    }

    public Type getType() {
        return type;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public String getMouseAction() {
        return mouseAction;
    }

    public String getLabel() {
        if (type == Type.KEYBOARD) {
            String raw = KeyEvent.keyCodeToString(keyCode);
            String label = raw
                    .replace("KEYCODE_", "")
                    .replace("_", " ")
                    .replace("GRAVE", "BACKTICK")
                    .replace("DEL", "BACKSPACE")
                    .trim();
            return "Keyboard " + label;
        } else {
            // Chuyển mouseAction thành dạng dễ đọc hơn
            String label = switch (mouseAction.toLowerCase()) {
                case "mouse_press" -> "Left";
                case "right_click" -> "Right";
                default -> mouseAction;
            };
            return "Mouse " + label;
        }
    }


    public void send(WebRtcManager rtc) {
        if (rtc == null || !rtc.isDataChannelReady()) return;

        if (type == Type.KEYBOARD) {
            rtc.sendKey(keyCode, "press");
            rtc.sendKey(keyCode, "release");
        } else if (type == Type.MOUSE) {
            if (mouseAction.equals("mouse_press")) {
                rtc.sendMouseCommand(0, 0, "mouse_press");
                rtc.sendMouseCommand(0, 0, "mouse_release");
            } else {
                rtc.sendMouseCommand(0, 0, mouseAction);
            }

        }
    }
}
