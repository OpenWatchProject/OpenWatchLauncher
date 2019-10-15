package com.openwatchproject.launcher;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ClockSkinInfo {
    private static final String TAG = "ClockSkinInfo";

    private File file;
    private boolean isZipped;
    private Bitmap preview;

    public ClockSkinInfo(File file) {
        this.file = file;

        if (file.isDirectory()) {
            this.isZipped = false;
            this.preview = BitmapFactory.decodeFile(new File(file, ClockSkinConstants.CLOCK_SKIN_PREVIEW).getAbsolutePath());
        } else {
            this.isZipped = true;
            try (ZipFile clockskinZip = new ZipFile(file)) {
                Enumeration<? extends ZipEntry> clockskinEntries = clockskinZip.entries();
                while (clockskinEntries.hasMoreElements()) {
                    ZipEntry clockskinEntry = clockskinEntries.nextElement();
                    if (clockskinEntry.getName().equals(ClockSkinConstants.CLOCK_SKIN_PREVIEW)) {
                        this.preview = BitmapFactory.decodeStream(clockskinZip.getInputStream(clockskinEntry));
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "getClockskinPreview: Unable to decode preview", e);
            }
        }
    }

    public File getFile() {
        return file;
    }

    public boolean isZipped() {
        return isZipped;
    }

    public Bitmap getPreview() {
        return preview;
    }

    public boolean isValid() {
        if (isZipped) {
            return file.exists();
        } else {
            return file.list().length > 0;
        }
    }
}
