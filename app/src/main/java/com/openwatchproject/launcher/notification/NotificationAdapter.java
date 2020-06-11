package com.openwatchproject.launcher.notification;

import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.openwatchproject.launcher.R;
import com.openwatchproject.launcher.fragment.NotificationsFragment;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    private static final String TAG = "NotificationAdapter";

    private List<OpenWatchNotification> notifications;
    private NotificationsFragment.NotificationCallback callback;

    public NotificationAdapter(NotificationsFragment.NotificationCallback callback) {
        this.notifications = new ArrayList<>();
        this.callback = callback;
    }

    public void addNotification(OpenWatchNotification own) {
        Log.d(TAG, "Adding " + own.getPackageName() + " notification...");
        boolean added = false;
        for (int i = 0; i < notifications.size(); i++) {
            if (notifications.get(i).equals(own)) {
                Log.d(TAG, "Replacing existing notification for " + own.getPackageName());
                notifications.set(i, own);
                added = true;
                break;
            }
        }

        if (!added) {
            Log.d(TAG, "addNotification: Adding new notification for " + own.getPackageName());
            notifications.add(own);
        }

        notifyDataSetChanged();
    }

    public void removeNotification(OpenWatchNotification own) {
        notifications.remove(own);
        notifyDataSetChanged();
    }

    public void setNotifications(List<OpenWatchNotification> owns) {
        notifications.clear();
        Log.d(TAG, "setNotifications: Adding " + owns.size() + " notifications");
        notifications.addAll(owns);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        FrameLayout frameLayout = new FrameLayout(parent.getContext());
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        frameLayout.setLayoutParams(layoutParams);
        return new ViewHolder(frameLayout);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: " + position);
        holder.setNotification(notifications.get(position));
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private FrameLayout frameLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            frameLayout = (FrameLayout) itemView;
        }

        public void setNotification(OpenWatchNotification own) {
            frameLayout.removeAllViews();
            View view = new NotiDrawer(frameLayout.getContext(), own).createContentView();
            view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            adjustInset(view);
            frameLayout.addView(view);
        }
    }

    private static void adjustInset(View v) {
        Resources res = v.getContext().getResources();
        //if (res.getConfiguration().isScreenRound()) {
        DisplayMetrics dm = res.getDisplayMetrics();
        int p1 = (int) (0.146467f * (float) dm.widthPixels);
        int p2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, res.getDisplayMetrics());
        v.setPadding(p1, p2, p1, p1);
        //}
    }
}
