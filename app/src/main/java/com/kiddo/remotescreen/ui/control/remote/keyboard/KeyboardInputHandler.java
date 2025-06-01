package com.kiddo.remotescreen.ui.control.remote.keyboard;

import static com.kiddo.remotescreen.ui.control.remote.keyboard.KeyboardAction.PRESS;
import static com.kiddo.remotescreen.ui.control.remote.keyboard.KeyboardAction.RELEASE;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.kiddo.remotescreen.R;
import com.kiddo.remotescreen.util.webrtc.WebRtcManager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class KeyboardInputHandler {

    private final WebRtcManager rtc;
    private final Set<Integer> lockedModifiers = new HashSet<>();
    private final Map<Integer, Button> modifierButtons = new HashMap<>();

    private static final int[] MODIFIER_KEYS = {
            KeyEvent.KEYCODE_TAB,
            KeyEvent.KEYCODE_SHIFT_LEFT,
            KeyEvent.KEYCODE_SHIFT_RIGHT,
            KeyEvent.KEYCODE_CTRL_LEFT,
            KeyEvent.KEYCODE_CTRL_RIGHT,
            KeyEvent.KEYCODE_ALT_LEFT,
            KeyEvent.KEYCODE_ALT_RIGHT,
            KeyEvent.KEYCODE_WINDOW
    };

    private static final Set<Integer> HOLDABLE_KEYS = new HashSet<>(Arrays.asList(
            KeyEvent.KEYCODE_SHIFT_LEFT,
            KeyEvent.KEYCODE_SHIFT_RIGHT,
            KeyEvent.KEYCODE_CTRL_LEFT,
            KeyEvent.KEYCODE_CTRL_RIGHT,
            KeyEvent.KEYCODE_ALT_LEFT,
            KeyEvent.KEYCODE_ALT_RIGHT,
            KeyEvent.KEYCODE_WINDOW
    ));

    public KeyboardInputHandler(WebRtcManager rtcManager) {
        this.rtc = rtcManager;
    }

    @SuppressLint("NewApi")
    public void attach(EditText input) {
        input.setOnKeyListener((v, keyCode, event) -> {
            if (!rtc.isDataChannelReady()) return false;

            int action = event.getAction();

            if (event.getDevice() != null && event.getDevice().isExternal()) {
                if (HOLDABLE_KEYS.contains(keyCode)) {
                    if (action == KeyEvent.ACTION_DOWN) {
                        rtc.sendKey(keyCode, PRESS);
                    } else if (action == KeyEvent.ACTION_UP) {
                        rtc.sendKey(keyCode, RELEASE);
                    }
                } else {
                    if (action == KeyEvent.ACTION_DOWN) {
                        rtc.sendKey(keyCode, PRESS);
                        rtc.sendKey(keyCode, RELEASE);
                    }
                }
                return false;
            }

            if (action == KeyEvent.ACTION_DOWN) {
                rtc.sendKey(keyCode, PRESS);
            } else if (action == KeyEvent.ACTION_UP) {
                rtc.sendKey(keyCode, RELEASE);
            }

            return false;
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    public void registerHoldableKey(int keyCode, Button button) {
        final Handler handler = new Handler();
        final Runnable[] repeatTask = new Runnable[1];

        button.setOnTouchListener((v, event) -> {
            if (!rtc.isDataChannelReady()) return false;

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    button.setAlpha(0.5f);
                    rtc.sendKey(keyCode, PRESS);

                    repeatTask[0] = new Runnable() {
                        @Override
                        public void run() {
                            rtc.sendKey(keyCode, PRESS);
                            handler.postDelayed(this, 100); // Lặp lại mỗi 100ms
                        }
                    };
                    handler.postDelayed(repeatTask[0], 400); // Bắt đầu lặp sau 400ms
                    return true;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    button.setAlpha(1.0f);
                    rtc.sendKey(keyCode, RELEASE);
                    handler.removeCallbacks(repeatTask[0]);
                    return true;
            }
            return false;
        });
    }

    public void registerClickKey(int keyCode, Button button) {
        button.setOnClickListener(v -> {
            if (!rtc.isDataChannelReady()) return;

            rtc.sendKey(keyCode, PRESS);
            rtc.sendKey(keyCode, RELEASE);

            button.setAlpha(0.5f);
            button.postDelayed(() -> button.setAlpha(1.0f), 100);
        });
    }

    public void registerModifierButton(int keyCode, Button button) {
        modifierButtons.put(keyCode, button);
        button.setOnClickListener(v -> toggleModifier(keyCode));
    }

    public boolean toggleModifier(int keyCode) {
        if (!rtc.isDataChannelReady() || !isModifierKey(keyCode)) return false;
        return handleModifierKey(keyCode);
    }

    public boolean isModifierToggled(int keyCode) {
        return lockedModifiers.contains(keyCode);
    }

    private boolean handleModifierKey(int keyCode) {
        Button btn = modifierButtons.get(keyCode);
        if (lockedModifiers.contains(keyCode)) {
            rtc.sendKey(keyCode, RELEASE);
            lockedModifiers.remove(keyCode);
            if (btn != null) {
                btn.setBackgroundResource(R.drawable.background_kb_button);
            }
        } else {
            rtc.sendKey(keyCode, PRESS);
            lockedModifiers.add(keyCode);
            if (btn != null) {
                btn.setBackgroundResource(R.drawable.background_button_selected);
            }
        }
        return true;
    }

    private boolean isModifierKey(int keyCode) {
        for (int mod : MODIFIER_KEYS) {
            if (mod == keyCode) return true;
        }
        return false;
    }

    public void releaseAllModifiers() {
        for (int keyCode : new HashSet<>(lockedModifiers)) {
            rtc.sendKey(keyCode, RELEASE);
            lockedModifiers.remove(keyCode);
            Button btn = modifierButtons.get(keyCode);
            if (btn != null) {
                btn.setBackgroundResource(R.drawable.background_kb_button);
            }
        }
    }
}
