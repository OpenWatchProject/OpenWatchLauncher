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
        notificationHelper.addNotification(sbn);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);

        Log.d(TAG, "onNotificationRemoved 1");
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn, RankingMap rankingMap) {
        super.onNotificationRemoved(sbn, rankingMap);

        Log.d(TAG, "onNotificationRemoved 2");
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn, RankingMap rankingMap, int reason) {
        super.onNotificationRemoved(sbn, rankingMap, reason);

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
        notificationHelper.setNotifications(getActiveNotifications());
    }

    @Override
    public void onListenerDisconnected() {
        super.onListenerDisconnected();

        notificationHelper.setNlService(null);
    }
}
