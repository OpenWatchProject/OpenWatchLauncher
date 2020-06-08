package com.openwatchproject.launcher.notification;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import java.util.ArrayList;
import java.util.List;

public class NotificationHelper {
    private final List<StatusBarNotification> postedNotifications;
    private final List<StatusBarNotification> removedNotifications;
    private NotificationListenerService.RankingMap rankingMap;
    private final List<NotificationListener> listeners;

    public NotificationHelper() {
        this.postedNotifications = new ArrayList<>();
        this.removedNotifications = new ArrayList<>();
        this.listeners = new ArrayList<>();
    }

    public void clearAll() {
        postedNotifications.clear();
        removedNotifications.clear();
    }

    public void setNotifications(StatusBarNotification[] sbns) {
        postedNotifications.clear();

        for (StatusBarNotification sbn : sbns) {
            if (filterNotification(sbn)) {
                addNotification(sbn);
            }
        }

        List<StatusBarNotification> toRemove = new ArrayList<>();
        for (StatusBarNotification sbn : removedNotifications) {
            if (!postedNotifications.contains(sbn)) {
                toRemove.add(sbn);
            }
        }
        removedNotifications.remove(toRemove);
    }

    public void setRankingMap(NotificationListenerService.RankingMap rankingMap) {
        this.rankingMap = rankingMap;

    }

    public void addNotification(StatusBarNotification sbn) {

        postedNotifications.add(sbn);

        for (NotificationListener listener : listeners)
            listener.onNotificationPosted(sbn);
    }

    public void dismissNotification(StatusBarNotification sbn) {
        removedNotifications.add(sbn);
    }

    public void removeNotification(StatusBarNotification sbn) {
        postedNotifications.remove(sbn);
        removedNotifications.remove(sbn);
    }

    public void addNotificationListener(NotificationListener listener) {
        listeners.add(listener);
    }

    public void removeNotificationListener(NotificationListener listener) {
        listeners.remove(listener);
    }

    private boolean filterNotification(StatusBarNotification sbn) {
        return true;
    }

    public List<StatusBarNotification> getPostedNotifications() {
        return postedNotifications;
    }

    public interface NotificationListener {
        void onNotificationPosted(StatusBarNotification sbn);
        void onNotificationRemoved(StatusBarNotification sbn);
        void onNotificationRankingUpdated(NotificationListenerService.RankingMap rankingMap);
    }
}
