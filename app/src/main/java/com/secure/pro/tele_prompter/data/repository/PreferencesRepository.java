package com.secure.pro.tele_prompter.data.repository;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public class PreferencesRepository {

    private static final String PREF_NAME = "teleprompter_preferences";

    private static final String KEY_THEME = "key_theme";
    private static final String KEY_DEFAULT_SPEED = "key_default_speed";
    private static final String KEY_DEFAULT_FONT_SIZE = "key_default_font_size";
    private static final String KEY_COUNTDOWN_SECONDS = "key_countdown_seconds";
    private static final String KEY_MIRROR_MODE = "key_mirror_mode";
    private static final String KEY_GUIDE_LINE = "key_guide_line";
    private static final String KEY_VOLUME_KEYS = "key_volume_keys";
    private static final String KEY_CAMERA_QUALITY = "key_camera_quality";
    private static final String KEY_PROMPTER_OPACITY = "key_prompter_opacity";
    private static final String KEY_PROMPTER_TEXT_COLOR = "key_prompter_text_color";
    private static final String KEY_PROMPTER_WIDTH_PERCENT = "key_prompter_width_percent";

    private final SharedPreferences prefs;

    public PreferencesRepository(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public int getThemeMode() {
        return prefs.getInt(KEY_THEME, AppCompatDelegate.MODE_NIGHT_YES); // Default to Midnight Dark
    }

    public void setThemeMode(int mode) {
        prefs.edit().putInt(KEY_THEME, mode).apply();
    }

    public int getDefaultSpeed() {
        return prefs.getInt(KEY_DEFAULT_SPEED, 3);
    }

    public void setDefaultSpeed(int speed) {
        prefs.edit().putInt(KEY_DEFAULT_SPEED, speed).apply();
    }

    public int getDefaultFontSize() {
        return prefs.getInt(KEY_DEFAULT_FONT_SIZE, 32);
    }

    public void setDefaultFontSize(int size) {
        prefs.edit().putInt(KEY_DEFAULT_FONT_SIZE, size).apply();
    }

    public int getCountdownSeconds() {
        return prefs.getInt(KEY_COUNTDOWN_SECONDS, 3);
    }

    public void setCountdownSeconds(int seconds) {
        prefs.edit().putInt(KEY_COUNTDOWN_SECONDS, seconds).apply();
    }

    public boolean isMirrorModeEnabled() {
        return prefs.getBoolean(KEY_MIRROR_MODE, false);
    }

    public void setMirrorModeEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_MIRROR_MODE, enabled).apply();
    }

    public boolean isGuideLineEnabled() {
        return prefs.getBoolean(KEY_GUIDE_LINE, true);
    }

    public void setGuideLineEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_GUIDE_LINE, enabled).apply();
    }

    public boolean isVolumeKeyControlEnabled() {
        return prefs.getBoolean(KEY_VOLUME_KEYS, true);
    }

    public void setVolumeKeyControlEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_VOLUME_KEYS, enabled).apply();
    }

    public String getCameraQuality() {
        return prefs.getString(KEY_CAMERA_QUALITY, "FHD");
    }

    public void setCameraQuality(String quality) {
        prefs.edit().putString(KEY_CAMERA_QUALITY, quality).apply();
    }

    public int getPrompterOpacity() {
        return prefs.getInt(KEY_PROMPTER_OPACITY, 75); // 75%
    }

    public void setPrompterOpacity(int percent) {
        prefs.edit().putInt(KEY_PROMPTER_OPACITY, percent).apply();
    }

    public int getPrompterWidthPercent() {
        return prefs.getInt(KEY_PROMPTER_WIDTH_PERCENT, 90); // 90%
    }

    public void setPrompterWidthPercent(int percent) {
        prefs.edit().putInt(KEY_PROMPTER_WIDTH_PERCENT, percent).apply();
    }
}
