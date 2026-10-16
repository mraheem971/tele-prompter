package com.secure.pro.tele_prompter.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.secure.pro.tele_prompter.data.local.entity.ScriptEntity;
import com.secure.pro.tele_prompter.data.repository.PreferencesRepository;
import com.secure.pro.tele_prompter.data.repository.ScriptRepository;

public class PrompterViewModel extends AndroidViewModel {

    private final ScriptRepository scriptRepository;
    private final PreferencesRepository preferencesRepository;

    private final MutableLiveData<ScriptEntity> script = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> isPlaying = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> scrollSpeed = new MutableLiveData<>(3);
    private final MutableLiveData<Integer> fontSize = new MutableLiveData<>(34);
    private final MutableLiveData<Float> lineSpacing = new MutableLiveData<>(1.4f);
    private final MutableLiveData<Boolean> isMirroredH = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isMirroredV = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isGuideLineVisible = new MutableLiveData<>(true);
    private final MutableLiveData<Integer> countdownSeconds = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> prompterWidthPercent = new MutableLiveData<>(90);
    private final MutableLiveData<Integer> prompterOpacity = new MutableLiveData<>(80);

    public PrompterViewModel(@NonNull Application application) {
        super(application);
        this.scriptRepository = new ScriptRepository(application);
        this.preferencesRepository = new PreferencesRepository(application);

        // Load user defaults
        this.scrollSpeed.setValue(preferencesRepository.getDefaultSpeed());
        this.fontSize.setValue(preferencesRepository.getDefaultFontSize());
        this.isMirroredH.setValue(preferencesRepository.isMirrorModeEnabled());
        this.isGuideLineVisible.setValue(preferencesRepository.isGuideLineEnabled());
        this.prompterWidthPercent.setValue(preferencesRepository.getPrompterWidthPercent());
        this.prompterOpacity.setValue(preferencesRepository.getPrompterOpacity());
    }

    public void loadScript(long id) {
        if (id > 0) {
            scriptRepository.getScriptByIdAsync(id, entity -> {
                if (entity != null) {
                    script.postValue(entity);
                    if (entity.getScrollSpeed() > 0) {
                        scrollSpeed.postValue(entity.getScrollSpeed());
                    }
                    if (entity.getFontSize() > 0) {
                        fontSize.postValue(entity.getFontSize());
                    }
                    if (entity.getLineSpacing() > 0) {
                        lineSpacing.postValue(entity.getLineSpacing());
                    }
                    isMirroredH.postValue(entity.isMirrored());
                }
            });
        }
    }

    public void setScriptDirect(String title, String content) {
        ScriptEntity entity = new ScriptEntity(title, content, "Quick Prompt");
        script.setValue(entity);
    }

    public LiveData<ScriptEntity> getScript() { return script; }
    public LiveData<Boolean> getIsPlaying() { return isPlaying; }
    public LiveData<Integer> getScrollSpeed() { return scrollSpeed; }
    public LiveData<Integer> getFontSize() { return fontSize; }
    public LiveData<Float> getLineSpacing() { return lineSpacing; }
    public LiveData<Boolean> getIsMirroredH() { return isMirroredH; }
    public LiveData<Boolean> getIsMirroredV() { return isMirroredV; }
    public LiveData<Boolean> getIsGuideLineVisible() { return isGuideLineVisible; }
    public LiveData<Integer> getCountdownSeconds() { return countdownSeconds; }
    public LiveData<Integer> getPrompterWidthPercent() { return prompterWidthPercent; }
    public LiveData<Integer> getPrompterOpacity() { return prompterOpacity; }

    public void togglePlayPause() {
        Boolean current = isPlaying.getValue();
        isPlaying.setValue(current == null || !current);
    }

    public void setPlaying(boolean playing) {
        isPlaying.setValue(playing);
    }

    public void increaseSpeed() {
        int speed = scrollSpeed.getValue() != null ? scrollSpeed.getValue() : 3;
        if (speed < 10) {
            scrollSpeed.setValue(speed + 1);
        }
    }

    public void decreaseSpeed() {
        int speed = scrollSpeed.getValue() != null ? scrollSpeed.getValue() : 3;
        if (speed > 1) {
            scrollSpeed.setValue(speed - 1);
        }
    }

    public void setScrollSpeed(int speed) {
        scrollSpeed.setValue(Math.max(1, Math.min(10, speed)));
    }

    public void setFontSize(int size) {
        fontSize.setValue(Math.max(18, Math.min(72, size)));
    }

    public void setLineSpacing(float spacing) {
        lineSpacing.setValue(spacing);
    }

    public void toggleMirrorHorizontal() {
        Boolean current = isMirroredH.getValue();
        isMirroredH.setValue(current == null || !current);
    }

    public void toggleMirrorVertical() {
        Boolean current = isMirroredV.getValue();
        isMirroredV.setValue(current == null || !current);
    }

    public void toggleGuideLine() {
        Boolean current = isGuideLineVisible.getValue();
        isGuideLineVisible.setValue(current == null || !current);
    }

    public void setCountdownSeconds(int seconds) {
        countdownSeconds.setValue(seconds);
    }

    public void setPrompterWidthPercent(int percent) {
        prompterWidthPercent.setValue(Math.max(40, Math.min(100, percent)));
    }

    public void setPrompterOpacity(int opacity) {
        prompterOpacity.setValue(Math.max(10, Math.min(100, opacity)));
    }

    public PreferencesRepository getPreferencesRepository() {
        return preferencesRepository;
    }
}
