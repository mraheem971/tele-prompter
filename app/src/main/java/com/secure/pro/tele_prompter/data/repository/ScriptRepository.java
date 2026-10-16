package com.secure.pro.tele_prompter.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.secure.pro.tele_prompter.data.local.AppDatabase;
import com.secure.pro.tele_prompter.data.local.dao.ScriptDao;
import com.secure.pro.tele_prompter.data.local.entity.ScriptEntity;
import com.secure.pro.tele_prompter.utils.TextMetricsHelper;

import java.util.List;

public class ScriptRepository {

    private final ScriptDao scriptDao;
    private final LiveData<List<ScriptEntity>> allScripts;

    public interface OnScriptInsertedListener {
        void onInserted(long newId);
    }

    public interface OnScriptLoadedListener {
        void onLoaded(ScriptEntity script);
    }

    public ScriptRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        this.scriptDao = db.scriptDao();
        this.allScripts = scriptDao.getAllScripts();
    }

    public LiveData<List<ScriptEntity>> getAllScripts() {
        return allScripts;
    }

    public LiveData<List<ScriptEntity>> getRecentScripts(int limit) {
        return scriptDao.getRecentScripts(limit);
    }

    public LiveData<ScriptEntity> getScriptById(long id) {
        return scriptDao.getScriptById(id);
    }

    public void getScriptByIdAsync(long id, OnScriptLoadedListener listener) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            ScriptEntity entity = scriptDao.getScriptByIdSync(id);
            if (listener != null) {
                listener.onLoaded(entity);
            }
        });
    }

    public LiveData<List<ScriptEntity>> getScriptsByCategory(String category) {
        if ("All".equalsIgnoreCase(category)) {
            return allScripts;
        }
        return scriptDao.getScriptsByCategory(category);
    }

    public LiveData<List<ScriptEntity>> searchScripts(String query) {
        return scriptDao.searchScripts(query);
    }

    public LiveData<Integer> getScriptCount() {
        return scriptDao.getScriptCount();
    }

    public LiveData<Integer> getTotalWordCount() {
        return scriptDao.getTotalWordCount();
    }

    public LiveData<Integer> getTotalReadingTimeSeconds() {
        return scriptDao.getTotalReadingTimeSeconds();
    }

    public void insert(ScriptEntity script, OnScriptInsertedListener listener) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            int words = TextMetricsHelper.calculateWordCount(script.getContent());
            script.setWordCount(words);
            script.setEstReadingTimeSeconds(TextMetricsHelper.calculateReadingTimeSeconds(words));
            long id = scriptDao.insert(script);
            if (listener != null) {
                listener.onInserted(id);
            }
        });
    }

    public void update(ScriptEntity script) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            int words = TextMetricsHelper.calculateWordCount(script.getContent());
            script.setWordCount(words);
            script.setEstReadingTimeSeconds(TextMetricsHelper.calculateReadingTimeSeconds(words));
            script.setUpdatedAt(System.currentTimeMillis());
            scriptDao.update(script);
        });
    }

    public void delete(ScriptEntity script) {
        AppDatabase.databaseWriteExecutor.execute(() -> scriptDao.delete(script));
    }

    public void deleteById(long id) {
        AppDatabase.databaseWriteExecutor.execute(() -> scriptDao.deleteById(id));
    }

    public void duplicateScript(ScriptEntity original, OnScriptInsertedListener listener) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            ScriptEntity copy = new ScriptEntity();
            copy.setTitle(original.getTitle() + " (Copy)");
            copy.setContent(original.getContent());
            copy.setCategory(original.getCategory());
            copy.setFontSize(original.getFontSize());
            copy.setScrollSpeed(original.getScrollSpeed());
            copy.setLineSpacing(original.getLineSpacing());
            copy.setMirrored(original.isMirrored());
            copy.setWordCount(original.getWordCount());
            copy.setEstReadingTimeSeconds(original.getEstReadingTimeSeconds());
            long newId = scriptDao.insert(copy);
            if (listener != null) {
                listener.onInserted(newId);
            }
        });
    }
}
