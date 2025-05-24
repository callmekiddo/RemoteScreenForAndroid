package com.kiddo.remotescreen.ui.control.remote.mouse;

import android.os.Handler;
import android.view.MotionEvent;

import com.kiddo.remotescreen.util.webrtc.WebRtcManager;

public class MouseInputHandler {
    private final WebRtcManager rtc;

    private boolean isHolding = false;
    private boolean rightClickTriggered = false;
    private boolean scrollMode = false;
    private boolean hasMoved = false;

    private long downTime = 0;
    private float downX = 0, downY = 0;
    private Float initialScrollY = null;

    private static final long HOLD_THRESHOLD = 400;
    private static final float MOVE_THRESHOLD = 20f;
    private static final long SCROLL_THRESHOLD_TIME = 150;
    private static final float SCROLL_MOVE_THRESHOLD = 30f;

    private final Handler handler = new Handler();
    private Runnable holdRunnable;

    public MouseInputHandler(WebRtcManager rtcManager) {
        this.rtc = rtcManager;
    }

    public void onTouch(MotionEvent event, int videoWidth, int videoHeight, int viewWidth, int viewHeight) {
        int pointerCount = event.getPointerCount();
        int action = event.getActionMasked();

        switch (action) {
            case MotionEvent.ACTION_DOWN -> {
                reset();
                downTime = System.currentTimeMillis();
                downX = event.getX(0);
                downY = event.getY(0);
                hasMoved = false;

                holdRunnable = () -> {
                    if (!hasMoved && !scrollMode) {
                        int[] pos = mapToRemoteXY(downX, downY, videoWidth, videoHeight, viewWidth, viewHeight);
                        rtc.sendMouseCommand(pos[0], pos[1], MouseAction.PRESS);
                        isHolding = true;
                    }
                };
                handler.postDelayed(holdRunnable, HOLD_THRESHOLD);
            }

            case MotionEvent.ACTION_MOVE -> {
                float dx = event.getX(0) - downX;
                float dy = event.getY(0) - downY;
                float distance = (float) Math.hypot(dx, dy);

                if (!hasMoved && distance > MOVE_THRESHOLD) {
                    hasMoved = true;
                    handler.removeCallbacks(holdRunnable);
                }

                if (hasMoved && !scrollMode && !isHolding) {
                    int[] pos = mapToRemoteXY(event.getX(0), event.getY(0), videoWidth, videoHeight, viewWidth, viewHeight);
                    rtc.sendMouseCommand(pos[0], pos[1], MouseAction.MOVE);
                }

                if (scrollMode && pointerCount == 2 && initialScrollY != null) {
                    float y0 = event.getY(0);
                    float y1 = event.getY(1);
                    float currentY = (y0 + y1) / 2;
                    float deltaY = currentY - initialScrollY;

                    if (Math.abs(deltaY) > SCROLL_MOVE_THRESHOLD) {
                        String direction = deltaY > 0 ? MouseAction.SCROLL_UP : MouseAction.SCROLL_DOWN;

                        float x0 = event.getX(0);
                        float x1 = event.getX(1);
                        float avgX = (x0 + x1) / 2;
                        float avgY = (y0 + y1) / 2;

                        int[] pos = mapToRemoteXY(avgX, avgY, videoWidth, videoHeight, viewWidth, viewHeight);
                        rtc.sendMouseCommand(pos[0], pos[1], direction);
                        initialScrollY = currentY;
                    }
                }
            }

            case MotionEvent.ACTION_POINTER_DOWN -> {
                if (pointerCount == 2) {
                    long delay = System.currentTimeMillis() - downTime;
                    float y0 = event.getY(0), y1 = event.getY(1);

                    if (delay <= SCROLL_THRESHOLD_TIME) {
                        scrollMode = true;
                        initialScrollY = (y0 + y1) / 2;
                        handler.removeCallbacks(holdRunnable);
                    } else if (!rightClickTriggered) {
                        int[] pos = mapToRemoteXY(downX, downY, videoWidth, videoHeight, viewWidth, viewHeight);
                        rtc.sendMouseCommand(pos[0], pos[1], MouseAction.RIGHT_CLICK);
                        rightClickTriggered = true;
                        handler.removeCallbacks(holdRunnable);
                    }
                }
            }

            case MotionEvent.ACTION_UP -> {
                handler.removeCallbacks(holdRunnable);
                int[] pos = mapToRemoteXY(event.getX(0), event.getY(0), videoWidth, videoHeight, viewWidth, viewHeight);

                if (rightClickTriggered) {
                    // Do nothing
                } else if (isHolding) {
                    rtc.sendMouseCommand(pos[0], pos[1], MouseAction.RELEASE);
                } else if (!hasMoved) {
                    rtc.sendMouseCommand(pos[0], pos[1], MouseAction.PRESS);
                    rtc.sendMouseCommand(pos[0], pos[1], MouseAction.RELEASE);
                }

                reset();
            }

            case MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_POINTER_UP -> {
                handler.removeCallbacks(holdRunnable);
                reset();
            }
        }
    }

    private void reset() {
        isHolding = false;
        hasMoved = false;
        rightClickTriggered = false;
        scrollMode = false;
        initialScrollY = null;
        downTime = 0;
    }

    private int[] mapToRemoteXY(float rawX, float rawY, int videoWidth, int videoHeight, int viewWidth, int viewHeight) {
        float[] mapped = mapTouchToVideo(rawX, rawY, videoWidth, videoHeight, viewWidth, viewHeight);
        return new int[]{(int) mapped[0], (int) mapped[1]};
    }

    private float[] mapTouchToVideo(float rawX, float rawY, int videoWidth, int videoHeight, int viewWidth, int viewHeight) {
        float aspectRatio = (float) videoWidth / videoHeight;
        float viewRatio = (float) viewWidth / viewHeight;

        float displayWidth, displayHeight;
        if (aspectRatio > viewRatio) {
            displayWidth = viewWidth;
            displayHeight = viewWidth / aspectRatio;
        } else {
            displayHeight = viewHeight;
            displayWidth = viewHeight * aspectRatio;
        }

        float offsetX = (viewWidth - displayWidth) / 2f;
        float offsetY = (viewHeight - displayHeight) / 2f;

        float relX = rawX - offsetX;
        float relY = rawY - offsetY;

        relX = Math.max(0, Math.min(relX, displayWidth));
        relY = Math.max(0, Math.min(relY, displayHeight));

        float normX = relX / displayWidth;
        float normY = relY / displayHeight;

        return new float[]{normX * videoWidth, normY * videoHeight};
    }
}
