package com.openwatchproject.launcher.activity;

import androidx.appcompat.app.AppCompatActivity;

import com.openwatchproject.launcher.OpenWatchLauncher;

public class OpenWatchActivity extends AppCompatActivity {
    public OpenWatchLauncher getLauncher() {
        return (OpenWatchLauncher) getApplication();
    }
}
