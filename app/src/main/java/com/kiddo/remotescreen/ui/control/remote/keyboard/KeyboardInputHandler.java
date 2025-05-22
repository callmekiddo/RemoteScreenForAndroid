package com.kiddo.remotescreen.ui.control.remote.keyboard;

import static com.kiddo.remotescreen.ui.control.remote.keyboard.KeyboardAction.PRESS;
import static com.kiddo.remotescreen.ui.control.remote.keyboard.KeyboardAction.RELEASE;

import android.view.KeyEvent;
import android.widget.EditText;

import com.kiddo.remotescreen.util.webrtc.WebRtcManager;

public class KeyboardInputHandler {

    private final WebRtcManager rtc;
    private boolean isShiftLocked = false;

    public KeyboardInputHandler(WebRtcManager rtcManager) {
        this.rtc = rtcManager;
    }

    public void attach(EditText input) {
        input.setOnKeyListener((v, keyCode, event) -> {
            if (!rtc.isDataChannelReady()) return false;

            String action = (event.getAction() == KeyEvent.ACTION_DOWN) ? PRESS : RELEASE;

            if (keyCode == KeyEvent.KEYCODE_SHIFT_LEFT || keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (!isShiftLocked) {
                        rtc.sendKey(keyCode, PRESS);
                        isShiftLocked = true;
                    } else {
                        rtc.sendKey(keyCode, RELEASE);
                        isShiftLocked = false;
                    }
                    return true;
                }
                return false;
            }

            if (isShiftLocked && event.getAction() == KeyEvent.ACTION_DOWN) {
                rtc.sendKey(keyCode, PRESS);
                rtc.sendKey(keyCode, RELEASE);
                rtc.sendKey(KeyEvent.KEYCODE_SHIFT_LEFT, RELEASE);
                isShiftLocked = false;
            } else {
                rtc.sendKey(keyCode, action);
            }

            return false;
        });
    }
}
