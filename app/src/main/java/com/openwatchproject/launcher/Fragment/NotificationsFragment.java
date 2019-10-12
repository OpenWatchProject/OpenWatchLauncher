package com.openwatchproject.launcher.Fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.openwatchproject.launcher.NLService;
import com.openwatchproject.launcher.R;

public class NotificationsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notifications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        IntentFilter notificationFilter = new IntentFilter();
        notificationFilter.addAction(NLService.ACTION_NOTIFICATION_POSTED);
        notificationFilter.addAction(NLService.ACTION_NOTIFICATION_REMOVED);
        notificationFilter.addAction(NLService.ACTION_NOTIFICATION_RANKING_UPDATE);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(notificationReceiver, notificationFilter);
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(notificationReceiver);
        super.onDestroy();
    }

    BroadcastReceiver notificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case NLService.ACTION_NOTIFICATION_POSTED:

                    break;
                case NLService.ACTION_NOTIFICATION_REMOVED:

                    break;
                case NLService.ACTION_NOTIFICATION_RANKING_UPDATE:

                    break;
            }
        }
    };
}
