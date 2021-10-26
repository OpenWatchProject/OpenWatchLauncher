package org.openwatchproject.launcher.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.openwatchproject.launcher.activity.ClockSkinChooserActivity;
import org.openwatchproject.launcher.model.WearWatchFace;
import org.openwatchproject.openwatchfaceview.OpenWatchFace;
import org.openwatchproject.openwatchfaceview.OpenWatchFaceFile;
import org.openwatchproject.openwatchfaceview.OpenWatchFaceView;

import java.util.ArrayList;
import java.util.List;

public class ClockSkinFragment extends Fragment {
    private static final String TAG = "ClockSkinFragment";
    private static final int REQUEST_CODE_CHOOSE_CLOCK_SKIN = 1;

    private OpenWatchFaceView clockSkinView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return new OpenWatchFaceView(container.getContext());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        clockSkinView = (OpenWatchFaceView) view;
        clockSkinView.setOnLongClickListener(view1 -> {
            Intent clockSkinChooserIntent = new Intent(getContext(), ClockSkinChooserActivity.class);
            final OpenWatchFace currentWatchFace = clockSkinView.getWatchFace();
            if (currentWatchFace != null) {
                clockSkinChooserIntent.putExtra(ClockSkinChooserActivity.EXTRA_CURRENT_CLOCKSKIN, currentWatchFace.getPath().toString());
            }
            startActivityForResult(clockSkinChooserIntent, REQUEST_CODE_CHOOSE_CLOCK_SKIN);
            return true;
        });

        //loadWatchFace();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_CHOOSE_CLOCK_SKIN) {
            if (resultCode == Activity.RESULT_OK) {
                Uri watchFacePath = data.getData();
                Log.d(TAG, "Selected clockskin: " + watchFacePath);

                SharedPreferences.Editor sharedPrefsEditor = getContext().getSharedPreferences(ClockSkinFragment.class.getName(), Context.MODE_PRIVATE).edit();
                sharedPrefsEditor.putString("lastWatchFace", watchFacePath.toString());
                sharedPrefsEditor.apply();

                OpenWatchFaceFile watchFaceFile = new OpenWatchFaceFile(getContext(), watchFacePath);
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

    /*public void loadWatchFace() {
        SharedPreferences sharedPrefs = getContext().getSharedPreferences(ClockSkinFragment.class.getName(), Context.MODE_PRIVATE);
        String lastWatchFace = sharedPrefs.getString("lastWatchFace", null);
        if (lastWatchFace != null) {
            File f = new File(lastWatchFace);
            if (f.exists()) {
                OpenWatchFaceFile watchFaceFile = new OpenWatchFaceFile(getContext(), Uri.parse(lastWatchFace));
                clockSkinView.setWatchFace(watchFaceFile.getWatchFace(getResources()));
                watchFaceFile.close();
                return;
            }
        }

        File clockskinFolder = new File(Environment.getExternalStorageDirectory(), "clockskin/");
        if (clockskinFolder.exists()) {
            File[] fs = clockskinFolder.listFiles();
            if (fs != null && fs.length > 0) {
                OpenWatchFaceFile watchFaceFile = new OpenWatchFaceFile(fs[0]);
                clockSkinView.setWatchFace(watchFaceFile.getWatchFace(getResources()));
                watchFaceFile.close();
                return;
            }
        }

        Toast.makeText(getContext(), "No valid ClockSkin was found.", Toast.LENGTH_LONG).show();
    }*/
}
