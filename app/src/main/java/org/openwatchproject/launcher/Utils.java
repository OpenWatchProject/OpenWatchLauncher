package org.openwatchproject.launcher;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.core.graphics.drawable.DrawableCompat;

import java.io.File;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class Utils {
    private static final String PREFERENCES_FILE = "materialsample_settings";

    public static boolean isSystemApp(ApplicationInfo ai) {
        int mask = ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP;
        return (ai.flags & mask) != 0;
    }

    public static Drawable tintMyDrawable(Drawable drawable, int color) {
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, color);
        DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_IN);
        return drawable;
    }

    public static void saveSharedSetting(Context ctx, String settingName, String settingValue) {
        SharedPreferences sharedPref = ctx.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(settingName, settingValue);
        editor.apply();
    }

    public static void deleteFile(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                deleteFile(f);
            }
        }
        file.delete();
    }

    public static void fixRoundScreenWidth(View v) {
        v.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                v.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                int[] location = new int[2];
                v.getLocationOnScreen(location);
                int displayWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
                int intersection = findIntersection(location[1], ((float) displayWidth) / 2.0f);
                ViewGroup.LayoutParams lp = v.getLayoutParams();
                lp.width = 2 * intersection;
                v.setLayoutParams(lp);
            }
        });
    }

    public static int findIntersection(int y, float radius) {
        return (int) Math.round(Math.sqrt(Math.pow(radius, 2) - Math.pow(radius - y, 2)));
    }

    public static String getApplicationName(Context context, String packageName) {
        final PackageManager packageManager = context.getPackageManager();

        try {
            final ApplicationInfo ai = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);

            if (ai.labelRes == 0) {
                return ai.nonLocalizedLabel.toString();
            } else {
                final Resources appRes = packageManager.getResourcesForApplication(packageName);
                return appRes.getString(ai.labelRes);
            }
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    public static int getTargetSdkVersion(Context context, String packageName) {
        final PackageManager packageManager = context.getPackageManager();

        try {
            final PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            return packageInfo.applicationInfo.targetSdkVersion;
        }
        catch (PackageManager.NameNotFoundException e) {
            return Build.VERSION_CODES.Q;
        }
    }

    public static NetworkGeneration getNetworkType(int networkType) {
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
            case TelephonyManager.NETWORK_TYPE_GSM:
                return NetworkGeneration.GENERATION_2;
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
            case TelephonyManager.NETWORK_TYPE_TD_SCDMA:
                return NetworkGeneration.GENERATION_3;
            case TelephonyManager.NETWORK_TYPE_LTE:
            case TelephonyManager.NETWORK_TYPE_IWLAN:
                return NetworkGeneration.GENERATION_4;
            case TelephonyManager.NETWORK_TYPE_NR:
                return NetworkGeneration.GENERATION_5;
        }

        return NetworkGeneration.GENERATION_UNKNOWN;
    }

    public enum NetworkGeneration {
        GENERATION_UNKNOWN,
        GENERATION_2,
        GENERATION_3,
        GENERATION_4,
        GENERATION_5,
    }
}
