package com.kiddo.remotescreen.util;

import android.content.Context;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatEditText;

public class SilentEditText extends AppCompatEditText {

    public SilentEditText(Context context) {
        super(context);
        init();
    }

    public SilentEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SilentEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setCursorVisible(false);       // ẩn con trỏ
        setTextIsSelectable(false);    // không cho select
        setLongClickable(false);       // không menu
        setFocusable(true);
        setFocusableInTouchMode(true);
        setInputType(android.text.InputType.TYPE_NULL); // không nhập được text
    }

    @Override
    public boolean onTextContextMenuItem(int id) {
        return false; // chặn paste/copy...
    }
}
