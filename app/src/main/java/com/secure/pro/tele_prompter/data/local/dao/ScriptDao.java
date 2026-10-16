package com.secure.pro.tele_prompter.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.secure.pro.tele_prompter.data.local.entity.ScriptEntity;

import java.util.List;

@Dao
public interface ScriptDao {

    @Query("SELECT * FROM scripts ORDER BY updated_at DESC")
    LiveData<List<ScriptEntity>> getAllScripts();

    @Query("SELECT * FROM scripts ORDER BY updated_at DESC LIMIT :limit")
    LiveData<List<ScriptEntity>> getRecentScripts(int limit);

    @Query("SELECT * FROM scripts WHERE id = :id")
    LiveData<ScriptEntity> getScriptById(long id);

    @Query("SELECT * FROM scripts WHERE id = :id")
    ScriptEntity getScriptByIdSync(long id);

    @Query("SELECT * FROM scripts WHERE category = :category ORDER BY updated_at DESC")
    LiveData<List<ScriptEntity>> getScriptsByCategory(String category);

    @Query("SELECT * FROM scripts WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY updated_at DESC")
    LiveData<List<ScriptEntity>> searchScripts(String query);

    @Query("SELECT COUNT(*) FROM scripts")
    LiveData<Integer> getScriptCount();

    @Query("SELECT SUM(word_count) FROM scripts")
    LiveData<Integer> getTotalWordCount();

    @Query("SELECT SUM(est_reading_time_seconds) FROM scripts")
    LiveData<Integer> getTotalReadingTimeSeconds();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(ScriptEntity script);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ScriptEntity> scripts);

    @Update
    void update(ScriptEntity script);

    @Delete
    void delete(ScriptEntity script);

    @Query("DELETE FROM scripts WHERE id = :id")
    void deleteById(long id);
}
