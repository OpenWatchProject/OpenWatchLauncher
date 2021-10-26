package org.openwatchproject.launcher;

import android.app.Application;

import org.openwatchproject.launcher.notification.NotificationHelper;
import org.openwatchproject.launcher.persistence.StorageManager;

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
