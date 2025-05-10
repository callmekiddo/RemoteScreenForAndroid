package com.kiddo.remotescreen.ui.layout.editor.popup;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;

import com.kiddo.remotescreen.R;

public class KeyboardFunctionPopup {

    public interface OnKeyboardFunctionSelected {
        void onSelected(String key);
    }

    private final Dialog dialog;

    public KeyboardFunctionPopup(Activity activity, OnKeyboardFunctionSelected callback) {
        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.view_keyboard_function_selector);
        dialog.setCancelable(true);

        ImageButton btnClose = dialog.findViewById(R.id.btnCloseKeyboardPopup);
        btnClose.setOnClickListener(v -> dialog.dismiss());

        String[] keys = {
                "esc", "f1", "f2", "f3", "f4", "f5", "f6", "f7", "f8", "f9", "f10", "f11", "f12",
                "backtick", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "hyphen", "equal", "backspace",
                "tab", "q", "w", "e", "r", "t", "y", "u", "i", "o", "p", "open_bracket", "close_bracket", "backslash",
                "caps_lock", "a", "s", "d", "f", "g", "h", "j", "k", "l", "semicolon", "apostrophe", "enter",
                "shift_left", "z", "x", "c", "v", "b", "n", "m", "comma", "dot", "slash", "shift_right",
                "control_left", "window", "alt_left", "space", "alt_right", "control_right",
                "arrow_up", "arrow_left", "arrow_down", "arrow_right"
        };

        for (String key : keys) {
            String viewIdName = "key_" + key;
            int resId = activity.getResources().getIdentifier(viewIdName, "id", activity.getPackageName());
            if (resId != 0) {
                View keyView = dialog.findViewById(resId);
                if (keyView instanceof Button) {
                    keyView.setOnClickListener(v -> {
                        if (callback != null) {
                            String displayKey = ((Button) keyView).getText().toString().toUpperCase().trim();
                            callback.onSelected(displayKey);
                        }
                        dialog.dismiss();
                    });
                }
            }
        }
    }

    public void show() {
        dialog.show();
    }
}
