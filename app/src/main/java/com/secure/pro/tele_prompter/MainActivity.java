package com.secure.pro.tele_prompter;

import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.secure.pro.tele_prompter.data.repository.PreferencesRepository;
import com.secure.pro.tele_prompter.databinding.ActivityMainBinding;
import com.secure.pro.tele_prompter.ui.editor.ScriptEditorActivity;
import com.secure.pro.tele_prompter.ui.home.HomeFragment;
import com.secure.pro.tele_prompter.ui.library.LibraryFragment;
import com.secure.pro.tele_prompter.ui.prompter.PrompterActivity;
import com.secure.pro.tele_prompter.ui.settings.SettingsFragment;
import com.secure.pro.tele_prompter.ui.viewmodel.MainViewModel;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private MainViewModel viewModel;

    private final HomeFragment homeFragment = new HomeFragment();
    private final LibraryFragment libraryFragment = new LibraryFragment();
    private final SettingsFragment settingsFragment = new SettingsFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply saved theme before view creation
        PreferencesRepository prefs = new PreferencesRepository(this);
        AppCompatDelegate.setDefaultNightMode(prefs.getThemeMode());

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Apply Window Insets cleanly to ensure TopBar and BottomNav are never hidden
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            // Top system bar padding for toolbar
            binding.topToolbar.setPadding(
                    binding.topToolbar.getPaddingLeft(),
                    systemBars.top,
                    binding.topToolbar.getPaddingRight(),
                    binding.topToolbar.getPaddingBottom()
            );

            // Bottom system navigation bar padding for BottomNavigationView
            binding.bottomNavigation.setPadding(
                    binding.bottomNavigation.getPaddingLeft(),
                    binding.bottomNavigation.getPaddingTop(),
                    binding.bottomNavigation.getPaddingRight(),
                    systemBars.bottom
            );

            // Anchor FAB smoothly right above the navigation bar
            ViewGroup.MarginLayoutParams fabParams = (ViewGroup.MarginLayoutParams) binding.fabNewScript.getLayoutParams();
            int baseBottomMargin = (int) (16 * getResources().getDisplayMetrics().density);
            int navBarHeight = (int) (64 * getResources().getDisplayMetrics().density);
            fabParams.bottomMargin = systemBars.bottom + navBarHeight + baseBottomMargin;
            binding.fabNewScript.setLayoutParams(fabParams);

            return insets;
        });

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        setupBottomNavigation();
        setupFab();
        setupHeaderQuickActions();

        if (savedInstanceState == null) {
            loadFragment(homeFragment);
        }
    }

    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                loadFragment(homeFragment);
                binding.fabNewScript.show();
                return true;
            } else if (id == R.id.nav_library) {
                loadFragment(libraryFragment);
                binding.fabNewScript.show();
                return true;
            } else if (id == R.id.nav_settings) {
                loadFragment(settingsFragment);
                binding.fabNewScript.hide();
                return true;
            }
            return false;
        });
    }

    public void navigateToTab(int tabIndex) {
        if (tabIndex == 0) {
            binding.bottomNavigation.setSelectedItemId(R.id.nav_home);
        } else if (tabIndex == 1) {
            binding.bottomNavigation.setSelectedItemId(R.id.nav_library);
        } else if (tabIndex == 2) {
            binding.bottomNavigation.setSelectedItemId(R.id.nav_settings);
        }
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    private void setupFab() {
        binding.fabNewScript.setOnClickListener(v -> {
            Intent intent = new Intent(this, ScriptEditorActivity.class);
            startActivity(intent);
        });
    }

    private void setupHeaderQuickActions() {
        binding.btnHeaderQuickPrompt.setOnClickListener(v -> {
            Intent intent = new Intent(this, PrompterActivity.class);
            startActivity(intent);
        });
    }
}