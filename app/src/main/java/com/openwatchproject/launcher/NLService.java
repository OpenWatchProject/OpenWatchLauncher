package com.openwatchproject.launcher;

import android.content.Intent;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class NLService extends NotificationListenerService {
    private static final String TAG = "NLService";

    public static final String ACTION_NOTIFICATION_POSTED = "com.openwatchproject.launcher.action.NOTIFICATION_POSTED";
    public static final String ACTION_NOTIFICATION_REMOVED = "com.openwatchproject.launcher.action.NOTIFICATION_REMOVED";
    public static final String ACTION_NOTIFICATION_RANKING_UPDATE = "com.openwatchproject.launcher.action.NOTIFICATION_RANKING_UPDATE";

    @Override
    public void onNotificationPosted(StatusBarNotification sbn, RankingMap rankingMap) {
        super.onNotificationPosted(sbn, rankingMap);

        Log.d(TAG, "Posted notification from " + sbn.getPackageName());

        Intent i = new Intent(ACTION_NOTIFICATION_POSTED);
        i.putExtra("sbn", sbn);
        i.putExtra("rankingMap", rankingMap);

        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn, RankingMap rankingMap, int reason) {
        super.onNotificationRemoved(sbn, rankingMap, reason);

        Log.d(TAG, "Removed notification from " + sbn.getPackageName());

        Intent i = new Intent(ACTION_NOTIFICATION_REMOVED);
        i.putExtra("sbn", sbn);
        i.putExtra("rankingMap", rankingMap);

        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
    }

    @Override
    public void onNotificationRankingUpdate(RankingMap rankingMap) {
        super.onNotificationRankingUpdate(rankingMap);

        Log.d(TAG, "Ranking updated");

        Intent i = new Intent(ACTION_NOTIFICATION_RANKING_UPDATE);
        i.putExtra("rankingMap", rankingMap);

        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
    }
}
