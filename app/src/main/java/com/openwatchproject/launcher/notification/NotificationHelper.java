package com.openwatchproject.launcher.notification;

import android.content.Context;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class NotificationHelper {
    private static final String TAG = "NotificationHelper";

    private NLService nlService;
    private final List<OpenWatchNotification> postedNotifications;
    private NotificationListenerService.RankingMap rankingMap;
    private final List<NotificationListener> listeners;

    public NotificationHelper() {
        nlService = null;
        this.postedNotifications = new ArrayList<>();
        this.listeners = new ArrayList<>();
    }

    public void setNlService(NLService nlService) {
        this.nlService = nlService;
    }

    public NLService getNlService() {
        return nlService;
    }

    public void clearAll() {
        postedNotifications.clear();
    }

    public void setNotifications(Context c, StatusBarNotification[] sbns) {
        // Clear the list of posted notifications
        postedNotifications.clear();

        // Add all the notifications
        for (StatusBarNotification sbn : sbns) {
            addNotification(c, sbn);
        }
    }

    public void setRankingMap(NotificationListenerService.RankingMap rankingMap) {
        this.rankingMap = rankingMap;

        for (NotificationListener listener : listeners) {
            listener.onNotificationRankingUpdated(rankingMap);
        }
    }

    public void addNotification(Context c, StatusBarNotification sbn) {
        OpenWatchNotification own = new OpenWatchNotification(c, sbn);
        if (!filter(own)) {
            boolean added = false;
            for (int i = 0; i < postedNotifications.size(); i++) {
                if (postedNotifications.get(i).equals(own)) {
                    Log.d(TAG, "Replacing existing notification for " + own.getPackageName());
                    postedNotifications.set(i, own);
                    added = true;
                    break;
                }
            }

            if (!added) {
                Log.d(TAG, "addNotification: Adding new notification for " + own.getPackageName());
                postedNotifications.add(own);
            }

            for (NotificationListener listener : listeners) {
                listener.onNotificationPosted(own);
            }
        }
    }

    private boolean filter(OpenWatchNotification own) {
        return false;
    }

    public void removeNotification(StatusBarNotification sbn) {
        OpenWatchNotification own = new OpenWatchNotification(sbn);
        postedNotifications.remove(own);

        for (NotificationListener listener : listeners) {
            listener.onNotificationRemoved(own);
        }
    }

    public void addNotificationListener(NotificationListener listener) {
        listeners.add(listener);
    }

    public void removeNotificationListener(NotificationListener listener) {
        listeners.remove(listener);
    }

    public List<OpenWatchNotification> getPostedNotifications() {
        return postedNotifications;
    }

    public interface NotificationListener {
        void onNotificationPosted(OpenWatchNotification own);
        void onNotificationRemoved(OpenWatchNotification own);
        void onNotificationRankingUpdated(NotificationListenerService.RankingMap rankingMap);
    }
}
