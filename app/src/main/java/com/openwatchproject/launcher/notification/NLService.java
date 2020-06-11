package com.openwatchproject.launcher.notification;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.openwatchproject.launcher.OpenWatchLauncher;

public class NLService extends NotificationListenerService {
    private static final String TAG = "NLService";

    private NotificationHelper notificationHelper;

    @Override
    public void onNotificationPosted(StatusBarNotification sbn, RankingMap rankingMap) {
        super.onNotificationPosted(sbn, rankingMap);

        Log.d(TAG, "Posted notification from " + sbn.getPackageName());
        notificationHelper.addNotification(getApplicationContext(), sbn);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);

        Log.d(TAG, "Removed notification from " + sbn.getPackageName());
        notificationHelper.removeNotification(sbn);
    }

    @Override
    public void onNotificationRankingUpdate(RankingMap rankingMap) {
        super.onNotificationRankingUpdate(rankingMap);

        Log.d(TAG, "Ranking updated");
        notificationHelper.setRankingMap(rankingMap);
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();

        Log.d(TAG, "Listener connected");
        notificationHelper = ((OpenWatchLauncher) getApplication()).getNotificationHelper();
        notificationHelper.setNlService(this);
        notificationHelper.setNotifications(getApplicationContext(), getActiveNotifications());
    }

    @Override
    public void onListenerDisconnected() {
        super.onListenerDisconnected();

        notificationHelper.setNlService(null);
    }
}
