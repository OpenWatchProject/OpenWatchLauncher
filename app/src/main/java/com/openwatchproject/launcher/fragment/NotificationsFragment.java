package com.openwatchproject.launcher.fragment;

import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.openwatchproject.launcher.notification.NotificationHelper;
import com.openwatchproject.launcher.OpenWatchLauncher;
import com.openwatchproject.launcher.notification.NotificationViewPagerAdapter;
import com.openwatchproject.launcher.databinding.FragmentNotificationsBinding;
import com.openwatchproject.launcher.view.VerticalViewPager;

public class NotificationsFragment extends Fragment {
    private static final String TAG = "NotificationsFragment";
    private FragmentNotificationsBinding binding;

    private NotificationHelper notificationHelper;

    private TextView noNotificationsText;
    private VerticalViewPager notificationsViewPager;
    private NotificationViewPagerAdapter notificationsAdapter;

    private NotificationCallback callback = new NotificationCallback() {
        @Override
        public void removeNotification(StatusBarNotification sbn) {
            Log.d(TAG, "removeNotification");
        }
    };

    private NotificationHelper.NotificationListener listener = new NotificationHelper.NotificationListener() {
        @Override
        public void onNotificationPosted(StatusBarNotification sbn) {
            Log.d(TAG, "onNotificationPosted");

            notificationsAdapter.addNotification(sbn);
            setNotificationVisibility(notificationsAdapter.getCount() != 0);
        }

        @Override
        public void onNotificationRemoved(StatusBarNotification sbn) {
            Log.d(TAG, "onNotificationRemoved");

            notificationsAdapter.removeNotification(sbn);
            setNotificationVisibility(notificationsAdapter.getCount() != 0);
        }

        @Override
        public void onNotificationRankingUpdated(NotificationListenerService.RankingMap rankingMap) {
            Log.d(TAG, "onNotificationRankingUpdated");
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        noNotificationsText = binding.noNotificationsText;
        notificationsViewPager = binding.notificationsViewPager;

        notificationsAdapter = new NotificationViewPagerAdapter(callback);
        notificationsViewPager.setAdapter(notificationsAdapter);
    }

    private void setNotificationVisibility(boolean visible) {
        if (visible) {
            notificationsViewPager.setVisibility(View.VISIBLE);
            noNotificationsText.setVisibility(View.GONE);
        } else {
            notificationsViewPager.setVisibility(View.GONE);
            noNotificationsText.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        notificationHelper.removeNotificationListener(listener);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        notificationHelper = ((OpenWatchLauncher) getActivity().getApplication()).getNotificationHelper();
        notificationsAdapter.setNotifications(notificationHelper.getPostedNotifications());
        notificationHelper.addNotificationListener(listener);
        setNotificationVisibility(notificationsAdapter.getCount() != 0);
    }

    public interface NotificationCallback {
        void removeNotification(StatusBarNotification sbn);
    }
}
