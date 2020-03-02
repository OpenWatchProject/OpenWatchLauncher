package com.openwatchproject.launcher.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.openwatchproject.launcher.ClockSkin;
import com.openwatchproject.launcher.R;
import com.openwatchproject.launcher.adapter.ClockSkinPagerAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ClockSkinChooserActivity extends AppCompatActivity {
    public static final String RESULT_CLOCKSKIN_PATH = "clockskinPath";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clock_skin_chooser);

        setResult(RESULT_CANCELED);

        ViewPager clockskinViewPager = findViewById(R.id.clockskin_viewpager);
        ClockSkinPagerAdapter clockSkinChooserAdapter = new ClockSkinPagerAdapter(getClockSkinsInfo(), item -> {
            Intent i = new Intent();
            i.putExtra(RESULT_CLOCKSKIN_PATH, item.getFile().getAbsolutePath());
            setResult(RESULT_OK, i);
            finish();
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
                if (clockSkin.isValid()) {
                    clockSkins.add(clockSkin);
                }
            }
        }

        return clockSkins;
    }
}
