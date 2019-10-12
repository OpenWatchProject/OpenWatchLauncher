package com.openwatchproject.launcher.Persistence;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.openwatchproject.launcher.Model.Recent;

@Database(entities = {Recent.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract RecentDao recentDao();
}
