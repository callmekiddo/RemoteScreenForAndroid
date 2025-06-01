package com.kiddo.remotescreen.util;

import android.content.Context;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.LinearLayout;

import com.kiddo.remotescreen.R;
import com.kiddo.remotescreen.ui.control.remote.keyboard.KeyboardInputHandler;
import com.kiddo.remotescreen.util.webrtc.WebRtcManager;

public class CustomKeyboardView extends LinearLayout {

    private WebRtcManager rtc;
    private KeyboardInputHandler keyboardHandler;
    private Runnable onSymbolModeRequested;

    public CustomKeyboardView(Context context) {
        super(context);
        init(context);
    }

    public CustomKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CustomKeyboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_keyboard, this, true);
    }

    public void setRtcManager(WebRtcManager rtcManager) {
        this.rtc = rtcManager;
        this.keyboardHandler = new KeyboardInputHandler(rtcManager);
        setupKeys();
    }

    private void setupKeys() {
        setupNumberRow();

        // Modifier keys
        keyboardHandler.registerModifierButton(KeyEvent.KEYCODE_SHIFT_LEFT, findViewById(R.id.btn_shift_left));
        keyboardHandler.registerModifierButton(KeyEvent.KEYCODE_SHIFT_RIGHT, findViewById(R.id.btn_shift_right));
        keyboardHandler.registerModifierButton(KeyEvent.KEYCODE_CTRL_LEFT, findViewById(R.id.btn_ctrl_left));
        keyboardHandler.registerModifierButton(KeyEvent.KEYCODE_ALT_LEFT, findViewById(R.id.btn_alt_left));
        keyboardHandler.registerModifierButton(KeyEvent.KEYCODE_WINDOW, findViewById(R.id.btn_win));

        // Alphanumeric keys (holdable)
        int[][] alphaKeys = {
                {R.id.btn_q, KeyEvent.KEYCODE_Q}, {R.id.btn_w, KeyEvent.KEYCODE_W}, {R.id.btn_e, KeyEvent.KEYCODE_E},
                {R.id.btn_r, KeyEvent.KEYCODE_R}, {R.id.btn_t, KeyEvent.KEYCODE_T}, {R.id.btn_y, KeyEvent.KEYCODE_Y},
                {R.id.btn_u, KeyEvent.KEYCODE_U}, {R.id.btn_i, KeyEvent.KEYCODE_I}, {R.id.btn_o, KeyEvent.KEYCODE_O},
                {R.id.btn_p, KeyEvent.KEYCODE_P}, {R.id.btn_a, KeyEvent.KEYCODE_A}, {R.id.btn_s, KeyEvent.KEYCODE_S},
                {R.id.btn_d, KeyEvent.KEYCODE_D}, {R.id.btn_f, KeyEvent.KEYCODE_F}, {R.id.btn_g, KeyEvent.KEYCODE_G},
                {R.id.btn_h, KeyEvent.KEYCODE_H}, {R.id.btn_j, KeyEvent.KEYCODE_J}, {R.id.btn_k, KeyEvent.KEYCODE_K},
                {R.id.btn_l, KeyEvent.KEYCODE_L}, {R.id.btn_z, KeyEvent.KEYCODE_Z}, {R.id.btn_x, KeyEvent.KEYCODE_X},
                {R.id.btn_c, KeyEvent.KEYCODE_C}, {R.id.btn_v, KeyEvent.KEYCODE_V}, {R.id.btn_b, KeyEvent.KEYCODE_B},
                {R.id.btn_n, KeyEvent.KEYCODE_N}, {R.id.btn_m, KeyEvent.KEYCODE_M}
        };
        for (int[] key : alphaKeys) {
            keyboardHandler.registerHoldableKey(key[1], findViewById(key[0]));
        }

        // Special keys
        int[][] clickKeys = {
                {R.id.btn_enter, KeyEvent.KEYCODE_ENTER},
                {R.id.btn_backspace, KeyEvent.KEYCODE_DEL},
                {R.id.btn_tab, KeyEvent.KEYCODE_TAB},
                {R.id.btn_space, KeyEvent.KEYCODE_SPACE},
                {R.id.btn_caps, KeyEvent.KEYCODE_CAPS_LOCK}
        };
        for (int[] key : clickKeys) {
            keyboardHandler.registerClickKey(key[1], findViewById(key[0]));
        }

        int[][] holdableSpecials = {
                {R.id.btn_esc, KeyEvent.KEYCODE_ESCAPE},
                {R.id.btn_arrow_left, KeyEvent.KEYCODE_DPAD_LEFT},
                {R.id.btn_arrow_up, KeyEvent.KEYCODE_DPAD_UP},
                {R.id.btn_arrow_down, KeyEvent.KEYCODE_DPAD_DOWN},
                {R.id.btn_arrow_right, KeyEvent.KEYCODE_DPAD_RIGHT},
                {R.id.btn_backtick, KeyEvent.KEYCODE_GRAVE},
                {R.id.btn_minus, KeyEvent.KEYCODE_MINUS},
                {R.id.btn_equal, KeyEvent.KEYCODE_EQUALS},
                {R.id.btn_left_bracket, KeyEvent.KEYCODE_LEFT_BRACKET},
                {R.id.btn_right_bracket, KeyEvent.KEYCODE_RIGHT_BRACKET},
                {R.id.btn_backslash, KeyEvent.KEYCODE_BACKSLASH},
                {R.id.btn_semicolon, KeyEvent.KEYCODE_SEMICOLON},
                {R.id.btn_apostrophe, KeyEvent.KEYCODE_APOSTROPHE},
                {R.id.btn_comma, KeyEvent.KEYCODE_COMMA},
                {R.id.btn_dot, KeyEvent.KEYCODE_PERIOD},
                {R.id.btn_slash, KeyEvent.KEYCODE_SLASH},
                {R.id.btn_f1, KeyEvent.KEYCODE_F1}, {R.id.btn_f2, KeyEvent.KEYCODE_F2}, {R.id.btn_f3, KeyEvent.KEYCODE_F3},
                {R.id.btn_f4, KeyEvent.KEYCODE_F4}, {R.id.btn_f5, KeyEvent.KEYCODE_F5}, {R.id.btn_f6, KeyEvent.KEYCODE_F6},
                {R.id.btn_f7, KeyEvent.KEYCODE_F7}, {R.id.btn_f8, KeyEvent.KEYCODE_F8}, {R.id.btn_f9, KeyEvent.KEYCODE_F9},
                {R.id.btn_f10, KeyEvent.KEYCODE_F10}, {R.id.btn_f11, KeyEvent.KEYCODE_F11}, {R.id.btn_f12, KeyEvent.KEYCODE_F12}
        };
        for (int[] key : holdableSpecials) {
            keyboardHandler.registerHoldableKey(key[1], findViewById(key[0]));
        }

        // Set dual labels for keys
        setupDualLabels();
    }

    private void setupNumberRow() {
        int[] ids = {
                R.id.btn_1, R.id.btn_2, R.id.btn_3, R.id.btn_4, R.id.btn_5,
                R.id.btn_6, R.id.btn_7, R.id.btn_8, R.id.btn_9, R.id.btn_0
        };
        int[] codes = {
                KeyEvent.KEYCODE_1, KeyEvent.KEYCODE_2, KeyEvent.KEYCODE_3, KeyEvent.KEYCODE_4, KeyEvent.KEYCODE_5,
                KeyEvent.KEYCODE_6, KeyEvent.KEYCODE_7, KeyEvent.KEYCODE_8, KeyEvent.KEYCODE_9, KeyEvent.KEYCODE_0
        };
        String[] mains = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0"};
        String[] hints = {"!", "@", "#", "$", "%", "^", "&", "*", "(", ")"};

        for (int i = 0; i < ids.length; i++) {
            keyboardHandler.registerHoldableKey(codes[i], findViewById(ids[i]));
            setDualLabel(ids[i], mains[i], hints[i]);
        }
    }

    private void setupDualLabels() {
        setDualLabel(R.id.btn_backtick, "`", "~");
        setDualLabel(R.id.btn_minus, "-", "_");
        setDualLabel(R.id.btn_equal, "=", "+");
        setDualLabel(R.id.btn_left_bracket, "[", "{");
        setDualLabel(R.id.btn_right_bracket, "]", "}");
        setDualLabel(R.id.btn_backslash, "\\", "|");
        setDualLabel(R.id.btn_semicolon, ";", ":");
        setDualLabel(R.id.btn_apostrophe, "'", "\"");
        setDualLabel(R.id.btn_comma, ",", "<");
        setDualLabel(R.id.btn_dot, ".", ">");
        setDualLabel(R.id.btn_slash, "/", "?");
    }

    private void setDualLabel(int viewId, String mainChar, String hintChar) {
        Button button = findViewById(viewId);
        if (button == null) return;

        SpannableString styled = new SpannableString(hintChar + "\n" + mainChar);

        float hintScale = 1.0f;
        float mainScale = 1.0f;
        styled.setSpan(new RelativeSizeSpan(hintScale), 0, hintChar.length(), 0);
        styled.setSpan(new RelativeSizeSpan(mainScale), hintChar.length() + 1, styled.length(), 0);

        button.setText(styled);
        button.setTextSize(9);
        button.setSingleLine(false);
        button.setMaxLines(2);
        button.setIncludeFontPadding(false);
        button.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        button.setLineSpacing(-5f, 1f);
        button.setPadding(0, 0, 0, 0);

        float density = button.getResources().getDisplayMetrics().density;
        float offsetDp = -5f;
        button.setTranslationY(offsetDp * density);
    }

    public KeyboardInputHandler getKeyboardHandler() {
        return keyboardHandler;
    }
}
