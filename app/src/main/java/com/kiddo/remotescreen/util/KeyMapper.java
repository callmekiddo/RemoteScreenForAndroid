package com.kiddo.remotescreen.util;

import android.annotation.SuppressLint;
import android.view.KeyEvent;
import com.kiddo.remotescreen.R;

import java.util.HashMap;
import java.util.Map;

public class KeyMapper {

    private static final Map<Integer, Integer> idToKeyCode = new HashMap<>();

    static {
        // Hàng F1–F12
        idToKeyCode.put(R.id.key_esc, KeyEvent.KEYCODE_ESCAPE);
        idToKeyCode.put(R.id.key_f1, KeyEvent.KEYCODE_F1);
        idToKeyCode.put(R.id.key_f2, KeyEvent.KEYCODE_F2);
        idToKeyCode.put(R.id.key_f3, KeyEvent.KEYCODE_F3);
        idToKeyCode.put(R.id.key_f4, KeyEvent.KEYCODE_F4);
        idToKeyCode.put(R.id.key_f5, KeyEvent.KEYCODE_F5);
        idToKeyCode.put(R.id.key_f6, KeyEvent.KEYCODE_F6);
        idToKeyCode.put(R.id.key_f7, KeyEvent.KEYCODE_F7);
        idToKeyCode.put(R.id.key_f8, KeyEvent.KEYCODE_F8);
        idToKeyCode.put(R.id.key_f9, KeyEvent.KEYCODE_F9);
        idToKeyCode.put(R.id.key_f10, KeyEvent.KEYCODE_F10);
        idToKeyCode.put(R.id.key_f11, KeyEvent.KEYCODE_F11);
        idToKeyCode.put(R.id.key_f12, KeyEvent.KEYCODE_F12);

        // Hàng số và ký hiệu
        idToKeyCode.put(R.id.key_backtick, KeyEvent.KEYCODE_GRAVE);
        idToKeyCode.put(R.id.key_1, KeyEvent.KEYCODE_1);
        idToKeyCode.put(R.id.key_2, KeyEvent.KEYCODE_2);
        idToKeyCode.put(R.id.key_3, KeyEvent.KEYCODE_3);
        idToKeyCode.put(R.id.key_4, KeyEvent.KEYCODE_4);
        idToKeyCode.put(R.id.key_5, KeyEvent.KEYCODE_5);
        idToKeyCode.put(R.id.key_6, KeyEvent.KEYCODE_6);
        idToKeyCode.put(R.id.key_7, KeyEvent.KEYCODE_7);
        idToKeyCode.put(R.id.key_8, KeyEvent.KEYCODE_8);
        idToKeyCode.put(R.id.key_9, KeyEvent.KEYCODE_9);
        idToKeyCode.put(R.id.key_0, KeyEvent.KEYCODE_0);
        idToKeyCode.put(R.id.key_hyphen, KeyEvent.KEYCODE_MINUS);
        idToKeyCode.put(R.id.key_equal, KeyEvent.KEYCODE_EQUALS);
        idToKeyCode.put(R.id.key_backspace, KeyEvent.KEYCODE_DEL);

        // QWERTY rows
        idToKeyCode.put(R.id.key_tab, KeyEvent.KEYCODE_TAB);
        idToKeyCode.put(R.id.key_q, KeyEvent.KEYCODE_Q);
        idToKeyCode.put(R.id.key_w, KeyEvent.KEYCODE_W);
        idToKeyCode.put(R.id.key_e, KeyEvent.KEYCODE_E);
        idToKeyCode.put(R.id.key_r, KeyEvent.KEYCODE_R);
        idToKeyCode.put(R.id.key_t, KeyEvent.KEYCODE_T);
        idToKeyCode.put(R.id.key_y, KeyEvent.KEYCODE_Y);
        idToKeyCode.put(R.id.key_u, KeyEvent.KEYCODE_U);
        idToKeyCode.put(R.id.key_i, KeyEvent.KEYCODE_I);
        idToKeyCode.put(R.id.key_o, KeyEvent.KEYCODE_O);
        idToKeyCode.put(R.id.key_p, KeyEvent.KEYCODE_P);
        idToKeyCode.put(R.id.key_open_bracket, KeyEvent.KEYCODE_LEFT_BRACKET);
        idToKeyCode.put(R.id.key_close_bracket, KeyEvent.KEYCODE_RIGHT_BRACKET);
        idToKeyCode.put(R.id.key_backslash, KeyEvent.KEYCODE_BACKSLASH);

        idToKeyCode.put(R.id.key_caps_lock, KeyEvent.KEYCODE_CAPS_LOCK);
        idToKeyCode.put(R.id.key_a, KeyEvent.KEYCODE_A);
        idToKeyCode.put(R.id.key_s, KeyEvent.KEYCODE_S);
        idToKeyCode.put(R.id.key_d, KeyEvent.KEYCODE_D);
        idToKeyCode.put(R.id.key_f, KeyEvent.KEYCODE_F);
        idToKeyCode.put(R.id.key_g, KeyEvent.KEYCODE_G);
        idToKeyCode.put(R.id.key_h, KeyEvent.KEYCODE_H);
        idToKeyCode.put(R.id.key_j, KeyEvent.KEYCODE_J);
        idToKeyCode.put(R.id.key_k, KeyEvent.KEYCODE_K);
        idToKeyCode.put(R.id.key_l, KeyEvent.KEYCODE_L);
        idToKeyCode.put(R.id.key_semicolon, KeyEvent.KEYCODE_SEMICOLON);
        idToKeyCode.put(R.id.key_apostrophe, KeyEvent.KEYCODE_APOSTROPHE);
        idToKeyCode.put(R.id.key_enter, KeyEvent.KEYCODE_ENTER);

        idToKeyCode.put(R.id.key_shift_left, KeyEvent.KEYCODE_SHIFT_LEFT);
        idToKeyCode.put(R.id.key_z, KeyEvent.KEYCODE_Z);
        idToKeyCode.put(R.id.key_x, KeyEvent.KEYCODE_X);
        idToKeyCode.put(R.id.key_c, KeyEvent.KEYCODE_C);
        idToKeyCode.put(R.id.key_v, KeyEvent.KEYCODE_V);
        idToKeyCode.put(R.id.key_b, KeyEvent.KEYCODE_B);
        idToKeyCode.put(R.id.key_n, KeyEvent.KEYCODE_N);
        idToKeyCode.put(R.id.key_m, KeyEvent.KEYCODE_M);
        idToKeyCode.put(R.id.key_comma, KeyEvent.KEYCODE_COMMA);
        idToKeyCode.put(R.id.key_dot, KeyEvent.KEYCODE_PERIOD);
        idToKeyCode.put(R.id.key_slash, KeyEvent.KEYCODE_SLASH);
        idToKeyCode.put(R.id.key_shift_right, KeyEvent.KEYCODE_SHIFT_RIGHT);

        // Control row
        idToKeyCode.put(R.id.key_control_left, KeyEvent.KEYCODE_CTRL_LEFT);
        idToKeyCode.put(R.id.key_window, KeyEvent.KEYCODE_WINDOW);
        idToKeyCode.put(R.id.key_alt_left, KeyEvent.KEYCODE_ALT_LEFT);
        idToKeyCode.put(R.id.key_space, KeyEvent.KEYCODE_SPACE);
        idToKeyCode.put(R.id.key_alt_right, KeyEvent.KEYCODE_ALT_RIGHT);
        idToKeyCode.put(R.id.key_control_right, KeyEvent.KEYCODE_CTRL_RIGHT);

        // Arrow keys
        idToKeyCode.put(R.id.key_arrow_up, KeyEvent.KEYCODE_DPAD_UP);
        idToKeyCode.put(R.id.key_arrow_down, KeyEvent.KEYCODE_DPAD_DOWN);
        idToKeyCode.put(R.id.key_arrow_left, KeyEvent.KEYCODE_DPAD_LEFT);
        idToKeyCode.put(R.id.key_arrow_right, KeyEvent.KEYCODE_DPAD_RIGHT);
    }

    public static boolean isMapped(int viewId) {
        return idToKeyCode.containsKey(viewId);
    }

    @SuppressLint("NewApi")
    public static int getKeyCode(int viewId) {
        return idToKeyCode.getOrDefault(viewId, -1);
    }
}
