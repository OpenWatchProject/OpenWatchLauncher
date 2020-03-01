package com.openwatchproject.launcher.activity;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.openwatchproject.launcher.ClockSkin;
import com.openwatchproject.launcher.adapter.ClockSkinPagerAdapter;
import com.openwatchproject.launcher.R;
import com.openwatchproject.launcher.view.ClockSkinView;

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
        ClockSkinView csv = findViewById(R.id.csv);
        ClockSkinPagerAdapter clockSkinChooserAdapter = new ClockSkinPagerAdapter(getClockSkinsInfo(), item -> {
            Log.d(TAG, "onClick: " + item.getFile().getName());
            csv.setClockSkin(item);
            clockskinViewPager.setVisibility(View.GONE);
        });
        clockskinViewPager.setAdapter(clockSkinChooserAdapter);
    }

    private List<ClockSkin> getClockSkinsInfo() {
        List<ClockSkin> clockSkins = new ArrayList<>();

        File clockskinFolder = new File(Environment.getExternalStorageDirectory(), "clockskin/");
        File[] fs = clockskinFolder.listFiles();

        if (fs != null) {
            for (File f : fs) {
                ClockSkin clockSkin = new ClockSkin(f);
                Log.d(TAG, "getClockSkinsInfo: new clockskin!");
                if (clockSkin.isValid()) {
                    Log.d(TAG, "getClockSkinsInfo: is valid!");
                    clockSkins.add(clockSkin);
                }
            }
        }

        return clockSkins;
    }
}
