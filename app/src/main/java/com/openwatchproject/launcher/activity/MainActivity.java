package com.openwatchproject.launcher.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.KeyEvent;

import androidx.appcompat.app.AppCompatActivity;

import com.openwatchproject.launcher.adapter.HorizontalViewPagerAdapter;
import com.openwatchproject.launcher.databinding.ActivityMainBinding;
import com.openwatchproject.launcher.view.HorizontalViewPager;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private HorizontalViewPager horizontalViewPager;

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                showWatchFace();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        horizontalViewPager = binding.horizontalViewPager;
        final HorizontalViewPagerAdapter pagerAdapter = new HorizontalViewPagerAdapter(getSupportFragmentManager());
        horizontalViewPager.setAdapter(pagerAdapter);
        horizontalViewPager.setCurrentItem(1);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_MAIN)) {
            showWatchFace();
        }

        super.onNewIntent(intent);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            showWatchFace();
            return true;
        }

        return super.onKeyUp(keyCode, event);
    }

    private void showWatchFace() {
        horizontalViewPager.setPage(1, 1);
    }
}
