package com.secure.pro.tele_prompter.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.secure.pro.tele_prompter.data.local.entity.ScriptEntity;
import com.secure.pro.tele_prompter.data.repository.ScriptRepository;

public class EditorViewModel extends AndroidViewModel {

    private final ScriptRepository repository;

    private final MutableLiveData<ScriptEntity> currentScript = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> saveSuccessEvent = new MutableLiveData<>(false);
    private final MutableLiveData<String> validationError = new MutableLiveData<>(null);

    private long scriptId = -1;

    public EditorViewModel(@NonNull Application application) {
        super(application);
        this.repository = new ScriptRepository(application);
    }

    public void init(long id) {
        this.scriptId = id;
        if (id > 0) {
            repository.getScriptByIdAsync(id, script -> currentScript.postValue(script));
        } else {
            ScriptEntity newScript = new ScriptEntity();
            newScript.setCategory("General");
            newScript.setTitle("");
            newScript.setContent("");
            currentScript.setValue(newScript);
        }
    }

    public LiveData<ScriptEntity> getCurrentScript() {
        return currentScript;
    }

    public LiveData<Boolean> getSaveSuccessEvent() {
        return saveSuccessEvent;
    }

    public LiveData<String> getValidationError() {
        return validationError;
    }

    public void saveScript(String title, String content, String category, ScriptRepository.OnScriptInsertedListener onInserted) {
        if (title == null || title.trim().isEmpty()) {
            validationError.setValue("Please enter a title for your script.");
            return;
        }
        if (content == null || content.trim().isEmpty()) {
            validationError.setValue("Please enter some script content.");
            return;
        }

        ScriptEntity existing = currentScript.getValue();
        final ScriptEntity targetScript = existing != null ? existing : new ScriptEntity();

        targetScript.setTitle(title.trim());
        targetScript.setContent(content.trim());
        targetScript.setCategory(category != null ? category : "General");

        if (targetScript.getId() > 0) {
            repository.update(targetScript);
            saveSuccessEvent.setValue(true);
        } else {
            repository.insert(targetScript, newId -> {
                targetScript.setId(newId);
                saveSuccessEvent.postValue(true);
                if (onInserted != null) {
                    onInserted.onInserted(newId);
                }
            });
        }
    }
}
