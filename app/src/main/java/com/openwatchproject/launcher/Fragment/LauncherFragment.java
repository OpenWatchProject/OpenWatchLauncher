package com.openwatchproject.launcher.Fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.room.Room;
import androidx.wear.widget.WearableLinearLayoutManager;
import androidx.wear.widget.WearableRecyclerView;

import com.openwatchproject.launcher.Adapter.LauncherAdapter;
import com.openwatchproject.launcher.Listener.LauncherItemClickListener;
import com.openwatchproject.launcher.Listener.LauncherItemLongClickListener;
import com.openwatchproject.launcher.Model.LauncherItem;
import com.openwatchproject.launcher.Model.Recent;
import com.openwatchproject.launcher.Persistence.AppDatabase;
import com.openwatchproject.launcher.R;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

import static com.openwatchproject.launcher.Utils.isSystemApp;

public class LauncherFragment extends Fragment {
    private static final String TAG = "LauncherFragment";

    private WearableRecyclerView wearableRecyclerView;
    private LauncherAdapter launcherAdapter;

    private SharedPreferences sharedPrefs;
    private AppDatabase db;

    private BroadcastReceiver appChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            launcherAdapter.submitList(getLauncherItems());
        }
    };

    private LauncherItemClickListener launcherItemClickListener = item -> {
        String packageName = item.getPackageName();

        Intent i = getContext().getPackageManager()
                .getLaunchIntentForPackage(packageName);
        getContext().startActivity(i);

        db.recentDao().insertRecent(new Recent(packageName, new Date().getTime()))
                .subscribeOn(Schedulers.io()).subscribe(new CompletableObserver() {
            @Override
            public void onSubscribe(Disposable d) {
                // Unused
            }

            @Override
            public void onComplete() {
                launcherAdapter.submitList(getLauncherItems());
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "Error while updating recents list", e);
                Toast.makeText(getContext(), "Error while updating recents list!", Toast.LENGTH_SHORT).show();
            }
        });
    };

    private LauncherItemLongClickListener launcherItemLongClickListener = item -> {
        try {
            PackageManager pm = getContext().getPackageManager();
            ApplicationInfo ai = pm.getApplicationInfo(item.getPackageName(), 0);
            if (isSystemApp(ai)) return;
        } catch (PackageManager.NameNotFoundException e) {
            return;
        }

        Intent i = new Intent(Intent.ACTION_UNINSTALL_PACKAGE);
        i.setData(Uri.parse("package:" + item.getPackageName()));
        getContext().startActivity(i);
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        db = Room.databaseBuilder(getContext().getApplicationContext(), AppDatabase.class, "openwatch-launcher").build();

        launcherAdapter = new LauncherAdapter(getContext(), launcherItemClickListener, launcherItemLongClickListener);
        launcherAdapter.submitList(getLauncherItems());

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        intentFilter.addDataScheme("package");
        getContext().registerReceiver(appChangeReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        getContext().unregisterReceiver(appChangeReceiver);
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_launcher, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        wearableRecyclerView = view.findViewById(R.id.wearable_recycler_view);
        wearableRecyclerView.setEdgeItemsCenteringEnabled(true);
        wearableRecyclerView.setLayoutManager(new WearableLinearLayoutManager(getContext()));
        wearableRecyclerView.setAdapter(launcherAdapter);
        new LinearSnapHelper().attachToRecyclerView(wearableRecyclerView);
    }

    private List<LauncherItem> getLauncherItems() {
        List<LauncherItem> launcherItems = new ArrayList<>();

        PackageManager pm = getContext().getPackageManager();
        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> appList = pm.queryIntentActivities(i, 0);
        String launcherPackageName = getContext().getPackageName();
        for (ResolveInfo app : appList) {
            String packageName = app.activityInfo.packageName;
            if (launcherPackageName.equals(packageName)) continue;
            Drawable icon = app.loadIcon(pm);
            String title = app.loadLabel(pm).toString();
            launcherItems.add(new LauncherItem(icon, title, packageName));
        }

        Collections.sort(launcherItems, (o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));

        if (sharedPrefs.getBoolean("recentsEnabled", true)) {
            int recentCount = Math.min(sharedPrefs.getInt("recentCount", 2),
                    db.recentDao().getRecentsCount().subscribeOn(Schedulers.io()).blockingGet());

            List<Recent> recents = db.recentDao().getRecents().subscribeOn(Schedulers.io())
                    .blockingGet();

            List<LauncherItem> launcherRecents = new ArrayList<>();
            int total = 0;

            for (Recent recent : recents) {
                if (total < recentCount) {
                    boolean found = false;

                    for (LauncherItem launcherItem : launcherItems) {
                        if (recent.getPackageName().equals(launcherItem.getPackageName())) {
                            launcherRecents.add(launcherItem);
                            found = true;
                            total++;
                            break;
                        }
                    }

                    if (!found) {
                        db.recentDao().deleteRecent(recent).subscribeOn(Schedulers.io())
                                .subscribe();
                    }
                } else {
                    break;
                }
            }

            launcherItems.addAll(0, launcherRecents);
        }

        return launcherItems;
    }
}