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
        void onSelected(String mouseAction);
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

        btnLeft.setOnClickListener(v -> {
            if (callback != null) callback.onSelected("mouse_press"); // giữ trái
            dialog.dismiss();
        });

        btnMiddle.setOnClickListener(v -> {
//            if (callback != null) callback.onSelected("MIDDLE_CLICK");
//            dialog.dismiss();
        });

        btnRight.setOnClickListener(v -> {
            if (callback != null) callback.onSelected("right_click"); // click phải
            dialog.dismiss();
        });

        // Optional: Nếu bạn muốn thêm các chức năng nâng cao
        // như MOUSE_PRESS, MOUSE_RELEASE, SCROLL_UP, SCROLL_DOWN
        // bạn có thể thêm thêm nút dưới dạng:
        //
        // Button btnScrollUp = dialog.findViewById(R.id.btnMouseScrollUp);
        // btnScrollUp.setOnClickListener(v -> {
        //     if (callback != null) callback.onSelected("SCROLL_UP");
        //     dialog.dismiss();
        // });
    }

    public void show() {
        dialog.show();
    }
}
