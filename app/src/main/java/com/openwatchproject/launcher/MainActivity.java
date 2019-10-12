package com.openwatchproject.launcher;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;

import com.openwatchproject.launcher.Adapter.HorizontalViewPagerAdapter;
import com.openwatchproject.launcher.View.HorizontalViewPager;

public class MainActivity extends AppCompatActivity {

    private HorizontalViewPager horizontalViewPager;
    private PagerAdapter pagerAdapter;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        horizontalViewPager = findViewById(R.id.horizontal_view_pager);
        pagerAdapter = new HorizontalViewPagerAdapter(getSupportFragmentManager());
        horizontalViewPager.setAdapter(pagerAdapter);
        horizontalViewPager.setCurrentItem(1);
    }
}
