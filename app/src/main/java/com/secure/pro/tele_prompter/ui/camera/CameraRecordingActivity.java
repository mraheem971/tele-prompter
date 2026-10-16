package com.secure.pro.tele_prompter.ui.camera;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.FileOutputOptions;
import androidx.camera.video.Quality;
import androidx.camera.video.QualitySelector;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.VideoCapture;
import androidx.camera.video.VideoRecordEvent;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.common.util.concurrent.ListenableFuture;
import com.secure.pro.tele_prompter.R;
import com.secure.pro.tele_prompter.data.repository.PreferencesRepository;
import com.secure.pro.tele_prompter.databinding.ActivityCameraRecordingBinding;
import com.secure.pro.tele_prompter.ui.preview.VideoPreviewActivity;
import com.secure.pro.tele_prompter.ui.viewmodel.PrompterViewModel;
import com.secure.pro.tele_prompter.utils.TimeUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class CameraRecordingActivity extends AppCompatActivity {

    public static final String EXTRA_SCRIPT_ID = "extra_script_id";
    public static final String EXTRA_DIRECT_TITLE = "extra_direct_title";
    public static final String EXTRA_DIRECT_CONTENT = "extra_direct_content";

    private ActivityCameraRecordingBinding binding;
    private PrompterViewModel viewModel;
    private PreferencesRepository prefs;

    // CameraX variables
    private ProcessCameraProvider cameraProvider;
    private Camera camera;
    private VideoCapture<Recorder> videoCapture;
    private Recording activeRecording;
    private boolean isRecording = false;
    private boolean isFrontCamera = true;
    private boolean isTorchOn = false;

    // Timer & Smooth scroll
    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private long recordingStartTime = 0;
    private CountDownTimer countdownTimer;

    // Opacity levels
    private final int[] opacityValues = new int[]{40, 75, 90, 100};
    private int opacityIndex = 1; // default 75%

    // Prompter Smooth Scroll
    private final Runnable scrollRunnable = new Runnable() {
        @Override
        public void run() {
            if (Boolean.TRUE.equals(viewModel.getIsPlaying().getValue())) {
                int speed = viewModel.getScrollSpeed().getValue() != null ? viewModel.getScrollSpeed().getValue() : 3;
                int scrollBy = Math.max(1, (int) Math.round(speed * 0.8));

                int maxScroll = binding.overlayScrollView.getChildAt(0).getHeight() - binding.overlayScrollView.getHeight();
                int currentY = binding.overlayScrollView.getScrollY();

                if (currentY >= maxScroll && maxScroll > 0) {
                    viewModel.setPlaying(false);
                } else {
                    binding.overlayScrollView.scrollBy(0, scrollBy);
                    binding.overlayScrollView.postDelayed(this, 16);
                }
            }
        }
    };

    // Recording duration ticker
    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRecording) {
                long elapsedSeconds = (System.currentTimeMillis() - recordingStartTime) / 1000L;
                binding.tvRecordingTimer.setText(TimeUtils.formatTimer(elapsedSeconds));
                timerHandler.postDelayed(this, 1000L);
            }
        }
    };

    // Permission launcher
    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean cameraGranted = Boolean.TRUE.equals(result.get(Manifest.permission.CAMERA));
                boolean audioGranted = Boolean.TRUE.equals(result.get(Manifest.permission.RECORD_AUDIO));

                if (cameraGranted && audioGranted) {
                    startCamera();
                } else {
                    showPermissionDeniedDialog();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCameraRecordingBinding.inflate(getLayoutInflater());
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
            viewModel.setScriptDirect("Studio Prompt",
                    "Welcome to Studio Camera Recorder!\n\n" +
                    "Your prompt appears directly below the lens so your eye gaze looks directly into the camera.\n\n" +
                    "Press the RED record button below to begin recording with audio and auto-scroll.\n\n" +
                    "You can switch front/back cameras, flip mirror mode, or change opacity at any time!");
        }

        setupObservers();
        setupControls();
        checkPermissionsAndStartCamera();
    }

    private void setupObservers() {
        viewModel.getScript().observe(this, script -> {
            if (script != null) {
                binding.tvOverlayContent.setText(script.getContent());
            }
        });

        viewModel.getIsPlaying().observe(this, isPlaying -> {
            if (Boolean.TRUE.equals(isPlaying)) {
                binding.fabCameraPlayPauseText.setImageResource(R.drawable.ic_pause);
                startSmoothScrolling();
            } else {
                binding.fabCameraPlayPauseText.setImageResource(R.drawable.ic_play);
                stopSmoothScrolling();
            }
        });

        viewModel.getScrollSpeed().observe(this, speed -> {
            binding.tvCameraSpeedLabel.setText(speed + "x");
        });

        viewModel.getIsMirroredH().observe(this, mirrored -> {
            binding.overlayMirrorContainer.setScaleX(Boolean.TRUE.equals(mirrored) ? -1f : 1f);
        });
    }

    private void setupControls() {
        binding.btnCameraClose.setOnClickListener(v -> finish());

        // Front / Back camera flip
        binding.btnCameraFlip.setOnClickListener(v -> {
            if (isRecording) {
                Toast.makeText(this, "Cannot flip camera while recording", Toast.LENGTH_SHORT).show();
                return;
            }
            isFrontCamera = !isFrontCamera;
            startCamera();
        });

        // Torch toggle
        binding.btnCameraTorch.setOnClickListener(v -> {
            if (camera != null && camera.getCameraInfo().hasFlashUnit()) {
                isTorchOn = !isTorchOn;
                camera.getCameraControl().enableTorch(isTorchOn);
                binding.btnCameraTorch.setColorFilter(isTorchOn ? ContextCompat.getColor(this, R.color.electric_blue) : Color.WHITE);
            } else {
                Toast.makeText(this, "Torch not available on front camera", Toast.LENGTH_SHORT).show();
            }
        });

        // Speed Controls
        binding.btnCameraSpeedUp.setOnClickListener(v -> viewModel.increaseSpeed());
        binding.btnCameraSpeedDown.setOnClickListener(v -> viewModel.decreaseSpeed());

        // Mirror Toggle
        binding.btnCameraMirrorToggle.setOnClickListener(v -> viewModel.toggleMirrorHorizontal());

        // Opacity Toggle
        binding.btnCameraOpacity.setOnClickListener(v -> cycleOpacity());

        // Prompter Scroll Play/Pause
        binding.fabCameraPlayPauseText.setOnClickListener(v -> viewModel.togglePlayPause());

        // Reset Scroll to Top
        binding.btnCameraResetScroll.setOnClickListener(v -> {
            viewModel.setPlaying(false);
            binding.overlayScrollView.smoothScrollTo(0, 0);
        });

        // Record Button Click
        binding.fabRecordVideo.setOnClickListener(v -> {
            if (isRecording) {
                stopRecording();
            } else {
                int cdSeconds = prefs.getCountdownSeconds();
                if (cdSeconds > 0) {
                    startCountdown(cdSeconds);
                } else {
                    startRecording();
                }
            }
        });
    }

    private void cycleOpacity() {
        opacityIndex = (opacityIndex + 1) % opacityValues.length;
        int opacity = opacityValues[opacityIndex];
        binding.btnCameraOpacity.setText(opacity + "%");

        int alpha = (int) (255 * (opacity / 100.0f));
        int color = Color.argb(alpha, 9, 13, 22);
        binding.cardPrompterOverlay.setCardBackgroundColor(color);
    }

    private void startCountdown(int seconds) {
        binding.cameraCountdownOverlay.setVisibility(View.VISIBLE);
        Animation pulse = AnimationUtils.loadAnimation(this, R.anim.anim_pulse);

        countdownTimer = new CountDownTimer(seconds * 1000L, 1000L) {
            @Override
            public void onTick(long millisUntilFinished) {
                long remaining = (millisUntilFinished / 1000L) + 1;
                binding.tvCameraCountdownNumber.setText(String.valueOf(remaining));
                binding.tvCameraCountdownNumber.startAnimation(pulse);
            }

            @Override
            public void onFinish() {
                binding.tvCameraCountdownNumber.setText("REC!");
                binding.tvCameraCountdownNumber.postDelayed(() -> {
                    binding.cameraCountdownOverlay.setVisibility(View.GONE);
                    startRecording();
                }, 400);
            }
        }.start();
    }

    private void checkPermissionsAndStartCamera() {
        boolean hasCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean hasAudio = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;

        if (hasCamera && hasAudio) {
            startCamera();
        } else {
            permissionLauncher.launch(new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO});
        }
    }

    private void showPermissionDeniedDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Permissions Required")
                .setMessage(R.string.camera_permission_required)
                .setPositiveButton(R.string.camera_permission_grant, (dialog, which) -> {
                    permissionLauncher.launch(new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO});
                })
                .setNegativeButton(R.string.dialog_cancel, (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();

                // 1. Preview
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(binding.cameraPreviewView.getSurfaceProvider());

                // 2. Video Quality based on preferences
                Quality quality = Quality.FHD;
                String prefQuality = prefs.getCameraQuality();
                if ("HD".equals(prefQuality)) {
                    quality = Quality.HD;
                } else if ("SD".equals(prefQuality)) {
                    quality = Quality.SD;
                }

                QualitySelector qualitySelector = QualitySelector.from(quality);
                Recorder recorder = new Recorder.Builder()
                        .setQualitySelector(qualitySelector)
                        .build();
                videoCapture = VideoCapture.withOutput(recorder);

                // 3. Camera Selector
                CameraSelector cameraSelector = isFrontCamera ?
                        CameraSelector.DEFAULT_FRONT_CAMERA :
                        CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                camera = cameraProvider.bindToLifecycle(
                        this,
                        cameraSelector,
                        preview,
                        videoCapture
                );

            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(this, "Failed to start camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void startRecording() {
        if (videoCapture == null) return;

        // Prepare video output file in Movies directory
        File moviesDir = getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        if (moviesDir == null) moviesDir = getFilesDir();
        if (!moviesDir.exists()) moviesDir.mkdirs();

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File videoFile = new File(moviesDir, "TelePrompt_" + timeStamp + ".mp4");

        FileOutputOptions outputOptions = new FileOutputOptions.Builder(videoFile).build();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            activeRecording = videoCapture.getOutput()
                    .prepareRecording(this, outputOptions)
                    .withAudioEnabled()
                    .start(ContextCompat.getMainExecutor(this), recordEvent -> {
                        if (recordEvent instanceof VideoRecordEvent.Start) {
                            onRecordingStarted();
                        } else if (recordEvent instanceof VideoRecordEvent.Finalize) {
                            VideoRecordEvent.Finalize finalizeEvent = (VideoRecordEvent.Finalize) recordEvent;
                            onRecordingFinalized(finalizeEvent, videoFile);
                        }
                    });
        }
    }

    private void stopRecording() {
        if (activeRecording != null) {
            activeRecording.stop();
            activeRecording = null;
        }
    }

    private void onRecordingStarted() {
        isRecording = true;
        recordingStartTime = System.currentTimeMillis();

        binding.layoutRecordingIndicator.setVisibility(View.VISIBLE);
        Animation pulse = AnimationUtils.loadAnimation(this, R.anim.anim_pulse);
        binding.ivRecordingPulseDot.startAnimation(pulse);

        // Update Big Record Button to Stop square
        binding.fabRecordVideo.setImageResource(R.drawable.ic_stop);

        // Start duration timer and prompter auto-scrolling!
        timerHandler.post(timerRunnable);
        viewModel.setPlaying(true);
    }

    private void onRecordingFinalized(VideoRecordEvent.Finalize finalizeEvent, File videoFile) {
        isRecording = false;
        binding.layoutRecordingIndicator.setVisibility(View.GONE);
        binding.ivRecordingPulseDot.clearAnimation();
        timerHandler.removeCallbacks(timerRunnable);
        viewModel.setPlaying(false);

        binding.fabRecordVideo.setImageResource(R.drawable.ic_videocam);

        if (!finalizeEvent.hasError()) {
            Toast.makeText(this, R.string.camera_recording_saved, Toast.LENGTH_SHORT).show();

            // Open Video Preview Screen!
            Intent intent = new Intent(this, VideoPreviewActivity.class);
            intent.putExtra(VideoPreviewActivity.EXTRA_VIDEO_PATH, videoFile.getAbsolutePath());
            startActivity(intent);
        } else {
            Toast.makeText(this, getString(R.string.camera_recording_failed, finalizeEvent.getCause() != null ? finalizeEvent.getCause().getMessage() : "Unknown"), Toast.LENGTH_LONG).show();
        }
    }

    private void startSmoothScrolling() {
        binding.overlayScrollView.removeCallbacks(scrollRunnable);
        binding.overlayScrollView.post(scrollRunnable);
    }

    private void stopSmoothScrolling() {
        binding.overlayScrollView.removeCallbacks(scrollRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isRecording) {
            stopRecording();
        }
        viewModel.setPlaying(false);
        if (countdownTimer != null) {
            countdownTimer.cancel();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopSmoothScrolling();
        timerHandler.removeCallbacks(timerRunnable);
        if (activeRecording != null) {
            activeRecording.stop();
            activeRecording = null;
        }
        if (countdownTimer != null) {
            countdownTimer.cancel();
        }
    }
}
