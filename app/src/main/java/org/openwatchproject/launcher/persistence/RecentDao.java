package org.openwatchproject.launcher.persistence;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import org.openwatchproject.launcher.model.Recent;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

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
