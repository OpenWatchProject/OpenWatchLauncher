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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.openwatchproject.launcher.notification.NotificationHelper;
import com.openwatchproject.launcher.OpenWatchLauncher;
import com.openwatchproject.launcher.notification.NotificationAdapter;
import com.openwatchproject.launcher.databinding.FragmentNotificationsBinding;
import com.openwatchproject.launcher.notification.OpenWatchNotification;

public class NotificationsFragment extends Fragment {
    private static final String TAG = "NotificationsFragment";
    private FragmentNotificationsBinding binding;

    private NotificationHelper notificationHelper;

    private TextView noNotificationsText;
    private RecyclerView notificationsRecyclerView;
    private NotificationAdapter notificationsAdapter;

    private NotificationCallback callback = new NotificationCallback() {
        @Override
        public void removeNotification(StatusBarNotification sbn) {
            Log.d(TAG, "removeNotification");
        }
    };

    private NotificationHelper.NotificationListener listener = new NotificationHelper.NotificationListener() {
        @Override
        public void onNotificationPosted(OpenWatchNotification own) {
            Log.d(TAG, "onNotificationPosted");

            notificationsAdapter.addNotification(own);
            setNotificationVisibility(notificationsAdapter.getItemCount() != 0);
        }

        @Override
        public void onNotificationRemoved(OpenWatchNotification own) {
            Log.d(TAG, "onNotificationRemoved");

            notificationsAdapter.removeNotification(own);
            setNotificationVisibility(notificationsAdapter.getItemCount() != 0);
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
        notificationsRecyclerView = binding.notificationsRecyclerView;
        notificationsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        new LinearSnapHelper().attachToRecyclerView(notificationsRecyclerView);

        notificationsAdapter = new NotificationAdapter(callback);
        notificationsRecyclerView.setAdapter(notificationsAdapter);
    }

    private void setNotificationVisibility(boolean visible) {
        if (visible) {
            notificationsRecyclerView.setVisibility(View.VISIBLE);
            noNotificationsText.setVisibility(View.GONE);
        } else {
            notificationsRecyclerView.setVisibility(View.GONE);
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
        setNotificationVisibility(notificationsAdapter.getItemCount() != 0);
    }

    public interface NotificationCallback {
        void removeNotification(StatusBarNotification sbn);
    }
}
