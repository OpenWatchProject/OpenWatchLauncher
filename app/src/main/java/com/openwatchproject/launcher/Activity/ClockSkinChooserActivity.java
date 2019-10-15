package com.openwatchproject.launcher.Activity;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.openwatchproject.launcher.Adapter.ClockSkinPagerAdapter;
import com.openwatchproject.launcher.ClockSkinInfo;
import com.openwatchproject.launcher.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ClockSkinChooserActivity extends AppCompatActivity {
    private static final String TAG = "ClockSkinChooserActivit";

    private ViewPager clockskinViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clock_skin_chooser);

        clockskinViewPager = findViewById(R.id.clockskin_viewpager);
        ClockSkinPagerAdapter clockSkinChooserAdapter = new ClockSkinPagerAdapter(getClockSkinsInfo(), item -> {
            Log.d(TAG, "onClick: " + item.getFile().getName());
        });
        clockskinViewPager.setAdapter(clockSkinChooserAdapter);
    }

    private List<ClockSkinInfo> getClockSkinsInfo() {
        List<ClockSkinInfo> clockSkinInfos = new ArrayList<>();

        File clockskinFolder = new File(Environment.getExternalStorageDirectory(), "clockskin/");

        for (File f : clockskinFolder.listFiles()) {
            ClockSkinInfo clockSkinInfo = new ClockSkinInfo(f);
            if (clockSkinInfo.isValid()) {
                clockSkinInfos.add(clockSkinInfo);
            }
        }

        return clockSkinInfos;
    }
}
