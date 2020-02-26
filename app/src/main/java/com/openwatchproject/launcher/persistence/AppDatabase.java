package com.openwatchproject.launcher.persistence;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.openwatchproject.launcher.model.Recent;

@Database(entities = {Recent.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract RecentDao recentDao();
}
