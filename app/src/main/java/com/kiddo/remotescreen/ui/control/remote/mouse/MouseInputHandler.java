package com.kiddo.remotescreen.ui.control.remote.mouse;

import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

import com.kiddo.remotescreen.util.webrtc.WebRtcManager;

public class MouseInputHandler implements View.OnTouchListener {
    private final WebRtcManager rtc;
    private boolean isSingleFingerDown, isMoving, isHolding, isScrolling, isRightClick;
    private long singleTouchStartTime, firstFingerDownTime;
    private int startX, startY, firstFingerX, firstFingerY;
    private Float initialTwoFingerY = null;

    private static final long HOLD_DELAY_MS = 800;
    private static final float MOVE_THRESHOLD = 20f;
    private static final float SCROLL_THRESHOLD = 30f;

    public MouseInputHandler(WebRtcManager rtcManager) {
        this.rtc = rtcManager;
    }

    @Override
    public boolean onTouch(@NonNull View v, @NonNull MotionEvent event) {
        if (!rtc.isDataChannelReady()) return false;

        int pointerCount = event.getPointerCount();
        int action = event.getActionMasked();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                isSingleFingerDown = true;
                isMoving = false;
                isHolding = false;
                singleTouchStartTime = System.currentTimeMillis();
                firstFingerDownTime = singleTouchStartTime;
                startX = (int) event.getX();
                startY = (int) event.getY();
                firstFingerX = startX;
                firstFingerY = startY;

                v.postDelayed(() -> {
                    if (isSingleFingerDown && !isMoving) {
                        rtc.sendMouseCommand(startX, startY, MouseAction.PRESS);
                        isHolding = true;
                    }
                }, HOLD_DELAY_MS);
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                if (pointerCount == 2) {
                    long interval = System.currentTimeMillis() - firstFingerDownTime;
                    if (interval < 50) {
                        isScrolling = true;
                        initialTwoFingerY = (event.getY(0) + event.getY(1)) / 2;
                    } else {
                        isRightClick = true;
                        rtc.sendMouseCommand(firstFingerX, firstFingerY, MouseAction.RIGHT_CLICK);
                    }
                    isSingleFingerDown = false;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (pointerCount == 1 && isSingleFingerDown) {
                    int currX = (int) event.getX();
                    int currY = (int) event.getY();
                    float distance = (float) Math.hypot(currX - startX, currY - startY);
                    if (distance > MOVE_THRESHOLD) {
                        isMoving = true;
                        rtc.sendMouseCommand(currX, currY, MouseAction.MOVE);
                    }
                } else if (pointerCount == 2 && isScrolling && initialTwoFingerY != null) {
                    float currentY = (event.getY(0) + event.getY(1)) / 2;
                    float deltaY = currentY - initialTwoFingerY;
                    if (Math.abs(deltaY) > SCROLL_THRESHOLD) {
                        String direction = deltaY > 0 ? MouseAction.SCROLL_DOWN : MouseAction.SCROLL_UP;
                        int avgX = (int) ((event.getX(0) + event.getX(1)) / 2);
                        int avgY = (int) ((event.getY(0) + event.getY(1)) / 2);
                        rtc.sendMouseCommand(avgX, avgY, direction);
                        initialTwoFingerY = currentY;
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
                int x = (int) event.getX();
                int y = (int) event.getY();
                if (isHolding) {
                    rtc.sendMouseCommand(x, y, MouseAction.RELEASE);
                } else if (!isMoving && (System.currentTimeMillis() - singleTouchStartTime) < 1000) {
                    rtc.sendMouseCommand(x, y, MouseAction.PRESS);
                    rtc.sendMouseCommand(x, y, MouseAction.RELEASE);
                }
                reset();
                break;

            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL:
                reset();
                break;
        }
        return true;
    }

    private void reset() {
        isSingleFingerDown = false;
        isMoving = false;
        isHolding = false;
        isScrolling = false;
        isRightClick = false;
        initialTwoFingerY = null;
    }
}
