package com.openwatchproject.launcher.notification.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.openwatchproject.launcher.R;
import com.openwatchproject.launcher.notification.NotiDrawer;

public class NotificationHeaderView extends ConstraintLayout {
    public static final int NO_COLOR = NotiDrawer.COLOR_INVALID;
    private final int mChildMinWidth;
    private final int mContentEndMargin;
    private final int mGravity;
    private View mAppName;
    private View mHeaderText;
    private View mSecondaryHeaderText;
    private ImageView mIcon;
    private View mProfileBadge;
    private int mIconColor;
    private int mOriginalNotificationColor;
    private boolean mShowWorkBadgeAtEnd;
    private Drawable mBackground;
    private int mTotalWidth;

    public NotificationHeaderView(Context context) {
        this(context, null);
    }

    public NotificationHeaderView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NotificationHeaderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Resources res = getResources();
        mChildMinWidth = res.getDimensionPixelSize(R.dimen.notification_header_shrink_min_width);
        mContentEndMargin = res.getDimensionPixelSize(R.dimen.notification_content_margin_end);
        int[] attrIds = {android.R.attr.gravity};
        TypedArray ta = context.obtainStyledAttributes(attrs, attrIds, defStyleAttr, 0);
        mGravity = ta.getInt(0, 0);
        ta.recycle();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mAppName = findViewById(R.id.app_name_text);
        mHeaderText = findViewById(R.id.header_text);
        mSecondaryHeaderText = findViewById(R.id.header_text_secondary);
        mIcon = findViewById(R.id.icon);
        mProfileBadge = findViewById(R.id.profile_badge);
    }

    /**
     * Set a {@link Drawable} to be displayed as a background on the header.
     */
    public void setHeaderBackgroundDrawable(Drawable drawable) {
        if (drawable != null) {
            setWillNotDraw(false);
            mBackground = drawable;
            mBackground.setCallback(this);
        } else {
            setWillNotDraw(true);
            mBackground = null;
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mBackground != null) {
            mBackground.setBounds(0, 0, getWidth(), getHeight());
            mBackground.draw(canvas);
        }
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        return super.verifyDrawable(who) || who == mBackground;
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (mBackground != null && mBackground.isStateful()) {
            mBackground.setState(getDrawableState());
        }
    }

    public int getOriginalIconColor() {
        return mIconColor;
    }

    public void setOriginalIconColor(int color) {
        mIconColor = color;
    }

    public int getOriginalNotificationColor() {
        return mOriginalNotificationColor;
    }

    public void setOriginalNotificationColor(int color) {
        mOriginalNotificationColor = color;
    }

    public void setShowWorkBadgeAtEnd(boolean showWorkBadgeAtEnd) {
        if (showWorkBadgeAtEnd != mShowWorkBadgeAtEnd) {
            setClipToPadding(!showWorkBadgeAtEnd);
            mShowWorkBadgeAtEnd = showWorkBadgeAtEnd;
        }
    }

    public View getWorkProfileIcon() {
        return mProfileBadge;
    }

    public ImageView getIcon() {
        return mIcon;
    }

    private View getFirstChildNotGone() {
        for (int i = 0; i < getChildCount(); i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                return child;
            }
        }
        return this;
    }
}
