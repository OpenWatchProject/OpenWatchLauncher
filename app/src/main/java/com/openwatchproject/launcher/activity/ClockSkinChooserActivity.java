package com.openwatchproject.launcher.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.openwatchproject.launcher.adapter.ClockSkinPagerAdapter;
import com.openwatchproject.launcher.databinding.ActivityClockSkinChooserBinding;
import com.openwatchproject.watchface.OpenWatchWatchFace;
import com.openwatchproject.watchface.OpenWatchWatchFaceConstants;
import com.openwatchproject.watchface.OpenWatchWatchFaceFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ClockSkinChooserActivity extends AppCompatActivity {
    public static final String EXTRA_CURRENT_CLOCKSKIN = "CURRENT_CLOCKSKIN";
    public static final String RESULT_WATCH_FACE_PATH = "CLOCK_SKIN_PATH";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivityClockSkinChooserBinding binding = ActivityClockSkinChooserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        final ViewPager clockskinViewPager = binding.clockskinViewpager;
        final ClockSkinPagerAdapter clockSkinChooserAdapter = new ClockSkinPagerAdapter(getClockSkinsInfo(), item -> {
            Intent i = new Intent();
            i.putExtra(RESULT_WATCH_FACE_PATH, item.getFile().getAbsolutePath());
            setResult(RESULT_OK, i);
            finish();
        });
        clockskinViewPager.setAdapter(clockSkinChooserAdapter);
        clockskinViewPager.setCurrentItem(clockSkinChooserAdapter.getItemPosition(getIntent().getStringExtra(EXTRA_CURRENT_CLOCKSKIN)));
    }

    private List<OpenWatchWatchFaceFile> getClockSkinsInfo() {
        List<OpenWatchWatchFaceFile> clockSkins = new ArrayList<>();

        File clockSkinFolder = new File(Environment.getExternalStorageDirectory(), "clockskin/");
        File[] files = clockSkinFolder.listFiles();

        if (files != null) {
            for (File f : files) {
                if (!f.getName().endsWith(OpenWatchWatchFaceConstants.WATCH_FACE_FILE_EXTENSION)) continue;

                OpenWatchWatchFaceFile clockSkin = new OpenWatchWatchFaceFile(f);
                if (clockSkin.isValid()) {
                    clockSkins.add(clockSkin);
                }
                clockSkin.close();
            }
        }

        return clockSkins;
    }
}
