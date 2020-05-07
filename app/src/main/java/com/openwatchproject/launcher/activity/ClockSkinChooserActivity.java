package com.openwatchproject.launcher.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.openwatchproject.launcher.ClockSkin;
import com.openwatchproject.launcher.R;
import com.openwatchproject.launcher.adapter.ClockSkinPagerAdapter;
import com.openwatchproject.launcher.databinding.ActivityClockSkinChooserBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ClockSkinChooserActivity extends AppCompatActivity {
    public static final String RESULT_CLOCKSKIN_PATH = "CLOCK_SKIN_PATH";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivityClockSkinChooserBinding binding = ActivityClockSkinChooserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        final ViewPager clockskinViewPager = binding.clockskinViewpager;
        final ClockSkinPagerAdapter clockSkinChooserAdapter = new ClockSkinPagerAdapter(getClockSkinsInfo(), item -> {
            Intent i = new Intent();
            i.putExtra(RESULT_CLOCKSKIN_PATH, item.getFile().getAbsolutePath());
            setResult(RESULT_OK, i);
            finish();
        });
        clockskinViewPager.setAdapter(clockSkinChooserAdapter);
    }

    private List<ClockSkin> getClockSkinsInfo() {
        List<ClockSkin> clockSkins = new ArrayList<>();

        File clockSkinFolder = new File(Environment.getExternalStorageDirectory(), "clockskin/");
        File[] files = clockSkinFolder.listFiles();

        if (files != null) {
            for (File f : files) {
                ClockSkin clockSkin = new ClockSkin(f);
                if (clockSkin.isValid()) {
                    clockSkins.add(clockSkin);
                }
            }
        }

        return clockSkins;
    }
}
