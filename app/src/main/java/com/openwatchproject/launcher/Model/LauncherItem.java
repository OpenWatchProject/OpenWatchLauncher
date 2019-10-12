package com.openwatchproject.launcher.Model;

import android.graphics.drawable.Drawable;

public class LauncherItem {
    private Drawable icon;
    private String name;
    private String packageName;

    public LauncherItem(Drawable icon, String name, String packageName) {
        this.icon = icon;
        this.name = name;
        this.packageName = packageName;
    }

    public Drawable getIcon() {
        return icon;
    }

    public String getName() {
        return name;
    }

    public String getPackageName() {
        return packageName;
    }
}
