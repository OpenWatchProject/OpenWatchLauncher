package org.openwatchproject.launcher.fragment;

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

import org.openwatchproject.launcher.OpenWatchLauncher;
import org.openwatchproject.launcher.R;
import org.openwatchproject.launcher.notification.NotificationAdapter;
import org.openwatchproject.launcher.notification.NotificationHelper;
import org.openwatchproject.launcher.notification.OpenWatchNotification;

public class NotificationsFragment extends Fragment {
    private static final String TAG = "NotificationsFragment";

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

            notificationsAdapter.notifyDataSetChanged();
            setNotificationVisibility(notificationsAdapter.getItemCount() != 0);
        }

        @Override
        public void onNotificationRemoved(OpenWatchNotification own) {
            Log.d(TAG, "onNotificationRemoved");

            notificationsAdapter.notifyDataSetChanged();
            setNotificationVisibility(notificationsAdapter.getItemCount() != 0);
        }

        @Override
        public void onNotificationRankingUpdated(NotificationListenerService.RankingMap rankingMap) {
            Log.d(TAG, "onNotificationRankingUpdated");
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notifications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        noNotificationsText = view.findViewById(R.id.no_notifications_text);
        notificationsRecyclerView = view.findViewById(R.id.notifications_recycler_view);

        notificationsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        new LinearSnapHelper().attachToRecyclerView(notificationsRecyclerView);

        notificationsRecyclerView.setAdapter(notificationsAdapter);
        setNotificationVisibility(notificationsAdapter.getItemCount() != 0);
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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        notificationHelper = ((OpenWatchLauncher) getActivity().getApplication()).getNotificationHelper();
        notificationsAdapter = new NotificationAdapter(callback, notificationHelper.getPostedNotifications());
        notificationHelper.addNotificationListener(listener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        notificationHelper.removeNotificationListener(listener);
    }

    public interface NotificationCallback {
        void removeNotification(StatusBarNotification sbn);
    }
}
