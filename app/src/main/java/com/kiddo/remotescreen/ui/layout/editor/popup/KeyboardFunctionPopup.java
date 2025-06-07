package com.kiddo.remotescreen.ui.layout.editor.popup;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.kiddo.remotescreen.R;

import java.util.function.Consumer;

public class KeyboardFunctionPopup {

    private final Dialog dialog;

    @SuppressLint("NewApi")
    public KeyboardFunctionPopup(Activity activity, Consumer<View> onKeySelected) {
        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.view_keyboard_function_selector);
        dialog.setCancelable(true);

        ImageButton btnClose = dialog.findViewById(R.id.btnCloseKeyboardPopup);
        btnClose.setOnClickListener(v -> dialog.dismiss());

        LinearLayout root = dialog.findViewById(R.id.linear_keyboard);

        traverseAndBindKeys(root, onKeySelected);
    }

    @SuppressLint("NewApi")
    private void traverseAndBindKeys(View view, Consumer<View> onKeySelected) {
        if (view instanceof Button) {
            view.setOnClickListener(v -> {
                onKeySelected.accept(v);
                dialog.dismiss();
            });
        } else if (view instanceof ViewGroup group) {
            for (int i = 0; i < group.getChildCount(); i++) {
                traverseAndBindKeys(group.getChildAt(i), onKeySelected);
            }
        }
    }

    public void show() {
        dialog.show();
    }
}
