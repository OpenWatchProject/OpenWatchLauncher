package org.openwatchproject.launcher.notification;

import android.app.Notification;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;

import static android.app.Notification.EXTRA_CHRONOMETER_COUNT_DOWN;
import static android.app.Notification.EXTRA_INFO_TEXT;
import static android.app.Notification.EXTRA_LARGE_ICON_BIG;
import static android.app.Notification.EXTRA_PICTURE;
import static android.app.Notification.EXTRA_SHOW_CHRONOMETER;
import static android.app.Notification.EXTRA_SHOW_WHEN;
import static android.app.Notification.EXTRA_SUMMARY_TEXT;
import static android.app.Notification.EXTRA_TEMPLATE;
import static android.app.Notification.EXTRA_TEXT;
import static android.app.Notification.EXTRA_TITLE;
import static android.app.Notification.EXTRA_TITLE_BIG;

public class NotificationUtils {
    public static String getTemplate(Notification n) {
        return n.extras.getString(EXTRA_TEMPLATE);
    }

    public static String getTitle(Notification n) {
        return n.extras.getString(EXTRA_TITLE);
    }

    public static String getTitleBig(Notification n) {
        return n.extras.getString(EXTRA_TITLE_BIG);
    }

    public static String getText(Notification n) {
        return n.extras.getString(EXTRA_TEXT);
    }

    public static String getInfoText(Notification n) {
        return n.extras.getString(EXTRA_INFO_TEXT);
    }

    /*public static long getChronometerBase(Notification n) {
        return n.extras.getLong();
    }*/

    public static boolean getChronometerCountDown(Notification n) {
        return n.extras.getBoolean(EXTRA_CHRONOMETER_COUNT_DOWN);
    }

    public static boolean getShowWhen(Notification n) {
        return n.extras.getBoolean(EXTRA_SHOW_WHEN);
    }

    public static boolean getShowChronometer(Notification n) {
        return n.extras.getBoolean(EXTRA_SHOW_CHRONOMETER);
    }

    public static boolean getShowsTime(Notification n) {
        return n.when != 0 && getShowWhen(n);
    }

    public static boolean getShowsChronometer(Notification n) {
        return n.when != 0 && getShowWhen(n);
    }

    public static String getSummaryText(Notification n) {
        return n.extras.getString(EXTRA_SUMMARY_TEXT);
    }

    public static Icon getLargeIconBig(Notification n) {
        return n.extras.getParcelable(EXTRA_LARGE_ICON_BIG);
    }

    public static Bitmap getPicture(Notification n) {
        return n.extras.getParcelable(EXTRA_PICTURE);
    }
}
