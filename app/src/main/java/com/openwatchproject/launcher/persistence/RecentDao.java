package com.openwatchproject.launcher.persistence;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.openwatchproject.launcher.model.Recent;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

@Dao
public interface RecentDao {

    @Query("SELECT * FROM recents ORDER BY lastLaunched DESC")
    Single<List<Recent>> getRecents();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertRecent(Recent recent);

    @Delete
    Completable deleteRecent(Recent recent);

    @Query("SELECT COUNT(*) FROM recents")
    Single<Integer> getRecentsCount();

}
