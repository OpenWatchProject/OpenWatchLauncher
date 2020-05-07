package com.openwatchproject.launcher.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

public class HorizontalViewPager extends ViewPager {
    private static final String TAG = "HorizontalViewPager";

    private VerticalViewPager verticalViewPager;

    public HorizontalViewPager(@NonNull Context context) {
        super(context);
        init();
    }

    public HorizontalViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        verticalViewPager = null;
        // Get rid of the overscroll effect
        setOverScrollMode(View.OVER_SCROLL_NEVER);
        setOffscreenPageLimit(2);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

    }

    public void setVerticalViewPager(VerticalViewPager verticalViewPager) {
        if (this.verticalViewPager != null) {
            Log.w(TAG, "setVerticalViewPager: VerticalViewPager was already set! Skipping...");
            return;
        }

        this.verticalViewPager = verticalViewPager;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return (verticalViewPager == null || verticalViewPager.getCurrentItem() == 1)
                && super.onInterceptTouchEvent(ev);
    }

    public void setPage(int horizontal, int vertical) {
        if (getCurrentItem() != horizontal) {
            setCurrentItem(horizontal);
        }

        if (verticalViewPager.getCurrentItem() != vertical) {
            verticalViewPager.setCurrentItem(vertical);
        }
    }

    public int getCurrentVerticalItem() {
        return verticalViewPager.getCurrentItem();
    }
}
