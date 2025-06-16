package com.example.moneydealer;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class OnSwipeTouchListener implements View.OnTouchListener {

    private final GestureDetector gestureDetector;
    private final OnSwipeListener swipeListener;

    public OnSwipeTouchListener(Context context, OnSwipeListener listener) {
        gestureDetector = new GestureDetector(context, new GestureListener());
        this.swipeListener = listener;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            swipeListener.onSwipeRight();
                        } else {
                            swipeListener.onSwipeLeft();
                        }
                        result = true;
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
        }
    }

    public interface OnSwipeListener {
        default void onSwipeRight() {}
        default void onSwipeLeft() {}
    }
} 