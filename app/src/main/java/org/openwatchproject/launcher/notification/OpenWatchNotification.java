package org.openwatchproject.launcher.notification;

import android.app.Notification;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.media.session.MediaSession;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import org.openwatchproject.launcher.Utils;

import static android.app.Notification.EXTRA_BIG_TEXT;
import static android.app.Notification.EXTRA_CHRONOMETER_COUNT_DOWN;
import static android.app.Notification.EXTRA_COMPACT_ACTIONS;
import static android.app.Notification.EXTRA_CONVERSATION_TITLE;
import static android.app.Notification.EXTRA_INFO_TEXT;
import static android.app.Notification.EXTRA_LARGE_ICON_BIG;
import static android.app.Notification.EXTRA_MEDIA_SESSION;
import static android.app.Notification.EXTRA_PICTURE;
import static android.app.Notification.EXTRA_PROGRESS;
import static android.app.Notification.EXTRA_PROGRESS_INDETERMINATE;
import static android.app.Notification.EXTRA_PROGRESS_MAX;
import static android.app.Notification.EXTRA_SHOW_CHRONOMETER;
import static android.app.Notification.EXTRA_SHOW_WHEN;
import static android.app.Notification.EXTRA_SUB_TEXT;
import static android.app.Notification.EXTRA_SUMMARY_TEXT;
import static android.app.Notification.EXTRA_TEMPLATE;
import static android.app.Notification.EXTRA_TEXT;
import static android.app.Notification.EXTRA_TEXT_LINES;
import static android.app.Notification.EXTRA_TITLE;
import static android.app.Notification.EXTRA_TITLE_BIG;

public class OpenWatchNotification {
    private static final String TAG = "OpenWatchNotification";

    /**
     * Maximum number of (generic) action buttons in a notification (contextual action buttons are
     * handled separately).
     * @hide
     */
    public static final int MAX_ACTION_BUTTONS = 3;

    public static final int FLAG_CAN_COLORIZE = 0x00000800;

    // Custom
    private String appName;
    private long chronometerBase;
    private Bitmap profileBadge;

    // StatusBarNotification data
    private String groupKey;
    private int id;
    private String key;
    private String overrideGroupKey;
    private String packageName;
    private long postTime;
    private String tag;
    private boolean isClearable;
    private boolean isGroup;
    private boolean isOngoing;

    // Notification data
    private Notification.Action[] actions;
    private String category;
    private int color;
    private int flags;
    private int number;
    private String tickerText;
    private int visibility;
    private long when;
    private String group;
    private Icon largeIcon;
    private Bitmap largeIconLegacy;
    private Icon smallIcon;
    private String sortKey;
    private int iconLevel;

    // Notification data extras
    private String bigText;
    private boolean chronometerCountDown;
    private int[] compactActions;
    private String conversationTitle;
    private String infoText;
    private Icon largeIconBig;
    private Bundle[] messages;
    private Bitmap picture;
    private int progress;
    private boolean progressIndeterminate;
    private int progressMax;
    private boolean showChronometer;
    private boolean showWhen;
    private String subText;
    private String summaryText;
    private String template;
    private String text;
    private String[] textLines;
    private String title;
    private String titleBig;
    private int targetSdkVersion = Build.VERSION_CODES.Q;
    private MediaSession.Token mediaSession;
    private boolean colorized;

    public boolean mUsesStandardHeader;

    private StatusBarNotification sbn;
    private Context c;
    private boolean isPhoneNotification = false;

    private View view;

    public OpenWatchNotification(Context c, StatusBarNotification sbn) {
        this.sbn = sbn;
        this.c = c;

        this.appName = Utils.getApplicationName(c, sbn.getPackageName());

        // StatusBarNotification data
        this.groupKey = sbn.getGroupKey();
        this.id = sbn.getId();
        this.key = sbn.getKey();
        this.overrideGroupKey = sbn.getOverrideGroupKey();
        this.packageName = sbn.getPackageName();
        this.postTime = sbn.getPostTime();
        this.tag = sbn.getTag();
        this.isClearable = sbn.isClearable();
        this.isGroup = sbn.isGroup();
        this.isOngoing = sbn.isOngoing();

        // Notification data
        Notification n = sbn.getNotification();
        this.actions = n.actions;
        this.category = n.category;
        this.color = n.color;
        this.flags = n.flags;
        this.number = n.number;
        this.tickerText = csToStringOrNull(n.tickerText);
        this.visibility = n.visibility;
        this.when = n.when;
        this.group = n.getGroup();
        this.largeIcon = n.getLargeIcon();
        this.smallIcon = n.getSmallIcon();
        this.sortKey = n.getSortKey();
        this.iconLevel = n.iconLevel;

        // Notification data extras
        Bundle extras = n.extras;
        this.bigText = csToStringOrNull(extras.getCharSequence(EXTRA_BIG_TEXT));
        this.chronometerCountDown = extras.getBoolean(EXTRA_CHRONOMETER_COUNT_DOWN);
        this.compactActions = extras.getIntArray(EXTRA_COMPACT_ACTIONS);
        this.conversationTitle = csToStringOrNull(extras.getCharSequence(EXTRA_CONVERSATION_TITLE));
        this.infoText = csToStringOrNull(extras.getCharSequence(EXTRA_INFO_TEXT));
        this.largeIconBig = extras.getParcelable(EXTRA_LARGE_ICON_BIG);
        //this.messages = (Bundle[]) extras.getParcelableArray(EXTRA_MESSAGES);
        this.messages = null;
        this.picture = extras.getParcelable(EXTRA_PICTURE);
        this.progress = extras.getInt(EXTRA_PROGRESS);
        this.progressIndeterminate = extras.getBoolean(EXTRA_PROGRESS_INDETERMINATE);
        this.progressMax = extras.getInt(EXTRA_PROGRESS_MAX);
        this.showChronometer = extras.getBoolean(EXTRA_SHOW_CHRONOMETER);
        this.showWhen = extras.getBoolean(EXTRA_SHOW_WHEN);
        this.subText = csToStringOrNull(extras.getCharSequence(EXTRA_SUB_TEXT));
        this.summaryText = csToStringOrNull(extras.getCharSequence(EXTRA_SUMMARY_TEXT));
        this.template = extras.getString(EXTRA_TEMPLATE);
        this.text = csToStringOrNull(extras.getCharSequence(EXTRA_TEXT));
        this.textLines = csaToStringArrayOrNull(extras.getCharSequenceArray(EXTRA_TEXT_LINES));
        this.title = csToStringOrNull(extras.getCharSequence(EXTRA_TITLE));
        this.titleBig = csToStringOrNull(extras.getCharSequence(EXTRA_TITLE_BIG));
        this.mediaSession = extras.getParcelable(EXTRA_MEDIA_SESSION);

        // Notification builder
        if (extras.getBoolean("android.contains.customView", false))
            throw new IllegalStateException("This notification has a customView which is not supported right now!");

        // TODO: Do something with this
        //Notification.Builder b = Notification.Builder.recoverBuilder(c, sbn.getNotification());

        fixWiiteStuff();

        Log.d(TAG, "OpenWatchNotification: " + tag + ", " + packageName + ", " + id);
        Log.d(TAG, "OpenWatchNotification: " + toString());
    }

    private void fixWiiteStuff() {
        if (packageName.equals("android") && title.isEmpty() && text.contains(",Tap")) {
            String[] splitted = text.split(",");
            title = splitted[0];
            text = splitted[1];
        }
    }

    public OpenWatchNotification(StatusBarNotification sbn) {
        this.sbn = sbn;

        // StatusBarNotification data
        this.groupKey = sbn.getGroupKey();
        this.id = sbn.getId();
        this.key = sbn.getKey();
        this.overrideGroupKey = sbn.getOverrideGroupKey();
        this.packageName = sbn.getPackageName();
        this.postTime = sbn.getPostTime();
        this.tag = sbn.getTag();
        this.isClearable = sbn.isClearable();
        this.isGroup = sbn.isGroup();
        this.isOngoing = sbn.isOngoing();
    }

    private static String[] csaToStringArrayOrNull(CharSequence[] csa) {
        if (csa == null)
            return null;

        String[] result = new String[csa.length];
        for (int i = 0; i < csa.length; i++)
            result[i] = csa[i].toString();
        return result;
    }

    private static String csToStringOrNull(CharSequence cs) {
        if (cs == null)
            return null;

        return cs.toString();
    }

    public String getAppName() {
        return appName;
    }

    public int getId() {
        return id;
}

    public Notification.Action[] getActions() {
        return actions;
    }

    public Long getWhen() {
        return when;
    }

    public Icon getLargeIcon() {
        return largeIcon;
    }

    public Icon getLargeIconBig() {
        return largeIconBig;
    }

    public Bitmap getPicture() {
        return picture;
    }

    public Icon getSmallIcon() {
        return smallIcon;
    }

    public void setSmallIcon(Icon smallIcon) {
        this.smallIcon = smallIcon;
    }

    public String getSummaryText() {
        return summaryText;
    }

    public String getTitleBig() {
        return titleBig;
    }

    public String getBigText() {
        return bigText;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getFlags() {
        return flags;
    }

    public boolean isLegacy() {
        return false;
    }

    public int getTargetSdkVersion() {
        if (isPhoneNotification) {
            return targetSdkVersion;
        }

        return Utils.getTargetSdkVersion(c, packageName);
    }

    public String getInfoText() {
        return infoText;
    }

    public long getCreationTime() {
        return 1;
    }

    public CharSequence[] getRemoteInputHistory() {
        return null;
    }

    public boolean getShowRemoteInputSpinner() {
        return false;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public String getSubText() {
        return subText;
    }

    public String getTemplate() {
        return template;
    }

    public int[] getCompactActions() {
        return compactActions;
    }

    public String getTag() {
        return tag;
    }

    public int getVisibility() {
        return visibility;
    }

    public boolean getChronometerCountDown() {
        return chronometerCountDown;
    }

    public boolean showChronometer() {
        return showChronometer;
    }

    public String getPackageName() {
        return packageName;
    }

    public int getIconLevel() {
        return iconLevel;
    }

    public int getColor() {
        return color;
    }

    public long getChronometerBase() {
        return chronometerBase;
    }

    public Bitmap getProfileBadge() {
        return profileBadge;
    }

    public void setLargeIcon(Icon largeIcon) {
        this.largeIcon = largeIcon;
    }

    public Bitmap getLegacyLargeIcon() {
        return null;
    }

    public void setLegacyLargeIcon(Bitmap legacyLargeIcon) {

    }

    public MediaSession.Token getMediaSession() {
        return mediaSession;
    }

    public View getView(Context context) {
        if (view == null) {
            Log.d(TAG, "Drawing " + getPackageName() + ", " + sbn.getNotification().extras);
            view = new NotiDrawer(context, this).createContentView();
        }

        return view;
    }

    /**
     * @return whether this notification is a foreground service notification
     */
    public boolean isForegroundService() {
        return (flags & Notification.FLAG_FOREGROUND_SERVICE) != 0;
    }

    /**
     * @return whether this notification has a media session attached
     */
    public boolean hasMediaSession() {
        return mediaSession != null;
    }

    /**
     * @return true if this notification is colorized.
     */
    public boolean isColorized() {
        if (isColorizedMedia()) {
            return true;
        }
        return colorized
                && (hasColorizedPermission() || isForegroundService());
    }

    /**
     * Returns whether an app can colorize due to the android.permission.USE_COLORIZED_NOTIFICATIONS
     * permission. The permission is checked when a notification is enqueued.
     */
    private boolean hasColorizedPermission() {
        return (flags & FLAG_CAN_COLORIZE) != 0;
    }

    /**
     * @return true if this notification is colorized and it is a media notification
     */
    public boolean isColorizedMedia() {
        if ("android.app.Notification$MediaStyle".equals(template)) {
            if (colorized && hasMediaSession()) {
                return true;
            }
        } else if ("android.app.Notification$DecoratedMediaCustomViewStyle".equals(template)) {
            if (colorized && hasMediaSession()) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return true if this is a media notification
     */
    public boolean isMediaNotification() {
        if ("android.app.Notification$MediaStyle".equals(template)) {
            return true;
        } else if ("android.app.Notification$DecoratedMediaCustomViewStyle".equals(template)) {
            return true;
        }
        return false;
    }

    /**
     * @return true if this notification is showing as a bubble
     * */
    public boolean isBubbleNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return (flags & Notification.FLAG_BUBBLE) != 0;
        }
        return false;
    }

    private boolean hasLargeIcon() {
        return largeIcon != null || largeIconLegacy != null;
    }

    /**
     * @return true if the notification will show the time; false otherwise
     */
    public boolean showsTime() {
        return when != 0 && showWhen;
    }

    /**
     * @return true if the notification will show a chronometer; false otherwise
     */
    public boolean showsChronometer() {
        return when != 0 && showChronometer;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof OpenWatchNotification) {
            OpenWatchNotification own1 = this;
            OpenWatchNotification own2 = (OpenWatchNotification) obj;

            if (own1.getTag() != null && own2.getTag() != null && own1.getTag().equals(own2.getTag())) return true;
            if (!(own1.getId() == own2.getId())) return false;
            if (!(own1.getPackageName().equals(own2.getPackageName()))) return false;

            return true;
        } else if (obj instanceof StatusBarNotification) {
            OpenWatchNotification own1 = this;
            StatusBarNotification own2 = (StatusBarNotification) obj;

            if (own1.getTag() != null && own1.getTag().equals(own2.getTag())) return true;
            if (!(own1.getId() == own2.getId())) return false;
            if (!(own1.getPackageName().equals(own2.getPackageName()))) return false;

            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        if (getTag() != null) {
            return tag.hashCode() + id + packageName.hashCode();
        }

        return id + packageName.hashCode();
    }
}
