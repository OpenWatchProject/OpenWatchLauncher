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
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ClockSkin {
    private static final String TAG = "ClockSkin";

    private File file;
    private Bitmap preview;

    private ArrayList<ClockSkinItem> clockSkinItems;

    public ClockSkin(File file) {
        this.file = file;

        if (file.isFile()) {
            try (ZipFile clockSkinZip = new ZipFile(file)) {
                ZipEntry previewEntry = clockSkinZip.getEntry(ClockSkinConstants.CLOCK_SKIN_PREVIEW);
                if (previewEntry != null) {
                    try (InputStream is = clockSkinZip.getInputStream(previewEntry)) {
                        this.preview = BitmapFactory.decodeStream(is);
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "getClockSkinPreview: Unable to decode preview", e);
            }
        } else {
            this.preview = BitmapFactory.decodeFile(new File(file, ClockSkinConstants.CLOCK_SKIN_PREVIEW).getAbsolutePath());
        }
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
        if (clockSkinItems == null) {
            clockSkinItems = new ArrayList<>();
        }

        clockSkinItems.add(clockSkinItem);
    }

    public ArrayList<ClockSkinItem> getClockSkinItems() {
        return clockSkinItems;
    }

    public boolean isValid() {
        return file.exists() && (!file.isDirectory() || file.listFiles().length > 2) && preview != null;
    }
}
