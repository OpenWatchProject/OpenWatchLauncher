package com.openwatchproject.launcher.notification;

import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.PagerAdapter;

import com.openwatchproject.launcher.fragment.NotificationsFragment;

import java.util.ArrayList;
import java.util.List;

public class NotificationViewPagerAdapter extends PagerAdapter {
    private static final String TAG = "NotificationViewPagerAd";

    private List<StatusBarNotification> notifications;
    private NotificationsFragment.NotificationCallback callback;

    public NotificationViewPagerAdapter(NotificationsFragment.NotificationCallback callback) {
        this.notifications = new ArrayList<>();
        this.callback = callback;
    }

    public void addNotification(StatusBarNotification sbn) {
        notifications.add(sbn);
        notifyDataSetChanged();
    }

    public void removeNotification(StatusBarNotification sbn) {
        notifications.remove(sbn);
        notifyDataSetChanged();
    }

    public void setNotifications(List<StatusBarNotification> sbns) {
        notifications.clear();
        notifications.addAll(sbns);
        notifyDataSetChanged();

        Log.d(TAG, "setNotifications: Added " + notifications.size() + " notifications.");
        for (StatusBarNotification sbn : notifications) {
            Log.d(TAG, "setNotifications: " + sbn.getNotification().flags);
            Log.d(TAG, "setNotifications: " + sbn.getPackageName() + ", " + sbn.getNotification().extras);
        }
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View v = new NotiDrawer(container.getContext(), new OpenWatchNotification(container.getContext(), notifications.get(position))).createContentView();
        container.addView(v);
        return v;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public int getCount() {
        return notifications.size();
    }
}
