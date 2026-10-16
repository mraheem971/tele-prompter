package com.secure.pro.tele_prompter.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.secure.pro.tele_prompter.R;
import com.secure.pro.tele_prompter.data.repository.PreferencesRepository;
import com.secure.pro.tele_prompter.databinding.FragmentSettingsBinding;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private PreferencesRepository prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        prefs = new PreferencesRepository(requireContext());

        loadCurrentSettings();
        setupListeners();
    }

    private void loadCurrentSettings() {
        // 1. Theme
        int currentTheme = prefs.getThemeMode();
        if (currentTheme == AppCompatDelegate.MODE_NIGHT_NO) {
            binding.rbThemeLight.setChecked(true);
        } else if (currentTheme == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
            binding.rbThemeSystem.setChecked(true);
        } else {
            binding.rbThemeDark.setChecked(true);
        }

        // 2. Default Speed
        int speed = prefs.getDefaultSpeed();
        binding.sliderDefaultSpeed.setValue(speed);
        binding.tvSettingsSpeedValue.setText(speed + "x");

        // 3. Default Font Size
        int fontSize = prefs.getDefaultFontSize();
        binding.sliderDefaultFontSize.setValue(fontSize);
        binding.tvSettingsFontValue.setText(fontSize + "sp");

        // 4. Countdown Duration
        int countdown = prefs.getCountdownSeconds();
        if (countdown == 0) binding.chipCd0.setChecked(true);
        else if (countdown == 5) binding.chipCd5.setChecked(true);
        else if (countdown == 10) binding.chipCd10.setChecked(true);
        else binding.chipCd3.setChecked(true);

        // 5. Switches
        binding.switchMirrorDefault.setChecked(prefs.isMirrorModeEnabled());
        binding.switchGuideLine.setChecked(prefs.isGuideLineEnabled());
        binding.switchVolumeKeys.setChecked(prefs.isVolumeKeyControlEnabled());

        // 6. Camera Quality
        String quality = prefs.getCameraQuality();
        if ("HD".equals(quality)) {
            binding.rbQualityHD.setChecked(true);
        } else if ("SD".equals(quality)) {
            binding.rbQualitySD.setChecked(true);
        } else {
            binding.rbQualityFHD.setChecked(true);
        }
    }

    private void setupListeners() {
        // Theme RadioGroup
        binding.rgThemeMode.setOnCheckedChangeListener((group, checkedId) -> {
            int newMode = AppCompatDelegate.MODE_NIGHT_YES;
            if (checkedId == R.id.rbThemeLight) {
                newMode = AppCompatDelegate.MODE_NIGHT_NO;
            } else if (checkedId == R.id.rbThemeSystem) {
                newMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
            }
            prefs.setThemeMode(newMode);
            AppCompatDelegate.setDefaultNightMode(newMode);
        });

        // Speed Slider
        binding.sliderDefaultSpeed.addOnChangeListener((slider, value, fromUser) -> {
            int val = (int) value;
            binding.tvSettingsSpeedValue.setText(val + "x");
            prefs.setDefaultSpeed(val);
        });

        // Font Size Slider
        binding.sliderDefaultFontSize.addOnChangeListener((slider, value, fromUser) -> {
            int val = (int) value;
            binding.tvSettingsFontValue.setText(val + "sp");
            prefs.setDefaultFontSize(val);
        });

        // Countdown ChipGroup
        binding.chipGroupCountdown.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            int seconds = 3;
            if (id == R.id.chipCd0) seconds = 0;
            else if (id == R.id.chipCd5) seconds = 5;
            else if (id == R.id.chipCd10) seconds = 10;
            prefs.setCountdownSeconds(seconds);
        });

        // Switches
        binding.switchMirrorDefault.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.setMirrorModeEnabled(isChecked);
        });

        binding.switchGuideLine.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.setGuideLineEnabled(isChecked);
        });

        binding.switchVolumeKeys.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.setVolumeKeyControlEnabled(isChecked);
        });

        // Camera Quality RadioGroup
        binding.rgCameraQuality.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbQualityHD) {
                prefs.setCameraQuality("HD");
            } else if (checkedId == R.id.rbQualitySD) {
                prefs.setCameraQuality("SD");
            } else {
                prefs.setCameraQuality("FHD");
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
