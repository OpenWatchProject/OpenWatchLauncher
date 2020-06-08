package com.openwatchproject.launcher.notification;

import android.app.Notification;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.media.session.MediaSession;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Field;

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
import static com.openwatchproject.launcher.Utils.getApplicationName;

public class OpenWatchNotification {
    private static final String TAG = "PhoneNotification";

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
    //private NotificationAction[] actions;
    private String category;
    private int color;
    private int flags;
    private int number;
    private String tickerText;
    private int visibility;
    private long when;
    private String group;
    private Icon largeIcon;
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
    private Integer mediaSession;
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

    private StatusBarNotification sbn;

    public OpenWatchNotification(Context c, StatusBarNotification sbn) {
        this.sbn = sbn;

        this.appName = getApplicationName(c, sbn.getPackageName());

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
        /*if (n.actions == null) {
            this.actions = null;
        } else {
            this.actions = new NotificationAction[n.actions.length];
            Field resIdField;
            try {
                resIdField = Icon.class.getDeclaredField("mInt1");
                resIdField.setAccessible(true);
            } catch (NoSuchFieldException e) {
                resIdField = null;
            }

            for (int i = 0; i < actions.length; i++) {
                Notification.Action na = n.actions[i];
                Drawable icon;
                if (resIdField != null && na.getIcon() != null) {
                    try {
                        icon = Utils.getDrawableFromPackage(c, resIdField.getInt(na.getIcon()), sbn.getPackageName());
                    } catch (IllegalAccessException e) {
                        icon = null;
                    }
                } else {
                    icon = null;
                }
                this.actions[i] = new NotificationAction(na.title.toString(), icon, na.hashCode());
            }
        }*/
        this.category = n.category;
        this.color = n.color;
        this.flags = n.flags;
        this.number = n.number;
        this.tickerText = n.tickerText != null ? n.tickerText.toString() : null;
        this.visibility = n.visibility;
        this.when = n.when;
        this.group = n.getGroup();
        this.largeIcon = n.getLargeIcon();
        this.smallIcon = n.getSmallIcon();
        this.sortKey = n.getSortKey();
        this.iconLevel = n.iconLevel;

        // Notification data extras
        Bundle extras = n.extras;
        //Log.d(TAG, "PhoneNotification: " + extras.getCharSequence(EXTRA_TITLE).getClass().getName());
        this.bigText = extras.getCharSequence(EXTRA_BIG_TEXT) == null ? null : extras.getCharSequence(EXTRA_BIG_TEXT).toString();
        this.chronometerCountDown = extras.getBoolean(EXTRA_CHRONOMETER_COUNT_DOWN);
        this.compactActions = extras.getIntArray(EXTRA_COMPACT_ACTIONS);
        this.conversationTitle = extras.getCharSequence(EXTRA_CONVERSATION_TITLE) == null ? null : extras.getCharSequence(EXTRA_CONVERSATION_TITLE).toString();
        this.infoText = extras.getCharSequence(EXTRA_INFO_TEXT) == null ? null : extras.getCharSequence(EXTRA_INFO_TEXT).toString();
        this.largeIconBig = extras.getParcelable(EXTRA_LARGE_ICON_BIG);
        if (this.largeIconBig == null) Log.d(TAG, "PhoneNotification: largeIconBig is null!!!!!");
        MediaSession.Token mediaSessionToken = extras.getParcelable(EXTRA_MEDIA_SESSION);
        this.mediaSession = mediaSessionToken != null ? mediaSessionToken.hashCode() : null;
        //this.messages = (Bundle[]) extras.getParcelableArray(EXTRA_MESSAGES);
        this.messages = null;
        if (extras.getParcelable(EXTRA_PICTURE) == null) Log.d(TAG, "PhoneNotification: picture is null!!!!!");
        this.picture = extras.getParcelable(EXTRA_PICTURE);
        this.progress = extras.getInt(EXTRA_PROGRESS);
        this.progressIndeterminate = extras.getBoolean(EXTRA_PROGRESS_INDETERMINATE);
        this.progressMax = extras.getInt(EXTRA_PROGRESS_MAX);
        this.showChronometer = extras.getBoolean(EXTRA_SHOW_CHRONOMETER);
        this.showWhen = extras.getBoolean(EXTRA_SHOW_WHEN);
        this.subText = extras.getCharSequence(EXTRA_SUB_TEXT) == null ? null : extras.getCharSequence(EXTRA_SUB_TEXT).toString();
        this.summaryText = extras.getCharSequence(EXTRA_SUMMARY_TEXT) == null ? null : extras.getCharSequence(EXTRA_SUMMARY_TEXT).toString();
        this.template = extras.getString(EXTRA_TEMPLATE);
        this.text = extras.getCharSequence(EXTRA_TEXT) == null ? null : extras.getCharSequence(EXTRA_TEXT).toString();
        CharSequence[] textLinesCsA = extras.getCharSequenceArray(EXTRA_TEXT_LINES);
        if (textLinesCsA != null) {
            this.textLines = new String[textLinesCsA.length];
            for (int i = 0; i < textLinesCsA.length; i++) {
                this.textLines[i] = textLinesCsA[i].toString();
            }
        } else {
            this.textLines = null;
        }
        this.title = extras.getCharSequence(EXTRA_TITLE) == null ? null : extras.getCharSequence(EXTRA_TITLE).toString();
        this.titleBig = extras.getCharSequence(EXTRA_TITLE_BIG) == null ? null : extras.getCharSequence(EXTRA_TITLE_BIG).toString();

        // Notification builder
        if (extras.getBoolean("android.contains.customView", false))
            throw new RuntimeException("This notification has a customView which is not supported right now!");

        Notification.Builder b = Notification.Builder.recoverBuilder(c, sbn.getNotification());
        if (TextUtils.isEmpty(template)) {

        } else {
            //throw new RuntimeException("This notification has a template: " + template + "!");
        }
    }

    public String getAppName() {
        return appName;
    }

    public String getId() {
        return packageName + ":" + id;
    }

    //public NotificationAction[] getActions() {
    //    return actions;
    //}

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
        return Build.VERSION_CODES.Q;
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
}
