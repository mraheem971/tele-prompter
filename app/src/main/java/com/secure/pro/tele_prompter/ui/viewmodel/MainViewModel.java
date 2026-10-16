package com.secure.pro.tele_prompter.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.secure.pro.tele_prompter.data.local.entity.ScriptEntity;
import com.secure.pro.tele_prompter.data.repository.PreferencesRepository;
import com.secure.pro.tele_prompter.data.repository.ScriptRepository;

import java.util.List;

public class MainViewModel extends AndroidViewModel {

    private final ScriptRepository scriptRepository;
    private final PreferencesRepository preferencesRepository;

    private final MutableLiveData<String> selectedCategory = new MutableLiveData<>("All");
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");

    private final LiveData<List<ScriptEntity>> filteredScripts;
    private final LiveData<List<ScriptEntity>> recentScripts;
    private final LiveData<Integer> totalScriptsCount;
    private final LiveData<Integer> totalWordsCount;
    private final LiveData<Integer> totalSpeakingTimeSeconds;

    public MainViewModel(@NonNull Application application) {
        super(application);
        this.scriptRepository = new ScriptRepository(application);
        this.preferencesRepository = new PreferencesRepository(application);

        this.recentScripts = scriptRepository.getRecentScripts(5);
        this.totalScriptsCount = scriptRepository.getScriptCount();
        this.totalWordsCount = scriptRepository.getTotalWordCount();
        this.totalSpeakingTimeSeconds = scriptRepository.getTotalReadingTimeSeconds();

        // Reactive filter based on category or search
        this.filteredScripts = Transformations.switchMap(searchQuery, query -> {
            if (query != null && !query.trim().isEmpty()) {
                return scriptRepository.searchScripts(query.trim());
            } else {
                return Transformations.switchMap(selectedCategory, cat ->
                    scriptRepository.getScriptsByCategory(cat)
                );
            }
        });
    }

    public LiveData<List<ScriptEntity>> getFilteredScripts() {
        return filteredScripts;
    }

    public LiveData<List<ScriptEntity>> getRecentScripts() {
        return recentScripts;
    }

    public LiveData<Integer> getTotalScriptsCount() {
        return totalScriptsCount;
    }

    public LiveData<Integer> getTotalWordsCount() {
        return totalWordsCount;
    }

    public LiveData<Integer> getTotalSpeakingTimeSeconds() {
        return totalSpeakingTimeSeconds;
    }

    public void setCategory(String category) {
        searchQuery.setValue("");
        selectedCategory.setValue(category);
    }

    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }

    public void deleteScript(ScriptEntity script) {
        scriptRepository.delete(script);
    }

    public void duplicateScript(ScriptEntity script, ScriptRepository.OnScriptInsertedListener listener) {
        scriptRepository.duplicateScript(script, listener);
    }

    public PreferencesRepository getPreferencesRepository() {
        return preferencesRepository;
    }
}
