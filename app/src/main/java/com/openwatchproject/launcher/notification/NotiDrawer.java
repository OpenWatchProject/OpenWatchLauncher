package com.openwatchproject.launcher.notification;

import android.app.Notification;
import android.app.Person;
import android.app.RemoteInput;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.graphics.drawable.RippleDrawable;
import android.media.session.MediaSession;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.core.text.BidiFormatter;

import com.openwatchproject.launcher.R;
import com.openwatchproject.launcher.notification.view.DateTimeView;
import com.openwatchproject.launcher.notification.view.NotificationHeaderView;

import java.util.ArrayList;
import java.util.List;

import static androidx.core.graphics.ColorUtils.calculateContrast;
import static androidx.core.graphics.ColorUtils.calculateLuminance;
import static androidx.core.graphics.ColorUtils.compositeColors;
import static com.openwatchproject.launcher.notification.OpenWatchNotification.MAX_ACTION_BUTTONS;

public class NotiDrawer {
    private static final String TAG = "NotiDrawer";
    
    public static final int COLOR_DEFAULT = 0; // AKA Color.TRANSPARENT
    public static final int COLOR_INVALID = 1;

    private final OpenWatchNotification mN;

    public static final String EXTRA_REBUILD_CONTENT_VIEW_ACTION_COUNT =
            "android.rebuild.contentViewActionCount";
    public static final String EXTRA_REBUILD_BIG_CONTENT_VIEW_ACTION_COUNT
            = "android.rebuild.bigViewActionCount";
    public static final String EXTRA_REBUILD_HEADS_UP_CONTENT_VIEW_ACTION_COUNT
            = "android.rebuild.hudViewActionCount";

    private static final boolean USE_ONLY_TITLE_IN_LOW_PRIORITY_SUMMARY =
            true;//SystemProperties.getBoolean("notifications.only_title", true);

    /**
     * The lightness difference that has to be added to the primary text color to obtain the
     * secondary text color when the background is light.
     */
    private static final int LIGHTNESS_TEXT_DIFFERENCE_LIGHT = 20;

    /**
     * The lightness difference that has to be added to the primary text color to obtain the
     * secondary text color when the background is dark.
     * A bit less then the above value, since it looks better on dark backgrounds.
     */
    private static final int LIGHTNESS_TEXT_DIFFERENCE_DARK = -10;

    private Context mContext;
    private Bundle mUserExtras = new Bundle();
    private Style mStyle;
    private ArrayList<Notification.Action> mActions = new ArrayList<>(MAX_ACTION_BUTTONS);
    private ArrayList<Person> mPersonList = new ArrayList<>();
    private ContrastColorUtil mColorUtil;
    private boolean mIsLegacy;
    private boolean mIsLegacyInitialized;

    /**
     * Caches a contrast-enhanced version of {@link #mCachedContrastColorIsFor}.
     */
    private int mCachedContrastColor = COLOR_INVALID;
    private int mCachedContrastColorIsFor = COLOR_INVALID;
    /**
     * Caches a ambient version of {@link #mCachedAmbientColorIsFor}.
     */
    private int mCachedAmbientColor = COLOR_INVALID;
    private int mCachedAmbientColorIsFor = COLOR_INVALID;
    /**
     * A neutral color color that can be used for icons.
     */
    private int mNeutralColor = COLOR_INVALID;

    /**
     * Caches an instance of StandardTemplateParams. Note that this may have been used before,
     * so make sure to call {@link StandardTemplateParams#reset()} before using it.
     */
    StandardTemplateParams mParams = new StandardTemplateParams();
    private int mTextColorsAreForBackground = COLOR_INVALID;
    private int mPrimaryTextColor = COLOR_INVALID;
    private int mSecondaryTextColor = COLOR_INVALID;
    private int mBackgroundColor = COLOR_INVALID;
    private int mForegroundColor = COLOR_INVALID;
    /**
     * A temporary location where actions are stored. If != null the view originally has action
     * but doesn't have any for this inflation.
     */
    private ArrayList<Notification.Action> mOriginalActions;
    private boolean mRebuildStyledRemoteViews;

    private boolean mTintActionButtons;
    private boolean mInNightMode;

    public NotiDrawer(Context context, OpenWatchNotification openWatchNotification) {
        this.mContext = context;
        this.mTintActionButtons = true;
        this.mN = openWatchNotification;
        this.mParams = new StandardTemplateParams();

        Style mStyle = null;
        if (this.mN.getTemplate() != null) {
            Log.d(TAG, "NotiDrawer: " + this.mN.getTemplate());
            switch (this.mN.getTemplate()) {
                case "android.app.Notification$BigPictureStyle":
                    mStyle = new BigPictureStyle(this);
                    break;
                case "android.app.Notification$BigTextStyle":

                    break;
                case "android.app.Notification$MessagingStyle":

                    break;
                case "android.app.Notification$InboxStyle":

                    break;
                case "android.app.Notification$MediaStyle":
                    mStyle = new MediaStyle(this);
                    break;
            }
        }
        this.mStyle = mStyle;
    }

    private void bindProfileBadge(View contentView, StandardTemplateParams p) {
        Bitmap profileBadge = mN.getProfileBadge();

        if (profileBadge != null) {
            DrawUtils.setImageViewBitmap(contentView, R.id.profile_badge, profileBadge);
            DrawUtils.setViewVisibility(contentView, R.id.profile_badge, View.VISIBLE);
            if (isColorized(p)) {
                DrawUtils.setDrawableTint(contentView, R.id.profile_badge, false,
                        getPrimaryTextColor(p), PorterDuff.Mode.SRC_ATOP);
            }
        }
    }

    private void bindAlertedIcon(View contentView, StandardTemplateParams p) {
        /*DrawUtils.setDrawableTint(contentView, 
                R.id.alerted_icon,
                false /* targetBackground *//*,
                getNeutralColor(p),
                PorterDuff.Mode.SRC_ATOP);*/
    }

    private void resetStandardTemplate(View contentView) {
        resetNotificationHeader(contentView);
        DrawUtils.setViewVisibility(contentView, R.id.right_icon, View.GONE);
        DrawUtils.setViewVisibility(contentView, R.id.title, View.GONE);
        DrawUtils.setTextViewText(contentView, R.id.title, null);
        DrawUtils.setSelected(contentView, R.id.title, true);
        DrawUtils.setViewVisibility(contentView, R.id.text, View.GONE);
        DrawUtils.setTextViewText(contentView, R.id.text, null);
        DrawUtils.setViewVisibility(contentView, R.id.text_line_1, View.GONE);
        DrawUtils.setTextViewText(contentView, R.id.text_line_1, null);
    }

    /**
     * Resets the notification header to its original state
     */
    private void resetNotificationHeader(View contentView) {
        // Small icon doesn't need to be reset, as it's always set. Resetting would prevent
        // re-using the drawable when the notification is updated.
        DrawUtils.setExpanded(contentView, R.id.notification_header, false);
        DrawUtils.setTextViewText(contentView, R.id.app_name_text, null);
        DrawUtils.setViewVisibility(contentView, R.id.chronometer, View.GONE);
        DrawUtils.setViewVisibility(contentView, R.id.header_text_layout, View.GONE);
        DrawUtils.setViewVisibility(contentView, R.id.header_text, View.GONE);
        DrawUtils.setTextViewText(contentView, R.id.header_text, null);
        DrawUtils.setViewVisibility(contentView, R.id.header_text_secondary, View.GONE);
        DrawUtils.setTextViewText(contentView, R.id.header_text_secondary, null);
        DrawUtils.setViewVisibility(contentView, R.id.header_text_divider, View.GONE);
        //DrawUtils.setViewVisibility(contentView, R.id.time_divider, View.GONE);
        DrawUtils.setViewVisibility(contentView, R.id.time, View.GONE);
        DrawUtils.setImageViewIcon(contentView, R.id.profile_badge, null);
        DrawUtils.setViewVisibility(contentView, R.id.profile_badge, View.GONE);
        //DrawUtils.setViewVisibility(contentView, R.id.alerted_icon, View.GONE);
        mN.mUsesStandardHeader = false;
    }

    private View applyStandardTemplate(int resId) {
        return applyStandardTemplate(resId, mParams.reset().fillTextsFrom(this));
    }

    private View applyStandardTemplate(int resId, StandardTemplateParams p) {
        View contentView = LayoutInflater.from(mContext).inflate(resId, null);

        resetStandardTemplate(contentView);

        updateBackgroundColor(contentView, p);
        bindNotificationHeader(contentView, p);
        bindLargeIconAndReply(contentView, p);
        boolean showProgress = handleProgressBar(contentView, mN, p);
        if (p.title != null) {
            DrawUtils.setViewVisibility(contentView, R.id.title, View.VISIBLE);
            DrawUtils.setTextViewText(contentView, R.id.title, processTextSpans(p.title));
            setTextViewColorPrimary(contentView, R.id.title, p);
        }
        if (p.text != null) {
            int textId = showProgress ? R.id.text_line_1
                    : R.id.text;
            DrawUtils.setTextViewText(contentView, textId, processTextSpans(p.text));
            setTextViewColorSecondary(contentView, textId, p);
            DrawUtils.setViewVisibility(contentView, textId, View.VISIBLE);
        }

        return contentView;
    }

    private CharSequence processTextSpans(CharSequence text) {
        if (hasForegroundColor() || mInNightMode) {
            return ContrastColorUtil.clearColorSpans(text);
        }
        return text;
    }

    private void setTextViewColorPrimary(View contentView, int id,
                                         StandardTemplateParams p) {
        ensureColors(p);
        DrawUtils.setTextColor(contentView, id, mPrimaryTextColor);
    }

    private boolean hasForegroundColor() {
        return mForegroundColor != COLOR_INVALID;
    }

    public int getPrimaryTextColor() {
        return getPrimaryTextColor(mParams);
    }

    /**
     * @param p the template params to inflate this with
     * @return the primary text color
     */
    public int getPrimaryTextColor(StandardTemplateParams p) {
        ensureColors(p);
        return mPrimaryTextColor;
    }

    /**
     * Return the secondary text color using the existing template params
     */
    public int getSecondaryTextColor() {
        return getSecondaryTextColor(mParams);
    }

    /**
     * @param p the template params to inflate this with
     * @return the secondary text color
     */
    public int getSecondaryTextColor(StandardTemplateParams p) {
        ensureColors(p);
        return mSecondaryTextColor;
    }

    private void setTextViewColorSecondary(View contentView, int id,
                                           StandardTemplateParams p) {
        ensureColors(p);
        DrawUtils.setTextColor(contentView, id, mSecondaryTextColor);
    }

    private void ensureColors(StandardTemplateParams p) {
        int backgroundColor = getBackgroundColor(p);
        if (mPrimaryTextColor == COLOR_INVALID
                || mSecondaryTextColor == COLOR_INVALID
                || mTextColorsAreForBackground != backgroundColor) {
            mTextColorsAreForBackground = backgroundColor;
            if (!hasForegroundColor() || !isColorized(p)) {
                mPrimaryTextColor = ContrastColorUtil.resolvePrimaryColor(mContext,
                        backgroundColor, mInNightMode);
                mSecondaryTextColor = ContrastColorUtil.resolveSecondaryColor(mContext,
                        backgroundColor, mInNightMode);
                if (backgroundColor != COLOR_DEFAULT && isColorized(p)) {
                    mPrimaryTextColor = ContrastColorUtil.findAlphaToMeetContrast(
                            mPrimaryTextColor, backgroundColor, 4.5);
                    mSecondaryTextColor = ContrastColorUtil.findAlphaToMeetContrast(
                            mSecondaryTextColor, backgroundColor, 4.5);
                }
            } else {
                double backLum = calculateLuminance(backgroundColor);
                double textLum = calculateLuminance(mForegroundColor);
                double contrast = calculateContrast(mForegroundColor,
                        backgroundColor);
                // We only respect the given colors if worst case Black or White still has
                // contrast
                boolean backgroundLight = backLum > textLum
                        && ContrastColorUtil.satisfiesTextContrast(backgroundColor, Color.BLACK)
                        || backLum <= textLum
                        && !ContrastColorUtil.satisfiesTextContrast(backgroundColor, Color.WHITE);
                if (contrast < 4.5f) {
                    if (backgroundLight) {
                        mSecondaryTextColor = ContrastColorUtil.findContrastColor(
                                mForegroundColor,
                                backgroundColor,
                                true /* findFG */,
                                4.5f);
                        mPrimaryTextColor = ContrastColorUtil.changeColorLightness(
                                mSecondaryTextColor, -LIGHTNESS_TEXT_DIFFERENCE_LIGHT);
                    } else {
                        mSecondaryTextColor =
                                ContrastColorUtil.findContrastColorAgainstDark(
                                        mForegroundColor,
                                        backgroundColor,
                                        true /* findFG */,
                                        4.5f);
                        mPrimaryTextColor = ContrastColorUtil.changeColorLightness(
                                mSecondaryTextColor, -LIGHTNESS_TEXT_DIFFERENCE_DARK);
                    }
                } else {
                    mPrimaryTextColor = mForegroundColor;
                    mSecondaryTextColor = ContrastColorUtil.changeColorLightness(
                            mPrimaryTextColor, backgroundLight ? LIGHTNESS_TEXT_DIFFERENCE_LIGHT
                                    : LIGHTNESS_TEXT_DIFFERENCE_DARK);
                    if (calculateContrast(mSecondaryTextColor,
                            backgroundColor) < 4.5f) {
                        // oh well the secondary is not good enough
                        if (backgroundLight) {
                            mSecondaryTextColor = ContrastColorUtil.findContrastColor(
                                    mSecondaryTextColor,
                                    backgroundColor,
                                    true /* findFG */,
                                    4.5f);
                        } else {
                            mSecondaryTextColor
                                    = ContrastColorUtil.findContrastColorAgainstDark(
                                    mSecondaryTextColor,
                                    backgroundColor,
                                    true /* findFG */,
                                    4.5f);
                        }
                        mPrimaryTextColor = ContrastColorUtil.changeColorLightness(
                                mSecondaryTextColor, backgroundLight
                                        ? -LIGHTNESS_TEXT_DIFFERENCE_LIGHT
                                        : -LIGHTNESS_TEXT_DIFFERENCE_DARK);
                    }
                }
            }
        }
    }

    private void updateBackgroundColor(View contentView,
                                       StandardTemplateParams p) {
        if (isColorized(p)) {
            DrawUtils.setBackgroundColor(contentView, R.id.status_bar_latest_event_content,
                    getBackgroundColor(p));
        } else {
            DrawUtils.setBackgroundResource(contentView, R.id.status_bar_latest_event_content,
                    0);
        }
    }

    private boolean handleProgressBar(View contentView, OpenWatchNotification n,
                                      StandardTemplateParams p) {
        /*final int max = ex.getInt(EXTRA_PROGRESS_MAX, 0);
        final int progress = ex.getInt(EXTRA_PROGRESS, 0);
        final boolean ind = ex.getBoolean(EXTRA_PROGRESS_INDETERMINATE);
        if (p.hasProgress && (max != 0 || ind)) {
            DrawUtils.setViewVisibility(contentView, R.id.progress, View.VISIBLE);
            contentView.setProgressBar(
                    R.id.progress, max, progress, ind);
            contentView.setProgressBackgroundTintList(
                    R.id.progress, ColorStateList.valueOf(mContext.getColor(
                            R.color.notification_progress_background_color)));
            if (getRawColor(p) != COLOR_DEFAULT) {
                int color = isColorized(p) ? getPrimaryTextColor(p) : resolveContrastColor(p);
                ColorStateList colorStateList = ColorStateList.valueOf(color);
                contentView.setProgressTintList(R.id.progress, colorStateList);
                contentView.setProgressIndeterminateTintList(R.id.progress, colorStateList);
            }
            return true;
        } else {
            DrawUtils.setViewVisibility(contentView, R.id.progress, View.GONE);
            return false;
        }*/return false;
    }

    private void bindLargeIconAndReply(View contentView, StandardTemplateParams p) {
        boolean largeIconShown = bindLargeIcon(contentView, p);
        boolean replyIconShown = bindReplyIcon(contentView, p, largeIconShown);
        boolean iconContainerVisible = largeIconShown || replyIconShown;
        DrawUtils.setViewVisibility(contentView, R.id.right_icon_container,
                iconContainerVisible ? View.VISIBLE : View.GONE);
    }

    /**
     * Bind the large icon.
     * @return if the largeIcon is visible
     */
    private boolean bindLargeIcon(View contentView, StandardTemplateParams p) {
        if (mN.getLargeIcon() == null && mN.getLegacyLargeIcon() != null) {
            mN.setLargeIcon(Icon.createWithBitmap(mN.getLegacyLargeIcon()));
        }
        boolean showLargeIcon = mN.getLargeIcon() != null && !p.hideLargeIcon;
        if (showLargeIcon) {
            DrawUtils.setViewVisibility(contentView, R.id.right_icon, View.VISIBLE);
            DrawUtils.setImageViewIcon(contentView, R.id.right_icon, mN.getLargeIcon());
            processLargeLegacyIcon(mN.getLargeIcon(), contentView, p);
        }
        return showLargeIcon;
    }

    /**
     * Bind the reply icon.
     * @return if the reply icon is visible
     */
    private boolean bindReplyIcon(View contentView, StandardTemplateParams p, boolean largeIconShown) {
        boolean actionVisible = !p.hideReplyIcon;
        Notification.Action action = null;
        if (actionVisible) {
            action = findReplyAction();
            actionVisible = action != null;
        }
        if (actionVisible) {
            DrawUtils.setViewVisibility(contentView, R.id.reply_icon_action, View.VISIBLE);
            DrawUtils.setDrawableTint(contentView, R.id.reply_icon_action,
                    false /* targetBackground */,
                    getNeutralColor(p),
                    PorterDuff.Mode.SRC_ATOP);
            //contentView.setOnClickPendingIntent(R.id.reply_icon_action, action.actionIntent);
            //contentView.setRemoteInputs(R.id.reply_icon_action, action.mRemoteInputs);
        } else {
            //contentView.setRemoteInputs(R.id.reply_icon_action, null);
        }
        DrawUtils.setViewVisibility(contentView, R.id.separator,
                largeIconShown && actionVisible ? View.VISIBLE : View.GONE);
        DrawUtils.setViewVisibility(contentView, R.id.reply_icon_action,
                actionVisible ? View.VISIBLE : View.GONE);
        return actionVisible;
    }

    private Notification.Action findReplyAction() {
        ArrayList<Notification.Action> actions = mActions;
        if (mOriginalActions != null) {
            actions = mOriginalActions;
        }
        int numActions = actions.size();
        for (int i = 0; i < numActions; i++) {
            Notification.Action action = actions.get(i);
            //if (hasValidRemoteInput(action)) {
            //    return action;
            //}
        }
        return null;
    }

    private void bindNotificationHeader(View contentView, StandardTemplateParams p) {
        bindSmallIcon(contentView, p);
        bindHeaderAppName(contentView, p);
        bindHeaderText(contentView, p);
        bindHeaderTextSecondary(contentView, p);
        bindHeaderChronometerAndTime(contentView, p);
        bindProfileBadge(contentView, p);
        bindAlertedIcon(contentView, p);
        //bindActivePermissions(contentView, p);
        //bindExpandButton(contentView, p);
        mN.mUsesStandardHeader = true;
    }

    private void bindActivePermissions(View contentView, StandardTemplateParams p) {
        int color = getNeutralColor(p);
        //DrawUtils.setDrawableTint(contentView, R.id.camera, false, color, PorterDuff.Mode.SRC_ATOP);
        //DrawUtils.setDrawableTint(contentView, R.id.mic, false, color, PorterDuff.Mode.SRC_ATOP);
        //DrawUtils.setDrawableTint(contentView, R.id.overlay, false, color, PorterDuff.Mode.SRC_ATOP);
    }

    private void bindExpandButton(View contentView, StandardTemplateParams p) {
        int color = isColorized(p) ? getPrimaryTextColor(p) : getSecondaryTextColor(p);
        /*DrawUtils.setDrawableTint(contentView, R.id.expand_button, false, color,
                PorterDuff.Mode.SRC_ATOP);
        contentView.setInt(R.id.notification_header, "setOriginalNotificationColor",
                color);*/
    }

    private void bindHeaderChronometerAndTime(View contentView,
                                              StandardTemplateParams p) {
        if (showsTimeOrChronometer()) {
            //DrawUtils.setViewVisibility(contentView, R.id.time_divider, View.VISIBLE);
            //setTextViewColorSecondary(contentView, R.id.time_divider, p);
            if (mN.showChronometer()) {
                DrawUtils.setViewVisibility(contentView, R.id.chronometer, View.VISIBLE);
                DrawUtils.setBase(contentView, R.id.chronometer, 
                        mN.getWhen() + (SystemClock.elapsedRealtime() - System.currentTimeMillis()));
                DrawUtils.setStarted(contentView, R.id.chronometer, true);
                boolean countsDown = mN.getChronometerCountDown();
                DrawUtils.setChronometerCountDown(contentView, R.id.chronometer, countsDown);
                setTextViewColorSecondary(contentView, R.id.chronometer, p);
            } else {
                DrawUtils.setViewVisibility(contentView, R.id.time, View.VISIBLE);
                DrawUtils.setTime(contentView, R.id.time, mN.getWhen());
                setTextViewColorSecondary(contentView, R.id.time, p);
            }
        } else {
            // We still want a time to be set but gone, such that we can show and hide it
            // on demand in case it's a child notification without anything in the header
            DrawUtils.setTime(contentView, R.id.time, mN.getWhen() != 0 ? mN.getWhen() : mN.getCreationTime());
        }
    }

    private boolean bindHeaderText(View contentView, StandardTemplateParams p) {
        CharSequence summaryText = p.summaryText;
        if (summaryText == null && mStyle != null && mStyle.mSummaryTextSet
                && mStyle.hasSummaryInHeader()) {
            summaryText = mStyle.mSummaryText;
        }
        if (summaryText == null
                && mN.getTargetSdkVersion() < Build.VERSION_CODES.N
                && mN.getInfoText() != null) {
            summaryText = mN.getInfoText();
        }
        if (summaryText != null) {
            // TODO: Remove the span entirely to only have the string with propper formating.
            DrawUtils.setTextViewText(contentView, R.id.header_text, processTextSpans(
                    processLegacyText(summaryText)));
            setTextViewColorSecondary(contentView, R.id.header_text, p);
            DrawUtils.setViewVisibility(contentView, R.id.header_text, View.VISIBLE);
            DrawUtils.setViewVisibility(contentView, R.id.header_text_layout, View.VISIBLE);
            return true;
        }

        return false;
    }

    private void bindHeaderTextSecondary(View contentView, StandardTemplateParams p) {
        if (!TextUtils.isEmpty(p.headerTextSecondary)) {
            DrawUtils.setTextViewText(contentView, R.id.header_text_secondary, processTextSpans(
                    processLegacyText(p.headerTextSecondary)));
            setTextViewColorSecondary(contentView, R.id.header_text_secondary, p);
            DrawUtils.setViewVisibility(contentView, R.id.header_text_secondary, View.VISIBLE);
            Log.d(TAG, "bindHeaderTextSecondary: text = " + p.headerTextSecondary);
            if (DrawUtils.isVisible(contentView, R.id.header_text)) {
                DrawUtils.setViewVisibility(contentView, R.id.header_text_divider, View.VISIBLE);
                setTextViewColorSecondary(contentView, R.id.header_text_divider, p);
            }
            DrawUtils.setViewVisibility(contentView, R.id.header_text_layout, View.VISIBLE);
        }
    }

    private void bindHeaderAppName(View contentView, StandardTemplateParams p) {
        DrawUtils.setTextViewText(contentView, R.id.app_name_text, mN.getAppName());
        if (isColorized(p)) {
            setTextViewColorPrimary(contentView, R.id.app_name_text, p);
        } else {
            DrawUtils.setTextColor(contentView, R.id.app_name_text, getSecondaryTextColor(p));
        }
    }

    private boolean isColorized(StandardTemplateParams p) {
        return p.allowColorization && mN.isColorized();
    }

    private void bindSmallIcon(View contentView, StandardTemplateParams p) {
        //if (mN.getSmallIcon() == null && mN.icon != 0) {
        //    mN.setSmallIcon(Icon.createWithResource(mContext, mN.icon));
        //}
        DrawUtils.setImageViewIcon(contentView, R.id.icon, mN.getSmallIcon());
        DrawUtils.setImageLevel(contentView, R.id.icon, mN.getIconLevel());
        processSmallIconColor(mN.getSmallIcon(), contentView, p);
    }

    /**
     * @return true if the built notification will show the time or the chronometer; false
     *         otherwise
     */
    private boolean showsTimeOrChronometer() {
        return mN.showsTime() || mN.showsChronometer();
    }

    private void resetStandardTemplateWithActions(View big) {
        // actions_container is only reset when there are no actions to avoid focus issues with
        // remote inputs.
        DrawUtils.setViewVisibility(big, R.id.actions, View.GONE);
        //big.removeAllViews(R.id.actions);

        DrawUtils.setViewVisibility(big, R.id.notification_material_reply_container, View.GONE);
        DrawUtils.setTextViewText(big, R.id.notification_material_reply_text_1, null);
        DrawUtils.setViewVisibility(big, R.id.notification_material_reply_text_1_container, View.GONE);
        DrawUtils.setViewVisibility(big, R.id.notification_material_reply_progress, View.GONE);

        DrawUtils.setViewVisibility(big, R.id.notification_material_reply_text_2, View.GONE);
        DrawUtils.setTextViewText(big, R.id.notification_material_reply_text_2, null);
        DrawUtils.setViewVisibility(big, R.id.notification_material_reply_text_3, View.GONE);
        DrawUtils.setTextViewText(big, R.id.notification_material_reply_text_3, null);
    }

    private View applyStandardTemplateWithActions(int layoutId) {
        return applyStandardTemplateWithActions(layoutId, mParams.reset().fillTextsFrom(this));
    }

    private static List<Notification.Action> filterOutContextualActions(
            List<Notification.Action> actions) {
        List<Notification.Action> nonContextualActions = new ArrayList<>();
        for (Notification.Action action : actions) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || !action.isContextual()) {
                nonContextualActions.add(action);
            }
        }
        return nonContextualActions;
    }

    private View applyStandardTemplateWithActions(int layoutId,
                                                         StandardTemplateParams p) {
        View big = applyStandardTemplate(layoutId, p);

        resetStandardTemplateWithActions(big);

        boolean validRemoteInput = false;

        // In the UI contextual actions appear separately from the standard actions, so we
        // filter them out here.
        List<Notification.Action> nonContextualActions = filterOutContextualActions(mActions);

        /*int N = nonContextualActions.size();
        boolean emphazisedMode = mN.fullScreenIntent != null;
        big.setBoolean(R.id.actions, "setEmphasizedMode", emphazisedMode);
        if (N > 0) {
            DrawUtils.setViewVisibility(big, R.id.actions_container, View.VISIBLE);
            DrawUtils.setViewVisibility(big, R.id.actions, View.VISIBLE);
            big.setViewLayoutMarginBottomDimen(R.id.notification_action_list_margin_target, 0);
            if (N>MAX_ACTION_BUTTONS) N=MAX_ACTION_BUTTONS;
            for (int i=0; i<N; i++) {
                Notification.Action action = nonContextualActions.get(i);

                boolean actionHasValidInput = hasValidRemoteInput(action);
                validRemoteInput |= actionHasValidInput;

                final RemoteViews button = generateActionButton(action, emphazisedMode, p);
                if (actionHasValidInput && !emphazisedMode) {
                    // Clear the drawable
                    button.setInt(R.id.action0, "setBackgroundResource", 0);
                }
                big.addView(R.id.actions, button);
            }
        } else {
            DrawUtils.setViewVisibility(big, R.id.actions_container, View.GONE);
        }

        CharSequence[] replyText = mN.extras.getCharSequenceArray(EXTRA_REMOTE_INPUT_HISTORY);
        if (validRemoteInput && replyText != null
                && replyText.length > 0 && !TextUtils.isEmpty(replyText[0])
                && p.maxRemoteInputHistory > 0) {
            boolean showSpinner = mN.extras.getBoolean(EXTRA_SHOW_REMOTE_INPUT_SPINNER);
            DrawUtils.setViewVisibility(big, R.id.notification_material_reply_container, View.VISIBLE);
            DrawUtils.setViewVisibility(big, R.id.notification_material_reply_text_1_container,
                    View.VISIBLE);
            DrawUtils.setTextViewText(big, R.id.notification_material_reply_text_1,
                    processTextSpans(replyText[0]));
            setTextViewColorSecondary(big, R.id.notification_material_reply_text_1, p);
            DrawUtils.setViewVisibility(big, R.id.notification_material_reply_progress,
                    showSpinner ? View.VISIBLE : View.GONE);
            big.setProgressIndeterminateTintList(
                    R.id.notification_material_reply_progress,
                    ColorStateList.valueOf(
                            isColorized(p) ? getPrimaryTextColor(p) : resolveContrastColor(p)));

            if (replyText.length > 1 && !TextUtils.isEmpty(replyText[1])
                    && p.maxRemoteInputHistory > 1) {
                DrawUtils.setViewVisibility(big, R.id.notification_material_reply_text_2, View.VISIBLE);
                DrawUtils.setTextViewText(big, R.id.notification_material_reply_text_2,
                        processTextSpans(replyText[1]));
                setTextViewColorSecondary(big, R.id.notification_material_reply_text_2, p);

                if (replyText.length > 2 && !TextUtils.isEmpty(replyText[2])
                        && p.maxRemoteInputHistory > 2) {
                    DrawUtils.setViewVisibility(big, 
                            R.id.notification_material_reply_text_3, View.VISIBLE);
                    DrawUtils.setTextViewText(big, R.id.notification_material_reply_text_3,
                            processTextSpans(replyText[2]));
                    setTextViewColorSecondary(big, R.id.notification_material_reply_text_3, p);
                }
            }
        }*/

        return big;
    }

    private boolean hasValidRemoteInput(Notification.Action action) {
        if (TextUtils.isEmpty(action.title) || action.actionIntent == null) {
            // Weird actions
            return false;
        }

        RemoteInput[] remoteInputs = action.getRemoteInputs();
        if (remoteInputs == null) {
            return false;
        }

        for (RemoteInput r : remoteInputs) {
            CharSequence[] choices = r.getChoices();
            if (r.getAllowFreeFormInput() || (choices != null && choices.length != 0)) {
                return true;
            }
        }
        return false;
    }

    public View createContentView() {
        return createContentView(false);
    }

    private View createContentView(boolean increasedHeight) {
        if (mStyle != null) {
            final View styleView = mStyle.makeContentView(increasedHeight);
            if (styleView != null) {
                return styleView;
            }
        }
        return applyStandardTemplate(R.layout.notification_template_material_base);
    }

    public View createBigContentView() {
        View result = null;
        /*if (mStyle != null) {
            result = mStyle.makeBigContentView();
            hideLine1Text(result);
        } else if (mActions.size() != 0) {
            result = applyStandardTemplateWithActions(getBigBaseLayoutResource());
        }
        makeHeaderExpanded(result);*/
        return result;
    }

    private void hideLine1Text(View result) {
        if (result != null) {
            result.findViewById(R.id.text_line_1).setVisibility(View.GONE);
        }
    }

    /**
     * Adapt the Notification header if this view is used as an expanded view.
     */
    public static void makeHeaderExpanded(View result) {
        if (result != null) {
            //result.setBoolean(R.id.notification_header, "setExpanded", true);
        }
    }

    private CharSequence createSummaryText() {
        CharSequence titleText = mN.getTitle();
        SpannableStringBuilder summary = new SpannableStringBuilder();
        if (titleText == null) {
            titleText = mN.getTitleBig();
        }
        BidiFormatter bidi = BidiFormatter.getInstance();
        if (titleText != null) {
            summary.append(bidi.unicodeWrap(titleText));
        }
        CharSequence contentText = mN.getText();
        if (titleText != null && contentText != null) {
            summary.append(bidi.unicodeWrap(" • "));
        }
        if (contentText != null) {
            summary.append(bidi.unicodeWrap(contentText));
        }
        return summary;
    }

    /*private View generateActionButton(Notification.Action action, boolean emphazisedMode,
                                      StandardTemplateParams p) {
        final boolean tombstone = (action.actionIntent == null);
        View button = new BuilderView(mContext.getApplicationInfo(),
                emphazisedMode ? getEmphasizedActionLayoutResource()
                        : tombstone ? getActionTombstoneLayoutResource()
                        : getActionLayoutResource());
        if (!tombstone) {
            button.setOnClickPendingIntent(R.id.action0, action.actionIntent);
        }
        button.setContentDescription(R.id.action0, action.title);
        if (action.mRemoteInputs != null) {
            button.setRemoteInputs(R.id.action0, action.mRemoteInputs);
        }
        if (emphazisedMode) {
            // change the background bgColor
            CharSequence title = action.title;
            ColorStateList[] outResultColor = null;
            int background = resolveBackgroundColor(p);
            if (isLegacy()) {
                title = ContrastColorUtil.clearColorSpans(title);
            } else {
                outResultColor = new ColorStateList[1];
                title = ensureColorSpanContrast(title, background, outResultColor);
            }
            button.setTextViewText(R.id.action0, processTextSpans(title));
            setTextViewColorPrimary(button, R.id.action0, p);
            int rippleColor;
            boolean hasColorOverride = outResultColor != null && outResultColor[0] != null;
            if (hasColorOverride) {
                // There's a span spanning the full text, let's take it and use it as the
                // background color
                background = outResultColor[0].getDefaultColor();
                int textColor = ContrastColorUtil.resolvePrimaryColor(mContext,
                        background, mInNightMode);
                button.setTextColor(R.id.action0, textColor);
                rippleColor = textColor;
            } else if (getRawColor(p) != COLOR_DEFAULT && !isColorized(p)
                    && mTintActionButtons && !mInNightMode) {
                rippleColor = resolveContrastColor(p);
                button.setTextColor(R.id.action0, rippleColor);
            } else {
                rippleColor = getPrimaryTextColor(p);
            }
            // We only want about 20% alpha for the ripple
            rippleColor = (rippleColor & 0x00ffffff) | 0x33000000;
            button.setColorStateList(R.id.action0, "setRippleColor",
                    ColorStateList.valueOf(rippleColor));
            button.setColorStateList(R.id.action0, "setButtonBackground",
                    ColorStateList.valueOf(background));
            button.setBoolean(R.id.action0, "setHasStroke", !hasColorOverride);
        } else {
            button.setTextViewText(R.id.action0, processTextSpans(
                    processLegacyText(action.title)));
            if (isColorized(p)) {
                setTextViewColorPrimary(button, R.id.action0, p);
            } else if (getRawColor(p) != COLOR_DEFAULT && mTintActionButtons) {
                button.setTextColor(R.id.action0, resolveContrastColor(p));
            }
        }
        button.setIntTag(R.id.action0, R.id.notification_action_index_tag,
                mActions.indexOf(action));
        return button;
    }*/

    /*/**
     * Ensures contrast on color spans against a background color. also returns the color of the
     * text if a span was found that spans over the whole text.
     *
     * @param charSequence the charSequence on which the spans are
     * @param background the background color to ensure the contrast against
     * @param outResultColor an array in which a color will be returned as the first element if
     *                    there exists a full length color span.
     * @return the contrasted charSequence
     */
    /*private CharSequence ensureColorSpanContrast(CharSequence charSequence, int background,
                                                 ColorStateList[] outResultColor) {
        if (charSequence instanceof Spanned) {
            Spanned ss = (Spanned) charSequence;
            Object[] spans = ss.getSpans(0, ss.length(), Object.class);
            SpannableStringBuilder builder = new SpannableStringBuilder(ss.toString());
            for (Object span : spans) {
                Object resultSpan = span;
                int spanStart = ss.getSpanStart(span);
                int spanEnd = ss.getSpanEnd(span);
                boolean fullLength = (spanEnd - spanStart) == charSequence.length();
                if (resultSpan instanceof CharacterStyle) {
                    resultSpan = ((CharacterStyle) span).getUnderlying();
                }
                if (resultSpan instanceof TextAppearanceSpan) {
                    TextAppearanceSpan originalSpan = (TextAppearanceSpan) resultSpan;
                    ColorStateList textColor = originalSpan.getTextColor();
                    if (textColor != null) {
                        int[] colors = textColor.getColors();
                        int[] newColors = new int[colors.length];
                        for (int i = 0; i < newColors.length; i++) {
                            newColors[i] = ContrastColorUtil.ensureLargeTextContrast(
                                    colors[i], background, mInNightMode);
                        }
                        textColor = new ColorStateList(textColor.getStates().clone(),
                                newColors);
                        if (fullLength) {
                            outResultColor[0] = textColor;
                            // Let's drop the color from the span
                            textColor = null;
                        }
                        resultSpan = new TextAppearanceSpan(
                                originalSpan.getFamily(),
                                originalSpan.getTextStyle(),
                                originalSpan.getTextSize(),
                                textColor,
                                originalSpan.getLinkTextColor());
                    }
                } else if (resultSpan instanceof ForegroundColorSpan) {
                    ForegroundColorSpan originalSpan = (ForegroundColorSpan) resultSpan;
                    int foregroundColor = originalSpan.getForegroundColor();
                    foregroundColor = ContrastColorUtil.ensureLargeTextContrast(
                            foregroundColor, background, mInNightMode);
                    if (fullLength) {
                        outResultColor[0] = ColorStateList.valueOf(foregroundColor);
                        resultSpan = null;
                    } else {
                        resultSpan = new ForegroundColorSpan(foregroundColor);
                    }
                } else {
                    resultSpan = span;
                }
                if (resultSpan != null) {
                    builder.setSpan(resultSpan, spanStart, spanEnd, ss.getSpanFlags(span));
                }
            }
            return builder;
        }
        return charSequence;
    }*/

    /**
     * @return Whether we are currently building a notification from a legacy (an app that
     *         doesn't create material notifications by itself) app.
     */
    private boolean isLegacy() {
        if (!mIsLegacyInitialized) {
            mIsLegacy = mN.getTargetSdkVersion()
                    < Build.VERSION_CODES.LOLLIPOP;
            mIsLegacyInitialized = true;
        }
        return mIsLegacy;
    }

    private CharSequence processLegacyText(CharSequence charSequence) {
        boolean isAlreadyLightText = isLegacy() || textColorsNeedInversion();
        if (isAlreadyLightText) {
            return ContrastColorUtil.invertCharSequenceColors(charSequence);
        } else {
            return charSequence;
        }
    }

    private void processSmallIconColor(Icon smallIcon, View contentView, StandardTemplateParams p) {
        boolean colorable = !mN.isLegacy() || false; //getColorUtil().isGrayScaleIcon(context, smallIcon);
        int color = NotificationHeaderView.NO_COLOR; //;
        if (isColorized(p)) {
            //color = getPrimaryTextColor(p);
        } else {
            //color = resolveContrastColor(p);
        }
        if (colorable) {
            ImageView i = contentView.findViewById(R.id.icon);
            //i.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        }
        NotificationHeaderView nhv = contentView.findViewById(R.id.notification_header);
        nhv.setOriginalIconColor(colorable ? color : NotificationHeaderView.NO_COLOR);
    }

    /*/**
     * Make the largeIcon dark if it's a fake smallIcon (that is,
     * if it's grayscale).
     */
    /*// TODO: also check bounds, transparency, that sort of thing.
    private void processLargeLegacyIcon(Icon largeIcon, View contentView,
                                        StandardTemplateParams p) {
        if (largeIcon != null && isLegacy()
                && getColorUtil().isGrayscaleIcon(mContext, largeIcon)) {
            // resolve color will fall back to the default when legacy
            DrawUtils.setDrawableTint(contentView, R.id.icon, false, resolveContrastColor(p),
                    PorterDuff.Mode.SRC_ATOP);
        }
    }*/

    private void sanitizeColor() {
        if (mN.getColor() != COLOR_DEFAULT) {
            mN.setColor(mN.getColor() | 0xFF000000); // no alpha for custom colors
        }
    }

    int resolveNeutralColor() {
        if (mNeutralColor != COLOR_INVALID) {
            return mNeutralColor;
        }
        int background = mContext.getColor(
                R.color.notification_material_background_color);
        mNeutralColor = ContrastColorUtil.resolveDefaultColor(mContext, background,
                mInNightMode);
        if (Color.alpha(mNeutralColor) < 255) {
            // alpha doesn't go well for color filters, so let's blend it manually
            mNeutralColor = compositeColors(mNeutralColor, background);
        }
        return mNeutralColor;
    }

    /*int resolveContrastColor(StandardTemplateParams p) {
        int rawColor = getRawColor(p);
        if (mCachedContrastColorIsFor == rawColor && mCachedContrastColor != COLOR_INVALID) {
            return mCachedContrastColor;
        }

        int color;
        int background = mContext.getColor(
                com.android.internal.R.color.notification_material_background_color);
        if (rawColor == COLOR_DEFAULT) {
            ensureColors(p);
            color = ContrastColorUtil.resolveDefaultColor(mContext, background, mInNightMode);
        } else {
            color = ContrastColorUtil.resolveContrastColor(mContext, rawColor,
                    background, mInNightMode);
        }
        if (Color.alpha(color) < 255) {
            // alpha doesn't go well for color filters, so let's blend it manually
            color = ContrastColorUtil.compositeColors(color, background);
        }
        mCachedContrastColorIsFor = rawColor;
        return mCachedContrastColor = color;
    }*/

    /**
     * Return the raw color of this Notification, which doesn't necessarily satisfy contrast.
     *
     * @see #resolveContrastColor(StandardTemplateParams) for the contrasted color
     * @param p the template params to inflate this with
     */
    private int getRawColor(StandardTemplateParams p) {
        if (p.forceDefaultColor) {
            return COLOR_DEFAULT;
        }
        return mN.getColor();
    }

    /*int resolveNeutralColor() {
        if (mNeutralColor != COLOR_INVALID) {
            return mNeutralColor;
        }
        int background = mContext.getColor(
                com.android.internal.R.color.notification_material_background_color);
        mNeutralColor = ContrastColorUtil.resolveDefaultColor(mContext, background,
                mInNightMode);
        if (Color.alpha(mNeutralColor) < 255) {
            // alpha doesn't go well for color filters, so let's blend it manually
            mNeutralColor = ContrastColorUtil.compositeColors(mNeutralColor, background);
        }
        return mNeutralColor;
    }*/

    private int getBackgroundColor(StandardTemplateParams p) {
        if (isColorized(p)) {
            return mBackgroundColor != COLOR_INVALID ? mBackgroundColor : getRawColor(p);
        } else {
            return COLOR_DEFAULT;
        }
    }

    /**
     * Gets a neutral color that can be used for icons or similar that should not stand out.
     * @param p the template params to inflate this with
     */
    private int getNeutralColor(StandardTemplateParams p) {
        if (isColorized(p)) {
            return getSecondaryTextColor(p);
        } else {
            return resolveNeutralColor();
        }
    }

    /**
     * Same as getBackgroundColor but also resolved the default color to the background.
     * @param p the template params to inflate this with
     */
    private int resolveBackgroundColor(StandardTemplateParams p) {
        int backgroundColor = getBackgroundColor(p);
        if (backgroundColor == COLOR_DEFAULT) {
            backgroundColor = mContext.getColor(
                    R.color.notification_material_background_color);
        }
        return backgroundColor;
    }

    private boolean shouldTintActionButtons() {
        return mTintActionButtons;
    }

    private boolean textColorsNeedInversion() {
        if (mStyle == null || !MediaStyle.class.equals(mStyle.getClass())) {
            return false;
        }
        int targetSdkVersion = mN.getTargetSdkVersion();
        return targetSdkVersion > Build.VERSION_CODES.M
                && targetSdkVersion < Build.VERSION_CODES.O;
    }

    /**
     * Set a color palette to be used as the background and textColors
     *
     * @param backgroundColor the color to be used as the background
     * @param foregroundColor the color to be used as the foreground
     *
     * @hide
     */
    public void setColorPalette(int backgroundColor, int foregroundColor) {
        mBackgroundColor = backgroundColor;
        mForegroundColor = foregroundColor;
        mTextColorsAreForBackground = COLOR_INVALID;
        ensureColors(mParams.reset().fillTextsFrom(this));
    }

    /**
     * Make the largeIcon dark if it's a fake smallIcon (that is,
     * if it's grayscale).
     */
    // TODO: also check bounds, transparency, that sort of thing.
    private void processLargeLegacyIcon(Icon largeIcon, View contentView,
                                        StandardTemplateParams p) {
        /*if (largeIcon != null && mN.isLegacy()
                && getColorUtil().isGrayscaleIcon(mContext, largeIcon)) {
            // resolve color will fall back to the default when legacy
            ImageView i = contentView.findViewById(R.id.icon);
            i.setColorFilter(resolveContrastColor(p), PorterDuff.Mode.SRC_ATOP);
        }*/
    }

    private int resolveContrastColor(StandardTemplateParams p) {
        /*int rawColor = getRawColor(p);
        if (mCachedContrastColorIsFor == rawColor && mCachedContrastColor != COLOR_INVALID) {
            return mCachedContrastColor;
        }

        int color;
        int background = mContext.getColor(
                R.color.notification_material_background_color);
        if (rawColor == COLOR_DEFAULT) {
            ensureColors(p);
            color = ContrastColorUtil.resolveDefaultColor(mContext, background, mInNightMode);
        } else {
            color = ContrastColorUtil.resolveContrastColor(mContext, rawColor,
                    background, mInNightMode);
        }
        if (Color.alpha(color) < 255) {
            // alpha doesn't go well for color filters, so let's blend it manually
            color = ContrastColorUtil.compositeColors(color, background);
        }
        mCachedContrastColorIsFor = rawColor;
        return mCachedContrastColor = color;*/
        return 0;
    }

    /**
     * An object that can apply a rich notification style to a {@link Notification.Builder}
     * object.
     */
    public static abstract class Style {

        /**
         * The number of items allowed simulatanously in the remote input history.
         */
        static final int MAX_REMOTE_INPUT_HISTORY_LINES = 3;
        private CharSequence mBigContentTitle;
        protected CharSequence mSummaryText = null;
        protected boolean mSummaryTextSet = false;
        protected final NotiDrawer mNotiDrawer;

        public Style(NotiDrawer notiDrawer){
            this.mNotiDrawer = notiDrawer;

            mSummaryText = notiDrawer.mN.getSummaryText();
            mSummaryTextSet = mSummaryText != null;
            mBigContentTitle = notiDrawer.mN.getTitleBig();
        }

        /**
         * Overrides ContentTitle in the big form of the template.
         * This defaults to the value passed to setContentTitle().
         */
        protected void internalSetBigContentTitle(CharSequence title) {
            mBigContentTitle = title;
        }

        /**
         * Set the first line of text after the detail section in the big form of the template.
         */
        protected void internalSetSummaryText(CharSequence cs) {
            mSummaryText = cs;
            mSummaryTextSet = true;
        }

        protected View getStandardView(int layoutId) {
            StandardTemplateParams p = mNotiDrawer.mParams.reset().fillTextsFrom(mNotiDrawer);
            return getStandardView(layoutId, p);
        }

        /**
         * Get the standard view for this style.
         *
         * @param layoutId The layout id to use.
         * @param p the params for this inflation.
         * @return A remoteView for this style.
         */
        protected View getStandardView(int layoutId, StandardTemplateParams p) {
            if (mBigContentTitle != null) {
                p.title = mBigContentTitle;
            }

            View contentView = mNotiDrawer.applyStandardTemplateWithActions(layoutId, p);

            if (mBigContentTitle != null && mBigContentTitle.equals("")) {
                contentView.findViewById(R.id.line1).setVisibility(View.GONE);
            } else {
                contentView.findViewById(R.id.line1).setVisibility(View.VISIBLE);
            }

            return contentView;
        }

        /**
         * Construct a Style-specific View for the collapsed notification layout.
         * The default implementation has nothing additional to add.
         *
         * @param increasedHeight true if this layout be created with an increased height.
         */
        public View makeContentView(boolean increasedHeight) {
            return null;
        }

        /**
         * Construct a Style-specific View for the final big notification layout.
         */
        public View makeBigContentView() {
            return null;
        }

        /**
         * Construct a Style-specific View for the final HUN layout.
         *
         * @param increasedHeight true if this layout be created with an increased height.
         */
        public View makeHeadsUpContentView(boolean increasedHeight) {
            return null;
        }

        public void purgeResources() {}

        /**
         * @return true if the style positions the progress bar on the second line; false if the
         *         style hides the progress bar
         */
        protected boolean hasProgress() {
            return true;
        }

        /**
         * @return Whether we should put the summary be put into the notification header
         */
        public boolean hasSummaryInHeader() {
            return true;
        }

        /**
         * @return Whether custom content views are displayed inline in the style
         */
        public boolean displayCustomViewInline() {
            return false;
        }

        /**
         * Reduces the image sizes contained in this style.
         *
         */
        public void reduceImageSizes(Context context) {
        }

        /**
         * Validate that this style was properly composed. This is called at build time.
         */
        public void validate(Context context) {
        }

        public abstract boolean areNotificationsVisiblyDifferent(Style other);

        /**
         * @return the text that should be displayed in the statusBar when heads-upped.
         * If {@code null} is returned, the default implementation will be used.
         *
         */
        public CharSequence getHeadsUpStatusBarText() {
            return null;
        }
    }

    /**
     * Helper class for generating large-format notifications that include a large image attachment.
     */
    public static class BigPictureStyle extends Style {
        private Bitmap mPicture;
        private Icon mBigLargeIcon;
        private boolean mBigLargeIconSet = false;

        private BigPictureStyle(NotiDrawer nd) {
            super(nd);

            mBigLargeIcon = nd.mN.getLargeIconBig();
            mBigLargeIconSet = mBigLargeIcon != null;
            mPicture = nd.mN.getPicture();
        }

        public Bitmap getBigPicture() {
            return mPicture;
        }

        // TODO: I don't remember what this is doing here lol
        @Override
        public View makeContentView(boolean increasedHeight) {
            return makeBigContentView();
        }

        @Override
        public View makeBigContentView() {
            // Replace mN.mLargeIcon with mBigLargeIcon if mBigLargeIconSet
            // This covers the following cases:
            //   1. mBigLargeIconSet -> mBigLargeIcon (null or non-null) applies, overrides
            //          mN.mLargeIcon
            //   2. !mBigLargeIconSet -> mN.mLargeIcon applies
            Icon oldLargeIcon = null;
            Bitmap largeIconLegacy = null;
            if (mBigLargeIconSet) {
                oldLargeIcon = mNotiDrawer.mN.getLargeIcon();
                mNotiDrawer.mN.setLargeIcon(mBigLargeIcon);
                // The legacy largeIcon might not allow us to clear the image, as it's taken in
                // replacement if the other one is null. Because we're restoring these legacy icons
                // for old listeners, this is in general non-null.
                largeIconLegacy = mNotiDrawer.mN.getLegacyLargeIcon();
                mNotiDrawer.mN.setLegacyLargeIcon(null);
            }

            StandardTemplateParams p = mNotiDrawer.mParams.reset().fillTextsFrom(mNotiDrawer);
            View contentView = getStandardView(R.layout.notification_template_material_big_picture, p);
            if (mSummaryTextSet) {
                TextView t = contentView.findViewById(R.id.text);
                t.setText(mNotiDrawer.processLegacyText(mSummaryText));
                //notiDrawer.setTextViewColorSecondary(t, p);
                t.setVisibility(View.VISIBLE);
            }

            if (mBigLargeIconSet) {
                mNotiDrawer.mN.setLargeIcon(oldLargeIcon);
                mNotiDrawer.mN.setLegacyLargeIcon(largeIconLegacy);
            }

            ImageView bp = contentView.findViewById(R.id.big_picture);
            bp.setImageBitmap(mPicture);
            return contentView;
        }

        @Override
        public boolean hasSummaryInHeader() {
            return false;
        }

        /**
         * Note that we aren't actually comparing the contents of the bitmaps here, so this
         * is only doing a cursory inspection. Bitmaps of equal size will appear the same.
         */
        @Override
        public boolean areNotificationsVisiblyDifferent(Style other) {
            if (other == null || getClass() != other.getClass()) {
                return true;
            }
            BigPictureStyle otherS = (BigPictureStyle) other;
            return areBitmapsObviouslyDifferent(getBigPicture(), otherS.getBigPicture());
        }

        private static boolean areBitmapsObviouslyDifferent(Bitmap a, Bitmap b) {
            if (a == b) {
                return false;
            }
            if (a == null || b == null) {
                return true;
            }
            return a.getWidth() != b.getWidth()
                    || a.getHeight() != b.getHeight()
                    || a.getConfig() != b.getConfig()
                    || a.getGenerationId() != b.getGenerationId();
        }
    }

    /**
     * Notification style for media playback notifications.
     */
    public static class MediaStyle extends Style {
        // Changing max media buttons requires also changing templates
        // (notification_template_material_media and notification_template_material_big_media).
        static final int MAX_MEDIA_BUTTONS_IN_COMPACT = 3;
        static final int MAX_MEDIA_BUTTONS = 5;
        @IdRes private static final int[] MEDIA_BUTTON_IDS = {
                R.id.action0,
                //R.id.action1,
                //R.id.action2,
                //R.id.action3,
                //R.id.action4,
        };

        private int[] mActionsToShowInCompact = null;
        private MediaSession.Token mToken;

        public MediaStyle(NotiDrawer notiDrawer) {
            super(notiDrawer);

            mToken = notiDrawer.mN.getMediaSession();
            mActionsToShowInCompact = notiDrawer.mN.getCompactActions();
        }
        
        @Override
        public View makeContentView(boolean increasedHeight) {
            return makeMediaContentView();
        }

        @Override
        public View makeBigContentView() {
            return makeMediaBigContentView();
        }

        @Override
        public View makeHeadsUpContentView(boolean increasedHeight) {
            View expanded = makeMediaBigContentView();
            return expanded != null ? expanded : makeMediaContentView();
        }

        @Override
        public boolean areNotificationsVisiblyDifferent(Style other) {
            if (other == null || getClass() != other.getClass()) {
                return true;
            }
            // All fields to compare are on the Notification object
            return false;
        }

        private void bindMediaActionButton(View container, @IdRes int buttonId,
                                           Notification.Action action, StandardTemplateParams p) {
            final boolean tombstone = (action.actionIntent == null);
            ImageView imageView = container.findViewById(buttonId);
            imageView.setVisibility(View.VISIBLE);
            imageView.setImageIcon(action.getIcon());

            // If the action buttons should not be tinted, then just use the default
            // notification color. Otherwise, just use the passed-in color.
            Resources resources = mNotiDrawer.mContext.getResources();
            Configuration currentConfig = resources.getConfiguration();
            boolean inNightMode = (currentConfig.uiMode & Configuration.UI_MODE_NIGHT_MASK)
                    == Configuration.UI_MODE_NIGHT_YES;
            int tintColor = mNotiDrawer.shouldTintActionButtons() || mNotiDrawer.isColorized(p)
                    ? getActionColor(p)
                    : ContrastColorUtil.resolveColor(mNotiDrawer.mContext,
                    Notification.COLOR_DEFAULT, inNightMode);

            ImageButton button = container.findViewById(buttonId);
            button.getDrawable().mutate().setColorFilter(tintColor, PorterDuff.Mode.SRC_ATOP);

            final TypedArray typedArray = mNotiDrawer.mContext.obtainStyledAttributes(
                    new int[]{ android.R.attr.colorControlHighlight });
            int rippleAlpha = Color.alpha(typedArray.getColor(0, 0));
            typedArray.recycle();
            int rippleColor = Color.argb(rippleAlpha, Color.red(tintColor), Color.green(tintColor),
                    Color.blue(tintColor));
            ((RippleDrawable) button.getBackground().mutate()).setColor(ColorStateList.valueOf(rippleColor));

            if (!tombstone) {
                //container.setOnClickPendingIntent(buttonId, action.actionIntent);
            }
            button.setContentDescription(action.title);
        }

        private View makeMediaContentView() {
            StandardTemplateParams p = mNotiDrawer.mParams.reset().hasProgress(false).fillTextsFrom(
                    mNotiDrawer);
            View view = mNotiDrawer.applyStandardTemplate(
                    R.layout.notification_template_material_media, p);

            //final int numActions = mNotiDrawer.mActions.size();
            //final int numActionsToShow = mActionsToShowInCompact == null
            //        ? 0
            //        : Math.min(mActionsToShowInCompact.length, MAX_MEDIA_BUTTONS_IN_COMPACT);
            //if (numActionsToShow > numActions) {
            //    throw new IllegalArgumentException(String.format(
            //            "setShowActionsInCompactView: action %d out of bounds (max %d)",
            //            numActions, numActions - 1));
            //}
            //for (int i = 0; i < MAX_MEDIA_BUTTONS_IN_COMPACT; i++) {
            //    if (i < numActionsToShow) {
            //        final Notification.Action action = mNotiDrawer.mActions.get(mActionsToShowInCompact[i]);
            //        bindMediaActionButton(view, MEDIA_BUTTON_IDS[i], action, p);
            //    } else {
            //        view.findViewById(MEDIA_BUTTON_IDS[i]).setVisibility(View.GONE);
            //    }
            //}
            handleImage(view);
            // handle the content margin
            int endMargin = R.dimen.notification_content_margin_end;
            if (mNotiDrawer.mN.getLargeIcon() != null) {
                endMargin = R.dimen.notification_media_image_margin_end;
            }
            //view.setViewLayoutMarginEndDimen(R.id.notification_main_column, endMargin);
            return view;
        }

        private int getActionColor(StandardTemplateParams p) {
            return mNotiDrawer.resolveContrastColor(p);
            //return mNotiDrawer.isColorized(p) ? mNotiDrawer.getPrimaryTextColor(p)
              //      : mNotiDrawer.resolveContrastColor(p);
        }

        private View makeMediaBigContentView() {
            //final int actionCount = Math.min(mNotiDrawer.mActions.size(), MAX_MEDIA_BUTTONS);
            // Dont add an expanded view if there is no more content to be revealed
            int actionsInCompact = mActionsToShowInCompact == null
                    ? 0
                    : Math.min(mActionsToShowInCompact.length, MAX_MEDIA_BUTTONS_IN_COMPACT);
            //if (mNotiDrawer.mN.getLargeIcon() == null && actionCount <= actionsInCompact) {
            //    return null;
            //}
            StandardTemplateParams p = mNotiDrawer.mParams.reset().hasProgress(false).fillTextsFrom(
                    mNotiDrawer);
            View big = mNotiDrawer.applyStandardTemplate(
                    R.layout.notification_template_material_big_media, p);

            for (int i = 0; i < MAX_MEDIA_BUTTONS; i++) {
                //if (i < actionCount) {
                //    bindMediaActionButton(big, MEDIA_BUTTON_IDS[i], mNotiDrawer.mActions.get(i), p);
                //} else {
                //    DrawUtils.setViewVisibility(big, MEDIA_BUTTON_IDS[i], View.GONE);
                //}
            }
            //bindMediaActionButton(big, R.id.media_seamless, new Notification.Action(R.drawable.ic_media_seamless,
            //        mNotiDrawer.mContext.getString(
            //                R.string.ext_media_seamless_action), null), p);
            //DrawUtils.setViewVisibility(big, R.id.media_seamless, View.GONE);
            handleImage(big);
            return big;
        }

        private void handleImage(View contentView) {
            if (mNotiDrawer.mN.getLargeIcon() != null) {
                //contentView.setViewLayoutMarginEndDimen(R.id.line1, 0);
                //contentView.setViewLayoutMarginEndDimen(R.id.text, 0);
            }
        }

        @Override
        protected boolean hasProgress() {
            return false;
        }
    }

    private static class StandardTemplateParams {
        boolean hasProgress = true;
        CharSequence title;
        CharSequence text;
        CharSequence headerTextSecondary;
        CharSequence summaryText;
        int maxRemoteInputHistory = Style.MAX_REMOTE_INPUT_HISTORY_LINES;
        boolean hideLargeIcon;
        boolean hideReplyIcon;
        boolean allowColorization  = true;
        boolean forceDefaultColor = false;

        final StandardTemplateParams reset() {
            hasProgress = true;
            title = null;
            text = null;
            summaryText = null;
            headerTextSecondary = null;
            maxRemoteInputHistory = Style.MAX_REMOTE_INPUT_HISTORY_LINES;
            allowColorization = true;
            forceDefaultColor = false;
            return this;
        }

        final StandardTemplateParams hasProgress(boolean hasProgress) {
            this.hasProgress = hasProgress;
            return this;
        }

        final StandardTemplateParams title(CharSequence title) {
            this.title = title;
            return this;
        }

        final StandardTemplateParams text(CharSequence text) {
            this.text = text;
            return this;
        }

        final StandardTemplateParams summaryText(CharSequence text) {
            this.summaryText = text;
            return this;
        }

        final StandardTemplateParams headerTextSecondary(CharSequence text) {
            this.headerTextSecondary = text;
            return this;
        }

        final StandardTemplateParams hideLargeIcon(boolean hideLargeIcon) {
            this.hideLargeIcon = hideLargeIcon;
            return this;
        }

        final StandardTemplateParams hideReplyIcon(boolean hideReplyIcon) {
            this.hideReplyIcon = hideReplyIcon;
            return this;
        }

        final StandardTemplateParams disallowColorization() {
            this.allowColorization = false;
            return this;
        }

        final StandardTemplateParams forceDefaultColor() {
            this.forceDefaultColor = true;
            return this;
        }

        final StandardTemplateParams fillTextsFrom(NotiDrawer nd) {
            OpenWatchNotification n = nd.mN;
            this.title = nd.processLegacyText(n.getTitle());

            CharSequence text = n.getBigText();
            if (TextUtils.isEmpty(text)) {
                text = n.getText();
            }
            this.text = nd.processLegacyText(text);
            this.summaryText = n.getSubText();
            return this;
        }

        /**
         * Set the maximum lines of remote input history lines allowed.
         * @param maxRemoteInputHistory The number of lines.
         * @return The builder for method chaining.
         */
        public StandardTemplateParams setMaxRemoteInputHistory(int maxRemoteInputHistory) {
            this.maxRemoteInputHistory = maxRemoteInputHistory;
            return this;
        }
    }

    private static class DrawUtils {
        public static void setViewVisibility(View root, @IdRes int id, int visibility) {
            View view = root.findViewById(id);
            if (view == null) return;
            
            view.setVisibility(visibility);
        }
        
        public static void setDrawableTint(View root, @IdRes int id, boolean targetBackground,
                                           int colorFilter, PorterDuff.Mode mode) {
            View view = root.findViewById(id);
            if (view == null) return;

            // Pick the correct drawable to modify for this view
            Drawable targetDrawable = null;
            if (targetBackground) {
                targetDrawable = view.getBackground();
            } else if (view instanceof ImageView) {
                ImageView imageView = (ImageView) view;
                targetDrawable = imageView.getDrawable();
            }

            if (targetDrawable != null) {
                targetDrawable.mutate().setColorFilter(colorFilter, mode);
            }
        }
        
        public static void setBase(View root, @IdRes int id, long base) {
            Chronometer view = root.findViewById(id);
            if (view == null) return;
            
            view.setBase(base);
        }
        
        public static void setStarted(View root, @IdRes int id, boolean started) {
            Chronometer view = root.findViewById(id);
            if (view == null) return;
            
            if (started)
                view.start();
            else
                view.stop();
        }

        public static void setChronometerCountDown(View root, @IdRes int id, boolean countDown) {
            Chronometer view = root.findViewById(id);
            if (view == null) return;

            view.setCountDown(countDown);
        }

        public static void setTime(View root, @IdRes int id, long time) {
            DateTimeView view = root.findViewById(id);
            if (view == null) return;

            view.setTime(time);
        }
        
        public static void setTextViewText(View root, @IdRes int id, CharSequence text) {
            TextView view = root.findViewById(id);
            if (view == null) return;
            
            view.setText(text);
        }
        
        public static void setImageViewBitmap(View root, @IdRes int id, Bitmap bitmap) {
            ImageView view = root.findViewById(id);
            if (view == null) return;

            view.setImageBitmap(bitmap);
        }

        public static void setImageViewIcon(View root, @IdRes int id, Icon icon) {
            ImageView view = root.findViewById(id);
            if (view == null) return;

            view.setImageIcon(icon);
        }

        public static void setExpanded(View root, @IdRes int id, boolean expanded) {
            NotificationHeaderView view = root.findViewById(id);
            if (view == null) return;

            //view.setExpanded(expanded);
        }

        public static void setTextColor(View root, @IdRes int id, int color) {
            TextView view = root.findViewById(id);
            if (view == null) return;

            //view.setTextColor(color);
        }

        public static void setBackgroundColor(View root, @IdRes int id, int color) {
            View view = root.findViewById(id);
            if (view == null) return;

            view.setBackgroundColor(color);
        }

        public static void setBackgroundResource(View root, @IdRes int id, int resource) {
            View view = root.findViewById(id);
            if (view == null) return;

            view.setBackgroundResource(resource);
        }

        public static void setImageLevel(View root, @IdRes int id, int level) {
            ImageView view = root.findViewById(id);
            if (view == null) return;

            view.setImageLevel(level);
        }

        public static boolean isVisible(View root, @IdRes int id) {
            View view = root.findViewById(id);

            return view != null && view.getVisibility() == View.VISIBLE;
        }

        public static void setSelected(View root, @IdRes int id, boolean selected) {
            View view = root.findViewById(id);
            if (view == null) return;

            view.setSelected(selected);
        }
    }
}
