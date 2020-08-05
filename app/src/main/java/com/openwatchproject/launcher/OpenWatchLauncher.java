package com.openwatchproject.launcher;

import android.app.Application;

import com.openwatchproject.launcher.notification.NotificationHelper;
import com.openwatchproject.launcher.persistence.StorageManager;

public class OpenWatchLauncher extends Application {
    private NotificationHelper notificationHelper;
    private StorageManager storageManager;

    @Override
    public void onCreate() {
        super.onCreate();

        notificationHelper = new NotificationHelper();
        storageManager = new StorageManager(getApplicationContext());
    }

    public NotificationHelper getNotificationHelper() {
        return notificationHelper;
    }

    public StorageManager getStorageManager() {
        return storageManager;
    }
}
