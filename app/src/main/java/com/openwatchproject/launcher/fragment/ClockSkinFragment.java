package com.openwatchproject.launcher.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Environment;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.openwatchproject.launcher.ClockSkin;
import com.openwatchproject.launcher.ClockSkinConstants;
import com.openwatchproject.launcher.R;
import com.openwatchproject.launcher.activity.ClockSkinChooserActivity;
import com.openwatchproject.launcher.model.ClockInfo;
import com.openwatchproject.launcher.model.WearWatchFace;
import com.openwatchproject.launcher.view.ClockSkinView;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.IOException;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ClockSkinFragment extends Fragment {
    private static final String TAG = "ClockSkinFragment";
    public static final int REQUEST_CODE_CHOOSE_CLOCK_SKIN = 1;

    private ClockSkinView clockSkinView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return new ClockSkinView(container.getContext());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.clockSkinView = (ClockSkinView) view;
        this.clockSkinView.setOnLongClickListener(view1 -> {
            Intent clockskinChooserIntent = new Intent(getContext(), ClockSkinChooserActivity.class);
            startActivityForResult(clockskinChooserIntent, REQUEST_CODE_CHOOSE_CLOCK_SKIN);
            return true;
        });

        loadWatchFace();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_CHOOSE_CLOCK_SKIN) {
            if (resultCode == Activity.RESULT_OK) {
                String clockskinPath = data.getStringExtra(ClockSkinChooserActivity.RESULT_CLOCKSKIN_PATH);
                Log.d(TAG, "Selected clockskin: " + clockskinPath);

                ClockSkin clockSkin = new ClockSkin(new File(clockskinPath));
                clockSkinView.setClockSkin(clockSkin);
            } else {
                Log.d(TAG, "No clockskin selected");
            }
        }
    }

    private Point getDisplaySize() {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        return size;
    }

    private int getMinDisplaySize() {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        return Math.min(size.x, size.y);
    }

    @DrawableRes
    public int getDrawableRes(String name) {
        try {
            return R.drawable.class.getField(name).getInt(null);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            return getResources().getIdentifier(name, "drawable", getContext().getPackageName());
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

    private Bitmap getClockskinPreview(File clockskinFile) {
        Bitmap preview = null;

        if (clockskinFile.isDirectory()) {
            preview = BitmapFactory.decodeFile(new File(clockskinFile, ClockSkinConstants.CLOCK_SKIN_PREVIEW).getAbsolutePath());
        } else {
            try (ZipFile clockskinZip = new ZipFile(clockskinFile)) {
                Enumeration<? extends ZipEntry> clockskinEntries = clockskinZip.entries();
                while (clockskinEntries.hasMoreElements()) {
                    ZipEntry clockskinEntry = clockskinEntries.nextElement();
                    if (clockskinEntry.getName().equals(ClockSkinConstants.CLOCK_SKIN_PREVIEW)) {
                        preview = BitmapFactory.decodeStream(clockskinZip.getInputStream(clockskinEntry));
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "getClockskinPreview: Unable to decode preview", e);
            }
        }

        return preview;
    }

    public void loadWatchFace() {
        File clockskinFolder = new File(Environment.getExternalStorageDirectory(), "clockskin/");
        File[] fs = clockskinFolder.listFiles();
        ClockSkin clockSkin = new ClockSkin(fs[0]);
        clockSkinView.setClockSkin(clockSkin);
    }

    private ClockInfo parseDrawable(Element drawable) {
        ClockInfo clockInfo = new ClockInfo();

        Element element = (Element) drawable;
        NodeList elementNodes = element.getChildNodes();
        for (int j = 0; j < elementNodes.getLength(); j++) {
            Node n = elementNodes.item(j);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Log.d(TAG, "\t" + n.getNodeName() + ": " + n.getTextContent());
            }
        }

        return clockInfo;
    }

}
