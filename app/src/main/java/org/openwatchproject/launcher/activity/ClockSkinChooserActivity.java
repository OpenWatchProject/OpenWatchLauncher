package org.openwatchproject.launcher.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;
import androidx.viewpager.widget.ViewPager;

import org.openwatchproject.launcher.R;
import org.openwatchproject.launcher.adapter.ClockSkinPagerAdapter;
import org.openwatchproject.launcher.persistence.StorageManager;
import org.openwatchproject.openwatchfaceview.OpenWatchFaceConstants;
import org.openwatchproject.openwatchfaceview.OpenWatchFaceFile;

import java.util.ArrayList;
import java.util.List;

public class ClockSkinChooserActivity extends OpenWatchActivity {
    private static final String TAG = "ClockSkinChooserActivit";
    
    public static final String EXTRA_CURRENT_CLOCKSKIN = "CURRENT_CLOCKSKIN";
    public static final String RESULT_WATCH_FACE_PATH = "CLOCK_SKIN_PATH";

    private ViewPager clockskinViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clock_skin_chooser);
        clockskinViewPager = findViewById(R.id.clockskin_viewpager);

        DocumentFile watchfaceFolder = getLauncher().getStorageManager().getWatchfaceFolder();
        if (watchfaceFolder != null) {
            loadWatchfaces(watchfaceFolder);
        } else {
            // TODO: Tell the user what's happening
            StorageManager.openDirectory(this, DIRECTORY_REQUEST, null);
        }
    }

    private void loadWatchfaces(DocumentFile watchfaceFolder) {
        final ClockSkinPagerAdapter clockSkinChooserAdapter = new ClockSkinPagerAdapter(getClockSkinsInfo(watchfaceFolder), item -> {
            Intent i = new Intent();
            i.setData(item.getFile().getUri());
            setResult(RESULT_OK, i);
            finish();
        });

        clockskinViewPager.setAdapter(clockSkinChooserAdapter);
        clockskinViewPager.setCurrentItem(clockSkinChooserAdapter.getItemPosition(getIntent().getStringExtra(EXTRA_CURRENT_CLOCKSKIN)));
    }

    private static final int DIRECTORY_REQUEST = 1;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == DIRECTORY_REQUEST) {
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "onActivityResult: Directory permissions OK!");
                if (data != null) {
                    getLauncher().getStorageManager().setWatchfaceFolder(data);
                    loadWatchfaces(getLauncher().getStorageManager().getWatchfaceFolder());
                } else {
                    Log.d(TAG, "onActivityResult: data is null!");
                    Toast.makeText(this, "Something went wrong...", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.d(TAG, "onActivityResult: Failed to get directory permissions!");
                Toast.makeText(this, "Storage permissions are required!", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private List<OpenWatchFaceFile> getClockSkinsInfo(DocumentFile watchfaceFolder) {
        List<OpenWatchFaceFile> clockSkins = new ArrayList<>();

        DocumentFile[] files = watchfaceFolder.listFiles();
        if (files != null) {
            for (DocumentFile f : files) {
                if (!f.getName().endsWith(OpenWatchFaceConstants.WATCH_FACE_FILE_EXTENSION)) continue;

                OpenWatchFaceFile clockSkin = new OpenWatchFaceFile(this, f);
                if (clockSkin.isValid()) {
                    clockSkins.add(clockSkin);
                }
                clockSkin.close();
            }
        }

        return clockSkins;
    }
}
