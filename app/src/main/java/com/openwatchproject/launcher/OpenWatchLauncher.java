package com.openwatchproject.launcher;

import android.app.Application;

import com.openwatchproject.launcher.notification.NotificationHelper;

public class OpenWatchLauncher extends Application {
    private NotificationHelper notificationHelper;

    @Override
    public void onCreate() {
        super.onCreate();

        notificationHelper = new NotificationHelper();
    }

    public NotificationHelper getNotificationHelper() {
        return notificationHelper;
    }
}
