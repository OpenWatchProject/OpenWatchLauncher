package com.openwatchproject.launcher.fragment;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Environment;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.openwatchproject.launcher.R;
import com.openwatchproject.launcher.activity.ClockSkinChooserActivity;
import com.openwatchproject.launcher.model.WearWatchFace;
import com.openwatchproject.watchface.OpenWatchWatchFace;
import com.openwatchproject.watchface.OpenWatchWatchFaceFile;
import com.openwatchproject.watchface.OpenWatchWatchFaceView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ClockSkinFragment extends Fragment {
    private static final String TAG = "ClockSkinFragment";
    private static final int REQUEST_CODE_CHOOSE_CLOCK_SKIN = 1;

    private OpenWatchWatchFaceView clockSkinView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return new OpenWatchWatchFaceView(container.getContext());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        clockSkinView = (OpenWatchWatchFaceView) view;
        clockSkinView.setOnClickListener(clockSkinView.onClickListener); // TODO: FIX THIS! DOESN'T WORK IF ATTACHED FROM INSIDE.
        clockSkinView.setOnLongClickListener(view1 -> {
            Intent clockSkinChooserIntent = new Intent(getContext(), ClockSkinChooserActivity.class);
            final OpenWatchWatchFace currentWatchFace = clockSkinView.getWatchFace();
            if (currentWatchFace != null) {
                clockSkinChooserIntent.putExtra(ClockSkinChooserActivity.EXTRA_CURRENT_CLOCKSKIN, currentWatchFace.getAbsolutePath());
            }
            startActivityForResult(clockSkinChooserIntent, REQUEST_CODE_CHOOSE_CLOCK_SKIN);
            return true;
        });

        loadWatchFace();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_CHOOSE_CLOCK_SKIN) {
            if (resultCode == Activity.RESULT_OK) {
                String watchFacePath = data.getStringExtra(ClockSkinChooserActivity.RESULT_WATCH_FACE_PATH);
                Log.d(TAG, "Selected clockskin: " + watchFacePath);

                SharedPreferences.Editor sharedPrefsEditor = getContext().getSharedPreferences(ClockSkinFragment.class.getName(), Context.MODE_PRIVATE).edit();
                sharedPrefsEditor.putString("lastWatchFace", watchFacePath);
                sharedPrefsEditor.apply();

                OpenWatchWatchFaceFile watchFaceFile = new OpenWatchWatchFaceFile(watchFacePath);
                clockSkinView.setWatchFace(watchFaceFile.getWatchFace(getResources()));
                watchFaceFile.close();
            } else {
                Log.d(TAG, "No clockskin selected");
            }
        }
    }

    public List<WearWatchFace> getWearWatchFaces() {
        Intent i = new Intent(WallpaperService.SERVICE_INTERFACE);
        i.addCategory("com.google.android.wearable.watchface.category.WATCH_FACE");

        List<ResolveInfo> availableWatchFaces = getContext().getPackageManager()
                .queryIntentServices(i, PackageManager.GET_META_DATA);
        List<WearWatchFace> wearWatchFaces = new ArrayList<>();

        for (ResolveInfo service : availableWatchFaces) {
            WearWatchFace wearWatchFace = new WearWatchFace(getContext(), service);
            wearWatchFaces.add(wearWatchFace);
        }

        return wearWatchFaces;
    }

    public void loadWatchFace() {
        SharedPreferences sharedPrefs = getContext().getSharedPreferences(ClockSkinFragment.class.getName(), Context.MODE_PRIVATE);
        String lastWatchFace = sharedPrefs.getString("lastWatchFace", null);
        if (lastWatchFace != null) {
            File f = new File(lastWatchFace);
            if (f.exists()) {
                OpenWatchWatchFaceFile watchFaceFile = new OpenWatchWatchFaceFile(new File(lastWatchFace));
                clockSkinView.setWatchFace(watchFaceFile.getWatchFace(getResources()));
                watchFaceFile.close();
                return;
            }
        }

        File clockskinFolder = new File(Environment.getExternalStorageDirectory(), "clockskin/");
        if (clockskinFolder.exists()) {
            File[] fs = clockskinFolder.listFiles();
            if (fs != null && fs.length > 0) {
                OpenWatchWatchFaceFile watchFaceFile = new OpenWatchWatchFaceFile(fs[0]);
                clockSkinView.setWatchFace(watchFaceFile.getWatchFace(getResources()));
                watchFaceFile.close();
                return;
            }
        }

        Toast.makeText(getContext(), "No valid ClockSkin was found.", Toast.LENGTH_LONG).show();
    }
}
