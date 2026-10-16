package com.secure.pro.tele_prompter.ui.preview;

import android.content.ContentValues;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.MediaController;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.secure.pro.tele_prompter.R;
import com.secure.pro.tele_prompter.databinding.ActivityVideoPreviewBinding;
import com.secure.pro.tele_prompter.utils.TimeUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class VideoPreviewActivity extends AppCompatActivity {

    public static final String EXTRA_VIDEO_PATH = "extra_video_path";

    private ActivityVideoPreviewBinding binding;
    private File videoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVideoPreviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String path = getIntent().getStringExtra(EXTRA_VIDEO_PATH);
        if (path == null) {
            Toast.makeText(this, "Video not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        videoFile = new File(path);
        if (!videoFile.exists()) {
            Toast.makeText(this, "Video file does not exist", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupToolbar();
        setupVideoPlayer();
        loadVideoMetadata();
        setupActionButtons();
    }

    private void setupToolbar() {
        binding.previewToolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupVideoPlayer() {
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(binding.videoView);
        binding.videoView.setMediaController(mediaController);
        binding.videoView.setVideoPath(videoFile.getAbsolutePath());

        binding.btnVideoPlayOverlay.setOnClickListener(v -> {
            binding.btnVideoPlayOverlay.setVisibility(View.GONE);
            binding.videoView.start();
        });

        binding.videoView.setOnCompletionListener(mp -> {
            binding.btnVideoPlayOverlay.setVisibility(View.VISIBLE);
        });

        binding.videoView.setOnPreparedListener(mp -> {
            long durationMillis = mp.getDuration();
            binding.tvVideoDuration.setText("Duration: " + TimeUtils.formatTimer(durationMillis / 1000L));
        });
    }

    private void loadVideoMetadata() {
        binding.tvVideoFileName.setText(videoFile.getName());
        binding.tvVideoSize.setText("Size: " + TimeUtils.formatFileSize(videoFile.length()));

        try (MediaMetadataRetriever retriever = new MediaMetadataRetriever()) {
            retriever.setDataSource(videoFile.getAbsolutePath());
            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            if (time != null) {
                long durationMs = Long.parseLong(time);
                binding.tvVideoDuration.setText("Duration: " + TimeUtils.formatTimer(durationMs / 1000L));
            }
        } catch (Exception ignored) {}
    }

    private void setupActionButtons() {
        // Share via Intent & FileProvider
        binding.btnShareVideo.setOnClickListener(v -> shareVideo());

        // Save to Gallery via MediaStore
        binding.btnSaveGallery.setOnClickListener(v -> exportToGallery());
    }

    private void shareVideo() {
        try {
            Uri contentUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    videoFile
            );

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("video/mp4");
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "Share Recorded Video"));
        } catch (Exception e) {
            Toast.makeText(this, "Failed to share video: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void exportToGallery() {
        try {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Video.Media.DISPLAY_NAME, videoFile.getName());
            values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/Teleprompter");
                values.put(MediaStore.Video.Media.IS_PENDING, 1);
            }

            Uri uri = getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
            if (uri != null) {
                try (InputStream in = new FileInputStream(videoFile);
                     OutputStream out = getContentResolver().openOutputStream(uri)) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.clear();
                    values.put(MediaStore.Video.Media.IS_PENDING, 0);
                    getContentResolver().update(uri, values, null, null);
                }

                Toast.makeText(this, R.string.preview_saved_gallery, Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Could not save to gallery: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (binding.videoView.isPlaying()) {
            binding.videoView.pause();
        }
    }
}
