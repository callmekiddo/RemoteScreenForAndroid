package com.kiddo.remotescreen.ui.layout.editor.popup;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;

import com.kiddo.remotescreen.R;

public class MouseFunctionPopup {

    public interface OnMouseFunctionSelected {
        void onSelected(String mouseButton); // e.g. "LEFT", "RIGHT", "MIDDLE"
    }

    private final Dialog dialog;

    public MouseFunctionPopup(Activity activity, OnMouseFunctionSelected callback) {
        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.view_mouse_function_selector);
        dialog.setCancelable(true);

        // Close button
        ImageButton btnClose = dialog.findViewById(R.id.btnCloseMousePopup);
        btnClose.setOnClickListener(v -> dialog.dismiss());

        // Mouse buttons
        Button btnLeft = dialog.findViewById(R.id.btnMouseLeft);
        Button btnMiddle = dialog.findViewById(R.id.btnMouseMiddle);
        Button btnRight = dialog.findViewById(R.id.btnMouseRight);

        View.OnClickListener clickListener = v -> {
            String value = null;
            if (v.getId() == R.id.btnMouseLeft) value = "Left";
            else if (v.getId() == R.id.btnMouseMiddle) value = "Middle";
            else if (v.getId() == R.id.btnMouseRight) value = "Right";

            if (value != null && callback != null) {
                callback.onSelected(value);
            }

            dialog.dismiss();
        };

        btnLeft.setOnClickListener(clickListener);
        btnMiddle.setOnClickListener(clickListener);
        btnRight.setOnClickListener(clickListener);
    }

    public void show() {
        dialog.show();
    }
}
