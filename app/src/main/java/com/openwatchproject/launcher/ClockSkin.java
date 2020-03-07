package com.openwatchproject.launcher;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ClockSkin {
    private static final String TAG = "ClockSkin";

    private File file;
    private Bitmap preview;

    private List<ClockSkinItem> clockSkinItems;
    private List<ClockSkinItem> touchClockSkinItems;
    private int width;
    private int height;

    public ClockSkin(File file) {
        this.file = file;

        if (file.isFile()) {
            try (ZipFile clockSkinZip = new ZipFile(file)) {
                ZipEntry previewEntry = clockSkinZip.getEntry(ClockSkinConstants.CLOCK_SKIN_PREVIEW);
                if (previewEntry != null) {
                    try (InputStream is = clockSkinZip.getInputStream(previewEntry)) {
                        this.preview = BitmapFactory.decodeStream(is);
                    } catch (NullPointerException e) {
                        Log.d(TAG, "ClockSkin: ");
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "getClockSkinPreview: Unable to decode preview", e);
            }
        } else {
            this.preview = BitmapFactory.decodeFile(new File(file, ClockSkinConstants.CLOCK_SKIN_PREVIEW).getAbsolutePath());
        }
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public File getFile() {
        return file;
    }

    public Bitmap getPreview() {
        return preview;
    }

    public InputStream getClockSkinFile(String name) {
        if (file.isFile()) {
            try (ZipFile clockSkinZip = new ZipFile(file)) {
                return clockSkinZip.getInputStream(clockSkinZip.getEntry(name));
            } catch (IOException | NullPointerException e) {
                Log.d(TAG, "getClockSkinFile: file not found: " + name);
                return null;
            }
        } else {
            try {
                return new FileInputStream(new File(file, name));
            } catch (FileNotFoundException e) {
                Log.d(TAG, "getClockSkinFile: file not found: " + name);
                return null;
            }
        }
    }

    public Drawable getDrawable(Context context, String name) {
        Bitmap bitmap = BitmapFactory.decodeFile(new File(getFile(), name).getAbsolutePath());
        return new BitmapDrawable(context.getResources(), bitmap);
    }

    public void addClockSkinItem(ClockSkinItem clockSkinItem) {
        if (clockSkinItem.getArrayType() == 100) {
            if (touchClockSkinItems == null) {
                touchClockSkinItems = new ArrayList<>();
            }

            touchClockSkinItems.add(clockSkinItem);
        } else {
            if (clockSkinItems == null) {
                clockSkinItems = new ArrayList<>();
            }

            clockSkinItems.add(clockSkinItem);
        }
    }

    public List<ClockSkinItem> getClockSkinItems() {
        return clockSkinItems;
    }

    public List<ClockSkinItem> getTouchClockSkinItems() {
        return touchClockSkinItems;
    }

    public boolean isValid() {
        return file.exists() && (!file.isDirectory() || file.listFiles().length > 2) && preview != null;
    }
}
