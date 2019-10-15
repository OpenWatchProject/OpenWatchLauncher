package com.openwatchproject.launcher.Model;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.service.wallpaper.WallpaperService;

import androidx.annotation.DrawableRes;

public class WearWatchFace {
    public static final String PREVIEW = "com.google.android.wearable.watchface.preview";
    public static final String PREVIEW_CIRCULAR = "com.google.android.wearable.watchface.preview_circular";
    public static final String WEARABLE_CONFIGURATION_ACTION = "com.google.android.wearable.watchface.wearableConfigurationAction";
    public static final String COMPANION_CONFIGURATION_ACTION = "com.google.android.wearable.watchface.companionConfigurationAction";

    private String serviceName;
    private XmlResourceParser wallpaper;
    private Drawable preview;
    private String wearableConfigurationAction;
    private String companionConfigurationAction;

    public WearWatchFace(Context context, ResolveInfo resolveInfo) {
        PackageManager pm = context.getPackageManager();
        Bundle metaData = resolveInfo.serviceInfo.metaData;

        serviceName = resolveInfo.serviceInfo.name;

        wallpaper = pm.getXml(resolveInfo.serviceInfo.packageName,
                metaData.getInt(WallpaperService.SERVICE_META_DATA),
                resolveInfo.serviceInfo.applicationInfo);

        @DrawableRes int previewResId;
        if (context.getResources().getConfiguration().isScreenRound()) {
            previewResId = metaData.getInt(PREVIEW_CIRCULAR);
            if (previewResId == 0) {
                previewResId = metaData.getInt(PREVIEW);
            }
        } else {
            previewResId = metaData.getInt(PREVIEW);
            if (previewResId == 0) {
                previewResId = metaData.getInt(PREVIEW_CIRCULAR);
            }
        }

        preview = pm.getDrawable(resolveInfo.serviceInfo.packageName,
                previewResId,
                resolveInfo.serviceInfo.applicationInfo);

        wearableConfigurationAction = metaData.getString(WEARABLE_CONFIGURATION_ACTION);

        companionConfigurationAction = metaData.getString(COMPANION_CONFIGURATION_ACTION);
    }
}
