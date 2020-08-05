package com.openwatchproject.launcher.activity;

import android.content.Intent;
import android.content.UriPermission;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;
import androidx.viewpager.widget.ViewPager;

import com.openwatchproject.launcher.OpenWatchLauncher;
import com.openwatchproject.launcher.adapter.ClockSkinPagerAdapter;
import com.openwatchproject.launcher.databinding.ActivityClockSkinChooserBinding;
import com.openwatchproject.launcher.persistence.StorageManager;
import com.openwatchproject.watchface.OpenWatchWatchFace;
import com.openwatchproject.watchface.OpenWatchWatchFaceConstants;
import com.openwatchproject.watchface.OpenWatchWatchFaceFile;

import java.io.File;
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
        final ActivityClockSkinChooserBinding binding = ActivityClockSkinChooserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        clockskinViewPager = binding.clockskinViewpager;

        Uri watchfaceFolder = getLauncher().getStorageManager().getWatchfaceFolder();
        if (watchfaceFolder != null) {
            loadWatchfaces(watchfaceFolder);
        } else {
            // TODO: Tell the user what's happening
            StorageManager.openDirectory(this, DIRECTORY_REQUEST, null);
        }
    }

    private void loadWatchfaces(Uri watchfaceFolder) {
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
                    loadWatchfaces(data.getData());
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

    private List<OpenWatchWatchFaceFile> getClockSkinsInfo(Uri watchfaceFolder) {
        List<OpenWatchWatchFaceFile> clockSkins = new ArrayList<>();

        DocumentFile folder = DocumentFile.fromTreeUri(this, watchfaceFolder);
        DocumentFile[] files = folder.listFiles();

        if (files != null) {
            for (DocumentFile f : files) {
                if (!f.getName().endsWith(OpenWatchWatchFaceConstants.WATCH_FACE_FILE_EXTENSION)) continue;

                OpenWatchWatchFaceFile clockSkin = new OpenWatchWatchFaceFile(this, f);
                if (clockSkin.isValid()) {
                    clockSkins.add(clockSkin);
                }
                clockSkin.close();
            }
        }

        return clockSkins;
    }
}
