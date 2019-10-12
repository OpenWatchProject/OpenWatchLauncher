package com.openwatchproject.launcher.Model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "recents")
public class Recent {

    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "packageName")
    private String packageName;
    @ColumnInfo(name = "lastLaunched")
    private long lastLaunched;

    public Recent(String packageName, long lastLaunched) {
        this.packageName = packageName;
        this.lastLaunched = lastLaunched;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public long getLastLaunched() {
        return lastLaunched;
    }

    public void setLastLaunched(long lastLaunched) {
        this.lastLaunched = lastLaunched;
    }
}
