package com.openwatchproject.launcher.notification;

import android.app.Notification;
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
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.core.text.BidiFormatter;

import com.openwatchproject.launcher.R;
import com.openwatchproject.launcher.notification.view.DateTimeView;
import com.openwatchproject.launcher.notification.view.NotificationHeaderView;

public class NotiDrawer {
    private static final String TAG = "NotiDrawer";
    
    public static final int COLOR_DEFAULT = 0; // AKA Color.TRANSPARENT
    public static final int COLOR_INVALID = 1;

    private final Context mContext;
    private final OpenWatchNotification mN;
    private final Style mStyle;
    private final StandardTemplateParams mParams;

    private boolean mUsesStandardHeader;
    private boolean mIsLegacy;
    private boolean mIsLegacyInitialized;

    private boolean mTintActionButtons;

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

    private void sanitizeColor() {
        if (mN.getColor() != COLOR_DEFAULT) {
            mN.setColor(mN.getColor() | 0xFF000000); // no alpha for custom colors
        }
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
        if (p.title != null) {
            TextView t = contentView.findViewById(R.id.title);
            t.setVisibility(View.VISIBLE);
            t.setText(p.title);
            //t.setTextColor(mN.getTitleColor());
        }
        if (p.text != null) {
            TextView t = contentView.findViewById(R.id.text);
            t.setText(p.text);
            t.setTextColor(Color.WHITE);
            //t.setTextColor(mN.getTextColor());
            t.setVisibility(View.VISIBLE);
        }

        return contentView;
    }
    
    /**
     * Resets the notification header to its original state
     */
    private void resetNotificationHeader(View contentView) {
        // Small icon doesn't need to be reset, as it's always set. Resetting would prevent
        // re-using the drawable when the notification is updated.
        TextView ant = contentView.findViewById(R.id.app_name_text);
        ant.setText(null);
        contentView.findViewById(R.id.chronometer).setVisibility(View.GONE);
        contentView.findViewById(R.id.chronometer).setVisibility(View.GONE);
        contentView.findViewById(R.id.header_text_layout).setVisibility(View.GONE);
        TextView ht = contentView.findViewById(R.id.header_text);
        ht.setVisibility(View.GONE);
        ht.setText(null);
        TextView hts = contentView.findViewById(R.id.header_text_secondary);
        hts.setVisibility(View.GONE);
        hts.setText(null);
        contentView.findViewById(R.id.header_text_divider).setVisibility(View.GONE);
        contentView.findViewById(R.id.time).setVisibility(View.GONE);
        ImageView pb = contentView.findViewById(R.id.profile_badge);
        pb.setImageIcon(null);
        contentView.findViewById(R.id.profile_badge).setVisibility(View.GONE);
        //contentView.findViewById(R.id.alerted_icon).setVisibility(View.GONE);
        mUsesStandardHeader = false;
    }

    private void resetStandardTemplate(View contentView) {
        resetNotificationHeader(contentView);
        contentView.findViewById(R.id.right_icon).setVisibility(View.GONE);
        contentView.findViewById(R.id.title).setVisibility(View.GONE);
        TextView t = contentView.findViewById(R.id.title);
        t.setText(null);
        contentView.findViewById(R.id.text).setVisibility(View.GONE);
        TextView te = contentView.findViewById(R.id.text);
        te.setText(null);
        contentView.findViewById(R.id.text_line_1).setVisibility(View.GONE);
        TextView tl1 = contentView.findViewById(R.id.text_line_1);
        tl1.setText(null);
    }

    private void updateBackgroundColor(View contentView, StandardTemplateParams p) {
        if (isColorized(p)) {
            //contentView.findViewById(R.id.status_bar_latest_event_content).setBackgroundColor(getBackgroundColor(p));
        } else {
            contentView.findViewById(R.id.status_bar_latest_event_content).setBackgroundResource(0);
        }
    }

    private void bindSmallIcon(View contentView, StandardTemplateParams p) {
        ImageView i = contentView.findViewById(R.id.icon);
        i.setImageIcon(mN.getSmallIcon());
        i.setImageLevel(mN.getIconLevel());
        processSmallIconColor(mN.getSmallIcon(), contentView, p);
    }

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

    private boolean isColorized(StandardTemplateParams p) {
        return false;
    }

    private void bindNotificationHeader(View contentView, StandardTemplateParams p) {
        bindSmallIcon(contentView, p);
        bindHeaderAppName(contentView, p);
        boolean headerTextVisible = bindHeaderText(contentView, p);
        bindHeaderTextSecondary(contentView, p, headerTextVisible);
        bindHeaderChronometerAndTime(contentView, p);
        bindProfileBadge(contentView, p);
        //bindAlertedIcon(contentView, p);
        mUsesStandardHeader = true;
    }

    private void bindHeaderAppName(View contentView, StandardTemplateParams p) {
        TextView ant = contentView.findViewById(R.id.app_name_text);
        ant.setText(mN.getAppName());
        if (isColorized(p)) {
            //setTextViewColorPrimary(ant, p);
        } else {
            //ant.setTextColor(getSecondaryTextColor(p));
        }
        //ant.setTextColor(mN.getAppNameTextColor());
    }

    private boolean bindHeaderText(View contentView, StandardTemplateParams p) {
        boolean visible = false;
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
            TextView ht = contentView.findViewById(R.id.header_text);
            ht.setText(summaryText);
            //setTextViewColorSecondary(ht, p);
            ht.setVisibility(View.VISIBLE);
            visible = true;
            contentView.findViewById(R.id.header_text_layout).setVisibility(View.VISIBLE);
        }
        return visible;
    }

    private void bindHeaderTextSecondary(View contentView, StandardTemplateParams p, boolean headerTextVisible) {
        if (!TextUtils.isEmpty(p.headerTextSecondary)) {
            TextView hts = contentView.findViewById(R.id.header_text_secondary);
            hts.setText(p.headerTextSecondary);
            //setTextViewColorSecondary(hts, p);
            hts.setVisibility(View.VISIBLE);
            if (headerTextVisible) {
                TextView htsd = contentView.findViewById(R.id.header_text_divider);
                htsd.setVisibility(View.VISIBLE);
                //setTextViewColorSecondary(htsd, p);
            }
            contentView.findViewById(R.id.header_text_layout).setVisibility(View.VISIBLE);
        }
    }

    private void bindHeaderChronometerAndTime(View contentView, StandardTemplateParams p) {
        if (showsTimeOrChronometer()) {
            if (mN.showChronometer()) {
                Chronometer c = contentView.findViewById(R.id.chronometer);
                c.setVisibility(View.VISIBLE);
                c.setBase(mN.getChronometerBase());
                c.start();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    c.setCountDown(mN.getChronometerCountDown());
                }
                //setTextViewColorSecondary(c, p);
            } else {
                DateTimeView t = contentView.findViewById(R.id.time);
                t.setVisibility(View.VISIBLE);
                t.setTime(mN.getWhen());
                //setTextViewColorSecondary(t, p);
            }
        } else {
            // We still want a time to be set but gone, such that we can show and hide it
            // on demand in case it's a child notification without anything in the header
            DateTimeView t = contentView.findViewById(R.id.time);
            t.setTime(mN.getWhen() != 0 ? mN.getWhen() : mN.getCreationTime());
        }
    }

    private void bindProfileBadge(View contentView, StandardTemplateParams p) {
        Bitmap profileBadge = mN.getProfileBadge();

        if (profileBadge != null) {
            ImageView pb = contentView.findViewById(R.id.profile_badge);
            pb.setImageBitmap(profileBadge);
            pb.setVisibility(View.VISIBLE);
            if (isColorized(p)) {
                //pb.setColorFilter(getPrimaryTextColor(p), PorterDuff.Mode.SRC_ATOP);
            }
        }
    }

    /**
     * @return true if the built notification will show the time or the chronometer; false
     *         otherwise
     */
    private boolean showsTimeOrChronometer() {
        return mN.showsTime() || mN.showsChronometer();
    }

    private void bindLargeIconAndReply(View contentView, StandardTemplateParams p) {
        boolean largeIconShown = bindLargeIcon(contentView, p);
        boolean replyIconShown = bindReplyIcon(contentView, p, largeIconShown);
        boolean iconContainerVisible = largeIconShown || replyIconShown;
        //contentView.findViewById(R.id.right_icon_container)
        //        .setVisibility(iconContainerVisible ? View.VISIBLE : View.GONE);
    }

    /**
     * Bind the large icon.
     * @return if the largeIcon is visible
     */
    private boolean bindLargeIcon(View contentView, StandardTemplateParams p) {
        boolean showLargeIcon = mN.getLargeIcon() != null && !p.hideLargeIcon;
        if (showLargeIcon) {
            ImageView ri = contentView.findViewById(R.id.right_icon);
            ri.setVisibility(View.VISIBLE);
            ri.setImageIcon(mN.getLargeIcon());
            processLargeLegacyIcon(mN.getLargeIcon(), contentView, p);
        }
        return showLargeIcon;
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
        ImageView ria = contentView.findViewById(R.id.reply_icon_action);
        if (actionVisible) {
            ria.setColorFilter(getNeutralColor(p), PorterDuff.Mode.SRC_ATOP);
            //contentView.setOnClickPendingIntent(R.id.reply_icon_action, action.actionIntent);
            //contentView.setRemoteInputs(R.id.reply_icon_action, action.mRemoteInputs);
        } else {
            //contentView.setRemoteInputs(R.id.reply_icon_action, null);
        }
        //contentView.findViewById(R.id.separator).setVisibility(largeIconShown && actionVisible ? View.VISIBLE : View.GONE);
        //ria.setVisibility(actionVisible ? View.VISIBLE : View.GONE);
        return actionVisible;
    }

    /**
     * Gets a neutral color that can be used for icons or similar that should not stand out.
     * @param p the template params to inflate this with
     */
    private int getNeutralColor(StandardTemplateParams p) {
        if (isColorized(p)) {
            //return getSecondaryTextColor(p);
        } else {
            //return resolveNeutralColor();
        }

        return 0;
    }

    private boolean shouldTintActionButtons() {
        return mTintActionButtons;
    }

    private boolean textColorsNeedInversion() {
        if (mStyle == null || !Notification.MediaStyle.class.equals(mStyle.getClass())) {
            return false;
        }
        int targetSdkVersion = mN.getTargetSdkVersion();
        return targetSdkVersion > Build.VERSION_CODES.M
                && targetSdkVersion < Build.VERSION_CODES.O;
    }

    private Notification.Action findReplyAction() {
        /*ArrayList<Notification.Action> actions = mActions;
        if (mOriginalActions != null) {
            actions = mOriginalActions;
        }
        int numActions = actions.size();
        for (int i = 0; i < numActions; i++) {
            Notification.Action action = actions.get(i);
            if (hasValidRemoteInput(action)) {
                return action;
            }
        }*/ // TODO: FIX THIS
        return null;
    }

    private View applyStandardTemplateWithActions(int layoutId) {
        return applyStandardTemplateWithActions(layoutId, mParams.reset().fillTextsFrom(this));
    }

    private void resetStandardTemplateWithActions(View big) {
        // actions_container is only reset when there are no actions to avoid focus issues with
        // remote inputs.

        //ViewGroup a = big.findViewById(R.id.actions);
        //a.setVisibility(View.GONE);
        //a.removeAllViews();

        //big.findViewById(R.id.notification_material_reply_container).setVisibility(View.GONE);
        //LinearLayout nmrt1c = big.findViewById(R.id.notification_material_reply_text_1_container);
        //TextView nmrt1 = big.findViewById(R.id.notification_material_reply_text_1);
        //nmrt1.setText(null);
        //nmrt1c.setVisibility(View.GONE);
        //big.findViewById(R.id.notification_material_reply_progress).setVisibility(View.GONE);

        //big.findViewById(R.id.notification_material_reply_text_2).setVisibility(View.GONE);
        //TextView nmrt2 = big.findViewById(R.id.notification_material_reply_text_2);
        //nmrt2.setText(null);
        //big.findViewById(R.id.notification_material_reply_text_3).setVisibility(View.GONE);
        //TextView nmrt3 = big.findViewById(R.id.notification_material_reply_text_3);
        //nmrt3.setText(null);

        //big.setViewLayoutMarginBottomDimen(R.id.notification_action_list_margin_target,
        //        R.dimen.notification_content_margin);
    }

    private View applyStandardTemplateWithActions(int layoutId, StandardTemplateParams p) {
        View big = applyStandardTemplate(layoutId, p);

        resetStandardTemplateWithActions(big);

        boolean validRemoteInput = false;

        /*// In the UI contextual actions appear separately from the standard actions, so we
        // filter them out here.
        List<Notification.Action> nonContextualActions = filterOutContextualActions(mActions);

        int N = nonContextualActions.size();
        boolean emphazisedMode = mN.fullScreenIntent != null;
        big.setBoolean(R.id.actions, "setEmphasizedMode", emphazisedMode);
        if (N > 0) {
            big.setViewVisibility(R.id.actions_container, View.VISIBLE);
            big.setViewVisibility(R.id.actions, View.VISIBLE);
            big.setViewLayoutMarginBottomDimen(R.id.notification_action_list_margin_target, 0);
            if (N>MAX_ACTION_BUTTONS) N=MAX_ACTION_BUTTONS;
            for (int i=0; i<N; i++) {
                Action action = nonContextualActions.get(i);

                boolean actionHasValidInput = hasValidRemoteInput(action);
                validRemoteInput |= actionHasValidInput;

                final View button = generateActionButton(action, emphazisedMode, p);
                if (actionHasValidInput && !emphazisedMode) {
                    // Clear the drawable
                    button.setInt(R.id.action0, "setBackgroundResource", 0);
                }
                big.addView(R.id.actions, button);
            }
        } else {
            big.setViewVisibility(R.id.actions_container, View.GONE);
        }*/

        CharSequence[] replyText = mN.getRemoteInputHistory();
        if (validRemoteInput && replyText != null
                && replyText.length > 0 && !TextUtils.isEmpty(replyText[0])
                && p.maxRemoteInputHistory > 0) {
            boolean showSpinner = mN.getShowRemoteInputSpinner();
            big.findViewById(R.id.notification_material_reply_container).setVisibility(View.VISIBLE);
            big.findViewById(R.id.notification_material_reply_text_1_container).setVisibility(View.VISIBLE);
            TextView nmrt1 = big.findViewById(R.id.notification_material_reply_text_1);
            nmrt1.setText(replyText[0]);
            //setTextViewColorSecondary(nmrt1, p);
            big.findViewById(R.id.notification_material_reply_progress)
                    .setVisibility(showSpinner ? View.VISIBLE : View.GONE);
            ProgressBar nmrp = big.findViewById(R.id.notification_material_reply_progress);
            //nmrp.setIndeterminateTintList(ColorStateList.valueOf(
            //        isColorized(p) ? getPrimaryTextColor(p) : resolveContrastColor(p)));

            if (replyText.length > 1 && !TextUtils.isEmpty(replyText[1])
                    && p.maxRemoteInputHistory > 1) {
                TextView nmrt2 = big.findViewById(R.id.notification_material_reply_text_2);
                nmrt2.setVisibility(View.VISIBLE);
                nmrt2.setText(replyText[1]);
                //setTextViewColorSecondary(nmrt2, p);

                if (replyText.length > 2 && !TextUtils.isEmpty(replyText[2])
                        && p.maxRemoteInputHistory > 2) {
                    TextView nmrt3 = big.findViewById(R.id.notification_material_reply_text_3);
                    nmrt3.setVisibility(View.VISIBLE);
                    nmrt3.setText(replyText[2]);
                    //setTextViewColorSecondary(nmrt3, p);
                }
            }
        }

        return big;
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

            //mToken = extras.getParcelable(EXTRA_MEDIA_SESSION);
            //mActionsToShowInCompact = extras.getIntArray(EXTRA_COMPACT_ACTIONS);
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
            for (int i = 0; i < MAX_MEDIA_BUTTONS_IN_COMPACT; i++) {
                //if (i < numActionsToShow) {
                //    final Notification.Action action = mNotiDrawer.mActions.get(mActionsToShowInCompact[i]);
                //    bindMediaActionButton(view, MEDIA_BUTTON_IDS[i], action, p);
                //} else {
                //    view.findViewById(MEDIA_BUTTON_IDS[i]).setVisibility(View.GONE);
                //}
            }
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
                //    big.setViewVisibility(MEDIA_BUTTON_IDS[i], View.GONE);
                //}
            }
            //bindMediaActionButton(big, R.id.media_seamless, new Notification.Action(R.drawable.ic_media_seamless,
            //        mNotiDrawer.mContext.getString(
            //                R.string.ext_media_seamless_action), null), p);
            //big.setViewVisibility(R.id.media_seamless, View.GONE);
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
}
