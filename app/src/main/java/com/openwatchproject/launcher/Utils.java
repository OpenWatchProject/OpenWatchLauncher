package com.openwatchproject.launcher;

import android.content.pm.ApplicationInfo;

public class Utils {
    public static boolean isSystemApp(ApplicationInfo ai) {
        int mask = ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP;
        return (ai.flags & mask) != 0;
    }
}
