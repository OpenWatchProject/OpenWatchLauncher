package org.openwatchproject.launcher.notification;

import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.openwatchproject.launcher.fragment.NotificationsFragment;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    private List<OpenWatchNotification> notifications;
    private NotificationsFragment.NotificationCallback callback;

    public NotificationAdapter(NotificationsFragment.NotificationCallback callback, List<OpenWatchNotification> notifications) {
        this.notifications = notifications;
        this.callback = callback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        FrameLayout frameLayout = new FrameLayout(parent.getContext());
        frameLayout.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        return new ViewHolder(frameLayout);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
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
            View view = own.getView(frameLayout.getContext());
            if (view.getParent() != null) {
                ((FrameLayout) view.getParent()).removeView(view);
            } else {
                view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                adjustInset(view);
            }
            frameLayout.addView(view);
        }
    }

    private static void adjustInset(View v) {
        Resources res = v.getContext().getResources();
        if (res.getConfiguration().isScreenRound()) {
            DisplayMetrics dm = res.getDisplayMetrics();
            int p1 = (int) (0.146467f * Math.min(dm.widthPixels, dm.heightPixels));
            int p2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, res.getDisplayMetrics());
            v.setPadding(p1, p2, p1, p1);
        }
    }
}
