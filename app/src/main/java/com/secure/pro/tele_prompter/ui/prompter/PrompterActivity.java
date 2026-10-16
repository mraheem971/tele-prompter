package com.secure.pro.tele_prompter.ui.prompter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.secure.pro.tele_prompter.R;
import com.secure.pro.tele_prompter.data.repository.PreferencesRepository;
import com.secure.pro.tele_prompter.databinding.ActivityPrompterBinding;
import com.secure.pro.tele_prompter.databinding.DialogPrompterSettingsBinding;
import com.secure.pro.tele_prompter.ui.viewmodel.PrompterViewModel;

public class PrompterActivity extends AppCompatActivity {

    public static final String EXTRA_SCRIPT_ID = "extra_script_id";
    public static final String EXTRA_DIRECT_TITLE = "extra_direct_title";
    public static final String EXTRA_DIRECT_CONTENT = "extra_direct_content";

    private ActivityPrompterBinding binding;
    private PrompterViewModel viewModel;
    private PreferencesRepository prefs;

    private boolean isHudVisible = true;
    private CountDownTimer countdownTimer;

    // Smooth Scrolling Engine
    private final Runnable scrollRunnable = new Runnable() {
        @Override
        public void run() {
            if (Boolean.TRUE.equals(viewModel.getIsPlaying().getValue())) {
                int speed = viewModel.getScrollSpeed().getValue() != null ? viewModel.getScrollSpeed().getValue() : 3;
                // Pixels per step (smooth fractional accumulation)
                int scrollBy = Math.max(1, (int) Math.round(speed * 0.9));

                int maxScroll = binding.prompterScrollView.getChildAt(0).getHeight() - binding.prompterScrollView.getHeight();
                int currentY = binding.prompterScrollView.getScrollY();

                if (currentY >= maxScroll && maxScroll > 0) {
                    viewModel.setPlaying(false);
                } else {
                    binding.prompterScrollView.scrollBy(0, scrollBy);
                    binding.prompterScrollView.postDelayed(this, 16); // ~60fps smooth ticker
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPrompterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(PrompterViewModel.class);
        prefs = viewModel.getPreferencesRepository();

        long scriptId = getIntent().getLongExtra(EXTRA_SCRIPT_ID, -1);
        if (scriptId > 0) {
            viewModel.loadScript(scriptId);
        } else if (getIntent().hasExtra(EXTRA_DIRECT_CONTENT)) {
            String title = getIntent().getStringExtra(EXTRA_DIRECT_TITLE);
            String content = getIntent().getStringExtra(EXTRA_DIRECT_CONTENT);
            viewModel.setScriptDirect(title != null ? title : "Quick Prompt", content != null ? content : "");
        } else {
            // Default sample prompter text if opened empty
            viewModel.setScriptDirect("Sample Prompt",
                    "Welcome to TelePrompt Pro.\n\n" +
                    "This distraction-free reading mode is engineered for flawless keynote speeches, YouTube videos, and boardroom presentations.\n\n" +
                    "Use the controls below to adjust scroll velocity, increase font size, or activate mirror mode for glass beam-splitters.\n\n" +
                    "You can tap the screen at any time to hide or reveal the playback dock.\n\n" +
                    "Keep your gaze locked on the electric-blue guide line to maintain perfect eye contact with your audience!");
        }

        setupObservers();
        setupControls();
        setupScreenTapToggle();
    }

    private void setupObservers() {
        viewModel.getScript().observe(this, script -> {
            if (script != null) {
                binding.tvPrompterTitle.setText(script.getTitle());
                binding.tvPrompterContent.setText(script.getContent());
            }
        });

        viewModel.getIsPlaying().observe(this, isPlaying -> {
            if (Boolean.TRUE.equals(isPlaying)) {
                binding.fabPrompterPlayPause.setImageResource(R.drawable.ic_pause);
                binding.fabPrompterPlayPause.setContentDescription(getString(R.string.prompter_pause));
                startSmoothScrolling();
            } else {
                binding.fabPrompterPlayPause.setImageResource(R.drawable.ic_play);
                binding.fabPrompterPlayPause.setContentDescription(getString(R.string.prompter_play));
                stopSmoothScrolling();
            }
        });

        viewModel.getScrollSpeed().observe(this, speed -> {
            binding.tvPrompterSpeedLabel.setText(speed + "x");
        });

        viewModel.getFontSize().observe(this, size -> {
            binding.tvPrompterContent.setTextSize(size);
        });

        viewModel.getLineSpacing().observe(this, spacing -> {
            binding.tvPrompterContent.setLineSpacing(0, spacing);
        });

        viewModel.getIsMirroredH().observe(this, mirrored -> {
            binding.prompterMirrorContainer.setScaleX(Boolean.TRUE.equals(mirrored) ? -1f : 1f);
        });

        viewModel.getIsMirroredV().observe(this, mirrored -> {
            binding.prompterMirrorContainer.setScaleY(Boolean.TRUE.equals(mirrored) ? -1f : 1f);
        });

        viewModel.getIsGuideLineVisible().observe(this, visible -> {
            binding.layoutGuideLine.setVisibility(Boolean.TRUE.equals(visible) ? View.VISIBLE : View.GONE);
        });

        viewModel.getPrompterWidthPercent().observe(this, percent -> {
            updatePrompterWidth(percent);
        });
    }

    private void updatePrompterWidth(int percent) {
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int targetWidth = (int) (screenWidth * (percent / 100.0f));
        int sideMargin = Math.max(16, (screenWidth - targetWidth) / 2);
        binding.prompterMarginContainer.setPadding(sideMargin, binding.prompterMarginContainer.getPaddingTop(), sideMargin, binding.prompterMarginContainer.getPaddingBottom());
    }

    private void setupControls() {
        binding.btnPrompterClose.setOnClickListener(v -> finish());

        // Play / Pause with Countdown check
        binding.fabPrompterPlayPause.setOnClickListener(v -> {
            Boolean isPlaying = viewModel.getIsPlaying().getValue();
            if (Boolean.TRUE.equals(isPlaying)) {
                viewModel.setPlaying(false);
            } else {
                int cdSeconds = prefs.getCountdownSeconds();
                if (cdSeconds > 0 && binding.prompterScrollView.getScrollY() == 0) {
                    startCountdown(cdSeconds);
                } else {
                    viewModel.setPlaying(true);
                }
            }
        });

        // Speed Stepper
        binding.btnPrompterSpeedUp.setOnClickListener(v -> viewModel.increaseSpeed());
        binding.btnPrompterSpeedDown.setOnClickListener(v -> viewModel.decreaseSpeed());

        // Rewind 5s
        binding.btnPrompterRewind.setOnClickListener(v -> {
            int currentY = binding.prompterScrollView.getScrollY();
            int step = 200;
            binding.prompterScrollView.smoothScrollTo(0, Math.max(0, currentY - step));
        });

        // Jump to Top
        binding.btnPrompterRestart.setOnClickListener(v -> {
            viewModel.setPlaying(false);
            binding.prompterScrollView.smoothScrollTo(0, 0);
        });

        // Toggles
        binding.btnToggleMirrorH.setOnClickListener(v -> viewModel.toggleMirrorHorizontal());
        binding.btnToggleGuideLine.setOnClickListener(v -> viewModel.toggleGuideLine());

        // Customization BottomSheet
        binding.btnPrompterSettings.setOnClickListener(v -> openCustomizationSheet());
    }

    private void startCountdown(int seconds) {
        binding.layoutCountdownOverlay.setVisibility(View.VISIBLE);
        Animation pulse = AnimationUtils.loadAnimation(this, R.anim.anim_pulse);

        countdownTimer = new CountDownTimer(seconds * 1000L, 1000L) {
            @Override
            public void onTick(long millisUntilFinished) {
                long remaining = (millisUntilFinished / 1000L) + 1;
                binding.tvCountdownNumber.setText(String.valueOf(remaining));
                binding.tvCountdownNumber.startAnimation(pulse);
            }

            @Override
            public void onFinish() {
                binding.tvCountdownNumber.setText("GO!");
                binding.tvCountdownNumber.postDelayed(() -> {
                    binding.layoutCountdownOverlay.setVisibility(View.GONE);
                    viewModel.setPlaying(true);
                }, 400);
            }
        }.start();
    }

    private void setupScreenTapToggle() {
        binding.prompterRoot.setOnClickListener(v -> toggleHud());
        binding.prompterScrollView.setOnClickListener(v -> toggleHud());
        binding.tvPrompterContent.setOnClickListener(v -> toggleHud());
    }

    private void toggleHud() {
        if (isHudVisible) {
            hideHud();
        } else {
            showHud();
        }
    }

    private void hideHud() {
        isHudVisible = false;
        binding.prompterHudCard.animate()
                .alpha(0f)
                .translationY(binding.prompterHudCard.getHeight())
                .setDuration(250)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        binding.prompterHudCard.setVisibility(View.GONE);
                    }
                });

        binding.prompterTopBar.animate()
                .alpha(0f)
                .translationY(-binding.prompterTopBar.getHeight())
                .setDuration(250)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        binding.prompterTopBar.setVisibility(View.GONE);
                    }
                });
    }

    private void showHud() {
        isHudVisible = true;
        binding.prompterHudCard.setVisibility(View.VISIBLE);
        binding.prompterHudCard.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(250)
                .setListener(null);

        binding.prompterTopBar.setVisibility(View.VISIBLE);
        binding.prompterTopBar.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(250)
                .setListener(null);
    }

    private void startSmoothScrolling() {
        binding.prompterScrollView.removeCallbacks(scrollRunnable);
        binding.prompterScrollView.post(scrollRunnable);
    }

    private void stopSmoothScrolling() {
        binding.prompterScrollView.removeCallbacks(scrollRunnable);
    }

    private void openCustomizationSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        DialogPrompterSettingsBinding sheetBinding = DialogPrompterSettingsBinding.inflate(getLayoutInflater());
        dialog.setContentView(sheetBinding.getRoot());

        // Font Size
        int currentSize = viewModel.getFontSize().getValue() != null ? viewModel.getFontSize().getValue() : 34;
        sheetBinding.sliderSheetFontSize.setValue(currentSize);
        sheetBinding.tvSheetFontSizeVal.setText(currentSize + "sp");
        sheetBinding.sliderSheetFontSize.addOnChangeListener((slider, value, fromUser) -> {
            int val = (int) value;
            sheetBinding.tvSheetFontSizeVal.setText(val + "sp");
            viewModel.setFontSize(val);
        });

        // Line Spacing
        float currentSpacing = viewModel.getLineSpacing().getValue() != null ? viewModel.getLineSpacing().getValue() : 1.4f;
        sheetBinding.sliderSheetLineSpacing.setValue(currentSpacing);
        sheetBinding.tvSheetLineSpacingVal.setText(String.format("%.1fx", currentSpacing));
        sheetBinding.sliderSheetLineSpacing.addOnChangeListener((slider, value, fromUser) -> {
            sheetBinding.tvSheetLineSpacingVal.setText(String.format("%.1fx", value));
            viewModel.setLineSpacing(value);
        });

        // Reading Width
        int currentWidth = viewModel.getPrompterWidthPercent().getValue() != null ? viewModel.getPrompterWidthPercent().getValue() : 90;
        sheetBinding.sliderSheetWidth.setValue(currentWidth);
        sheetBinding.tvSheetWidthVal.setText(currentWidth + "%");
        sheetBinding.sliderSheetWidth.addOnChangeListener((slider, value, fromUser) -> {
            int val = (int) value;
            sheetBinding.tvSheetWidthVal.setText(val + "%");
            viewModel.setPrompterWidthPercent(val);
        });

        // Colors
        sheetBinding.btnColorWhite.setOnClickListener(v -> binding.tvPrompterContent.setTextColor(Color.WHITE));
        sheetBinding.btnColorCyan.setOnClickListener(v -> binding.tvPrompterContent.setTextColor(ContextCompat.getColor(this, R.color.prompter_text_cyan)));
        sheetBinding.btnColorYellow.setOnClickListener(v -> binding.tvPrompterContent.setTextColor(ContextCompat.getColor(this, R.color.prompter_text_yellow)));
        sheetBinding.btnColorGreen.setOnClickListener(v -> binding.tvPrompterContent.setTextColor(ContextCompat.getColor(this, R.color.prompter_text_green)));

        // Switches
        sheetBinding.switchSheetMirrorH.setChecked(Boolean.TRUE.equals(viewModel.getIsMirroredH().getValue()));
        sheetBinding.switchSheetMirrorH.setOnCheckedChangeListener((btn, isChecked) -> viewModel.toggleMirrorHorizontal());

        sheetBinding.switchSheetMirrorV.setChecked(Boolean.TRUE.equals(viewModel.getIsMirroredV().getValue()));
        sheetBinding.switchSheetMirrorV.setOnCheckedChangeListener((btn, isChecked) -> viewModel.toggleMirrorVertical());

        sheetBinding.btnSheetDismiss.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    // Hardware volume key control support
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (prefs.isVolumeKeyControlEnabled()) {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                viewModel.increaseSpeed();
                Toast.makeText(this, "Speed: " + viewModel.getScrollSpeed().getValue() + "x", Toast.LENGTH_SHORT).show();
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                viewModel.decreaseSpeed();
                Toast.makeText(this, "Speed: " + viewModel.getScrollSpeed().getValue() + "x", Toast.LENGTH_SHORT).show();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        super.onPause();
        viewModel.setPlaying(false);
        if (countdownTimer != null) {
            countdownTimer.cancel();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopSmoothScrolling();
        if (countdownTimer != null) {
            countdownTimer.cancel();
        }
    }
}
