package com.secure.pro.tele_prompter.ui.editor;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.secure.pro.tele_prompter.R;
import com.secure.pro.tele_prompter.data.local.entity.ScriptEntity;
import com.secure.pro.tele_prompter.data.model.ScriptTemplate;
import com.secure.pro.tele_prompter.databinding.ActivityScriptEditorBinding;
import com.secure.pro.tele_prompter.ui.camera.CameraRecordingActivity;
import com.secure.pro.tele_prompter.ui.prompter.PrompterActivity;
import com.secure.pro.tele_prompter.ui.viewmodel.EditorViewModel;
import com.secure.pro.tele_prompter.utils.TextMetricsHelper;

import java.util.List;

public class ScriptEditorActivity extends AppCompatActivity {

    public static final String EXTRA_SCRIPT_ID = "extra_script_id";
    public static final String EXTRA_TEMPLATE_TITLE = "extra_template_title";
    public static final String EXTRA_TEMPLATE_CONTENT = "extra_template_content";
    public static final String EXTRA_TEMPLATE_CATEGORY = "extra_template_category";

    private ActivityScriptEditorBinding binding;
    private EditorViewModel viewModel;
    private long scriptId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityScriptEditorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(EditorViewModel.class);

        scriptId = getIntent().getLongExtra(EXTRA_SCRIPT_ID, -1);
        viewModel.init(scriptId);

        setupToolbar();
        setupInputWatchers();
        setupCategoryChips();
        setupAssistButtons();
        setupTemplatePicker();
        setupSaveAndLaunch();

        observeViewModel();
        checkTemplateExtras();
    }

    private void setupToolbar() {
        binding.editorToolbar.setNavigationOnClickListener(v -> finish());
        if (scriptId > 0) {
            binding.editorToolbar.setTitle(R.string.editor_title_edit);
        } else {
            binding.editorToolbar.setTitle(R.string.editor_title_new);
        }
    }

    private void checkTemplateExtras() {
        if (getIntent().hasExtra(EXTRA_TEMPLATE_TITLE)) {
            String title = getIntent().getStringExtra(EXTRA_TEMPLATE_TITLE);
            String content = getIntent().getStringExtra(EXTRA_TEMPLATE_CONTENT);
            String category = getIntent().getStringExtra(EXTRA_TEMPLATE_CATEGORY);

            if (title != null) binding.etScriptTitle.setText(title);
            if (content != null) binding.etScriptContent.setText(content);
            if (category != null) selectCategoryChip(category);
        }
    }

    private void observeViewModel() {
        viewModel.getCurrentScript().observe(this, script -> {
            if (script != null && scriptId > 0) {
                binding.etScriptTitle.setText(script.getTitle());
                binding.etScriptContent.setText(script.getContent());
                selectCategoryChip(script.getCategory());
                updateMetrics(script.getContent());
            }
        });

        viewModel.getValidationError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getSaveSuccessEvent().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(this, R.string.editor_saved_success, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupInputWatchers() {
        binding.etScriptContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateMetrics(s != null ? s.toString() : "");
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void updateMetrics(String text) {
        int wordCount = TextMetricsHelper.calculateWordCount(text);
        int estSeconds = TextMetricsHelper.calculateReadingTimeSeconds(wordCount);
        binding.tvEditorMetrics.setText(String.format(
                getString(R.string.editor_word_stats),
                wordCount,
                TextMetricsHelper.formatDurationFriendly(estSeconds)
        ));
    }

    private void setupAssistButtons() {
        binding.btnInsertCue.setOnClickListener(v -> {
            int start = Math.max(binding.etScriptContent.getSelectionStart(), 0);
            int end = Math.max(binding.etScriptContent.getSelectionEnd(), 0);
            binding.etScriptContent.getText().replace(
                    Math.min(start, end),
                    Math.max(start, end),
                    "\n[PAUSE / BREATHE]\n"
            );
        });

        binding.btnInsertBullet.setOnClickListener(v -> {
            int start = Math.max(binding.etScriptContent.getSelectionStart(), 0);
            int end = Math.max(binding.etScriptContent.getSelectionEnd(), 0);
            binding.etScriptContent.getText().replace(
                    Math.min(start, end),
                    Math.max(start, end),
                    "\n• "
            );
        });

        binding.btnClearContent.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Clear Script")
                    .setMessage("Are you sure you want to clear the editor text?")
                    .setPositiveButton("Clear", (dialog, which) -> binding.etScriptContent.setText(""))
                    .setNegativeButton(R.string.dialog_cancel, null)
                    .show();
        });
    }

    private void setupCategoryChips() {
        // Selection handled via getSelectedCategory()
    }

    private String getSelectedCategory() {
        int checkedId = binding.chipGroupEditorCategory.getCheckedChipId();
        if (checkedId == R.id.chipEditorYouTube) return "YouTube";
        if (checkedId == R.id.chipEditorKeynote) return "Keynote";
        if (checkedId == R.id.chipEditorPitch) return "Pitch";
        if (checkedId == R.id.chipEditorReels) return "Shorts / Reels";
        if (checkedId == R.id.chipEditorInterview) return "Interview";
        return "General";
    }

    private void selectCategoryChip(String category) {
        if ("YouTube".equalsIgnoreCase(category)) {
            binding.chipEditorYouTube.setChecked(true);
        } else if ("Keynote".equalsIgnoreCase(category)) {
            binding.chipEditorKeynote.setChecked(true);
        } else if ("Pitch".equalsIgnoreCase(category)) {
            binding.chipEditorPitch.setChecked(true);
        } else if ("Shorts / Reels".equalsIgnoreCase(category)) {
            binding.chipEditorReels.setChecked(true);
        } else if ("Interview".equalsIgnoreCase(category)) {
            binding.chipEditorInterview.setChecked(true);
        } else {
            binding.chipEditorGeneral.setChecked(true);
        }
    }

    private void setupTemplatePicker() {
        binding.btnLoadTemplate.setOnClickListener(v -> {
            List<ScriptTemplate> templates = ScriptTemplate.getDefaultTemplates();
            String[] titles = new String[templates.size()];
            for (int i = 0; i < templates.size(); i++) {
                titles[i] = templates.get(i).getTitle() + " (" + templates.get(i).getCategory() + ")";
            }

            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.dashboard_templates)
                    .setItems(titles, (dialog, which) -> {
                        ScriptTemplate selected = templates.get(which);
                        binding.etScriptTitle.setText(selected.getTitle());
                        binding.etScriptContent.setText(selected.getContent());
                        selectCategoryChip(selected.getCategory());
                    })
                    .setNegativeButton(R.string.dialog_cancel, null)
                    .show();
        });
    }

    private void setupSaveAndLaunch() {
        binding.btnSaveScript.setOnClickListener(v -> {
            String title = binding.etScriptTitle.getText().toString();
            String content = binding.etScriptContent.getText().toString();
            String category = getSelectedCategory();
            viewModel.saveScript(title, content, category, newId -> {
                scriptId = newId;
            });
        });

        binding.btnBottomPrompt.setOnClickListener(v -> {
            String title = binding.etScriptTitle.getText().toString();
            String content = binding.etScriptContent.getText().toString();
            if (content.trim().isEmpty()) {
                Toast.makeText(this, R.string.editor_validation_content, Toast.LENGTH_SHORT).show();
                return;
            }
            if (title.trim().isEmpty()) title = "Untitled Script";

            // If existing or saved, pass id, else pass direct content
            Intent intent = new Intent(this, PrompterActivity.class);
            if (scriptId > 0) {
                intent.putExtra(PrompterActivity.EXTRA_SCRIPT_ID, scriptId);
            } else {
                intent.putExtra(PrompterActivity.EXTRA_DIRECT_TITLE, title);
                intent.putExtra(PrompterActivity.EXTRA_DIRECT_CONTENT, content);
            }
            startActivity(intent);
        });

        binding.btnBottomRecord.setOnClickListener(v -> {
            String title = binding.etScriptTitle.getText().toString();
            String content = binding.etScriptContent.getText().toString();
            if (content.trim().isEmpty()) {
                Toast.makeText(this, R.string.editor_validation_content, Toast.LENGTH_SHORT).show();
                return;
            }
            if (title.trim().isEmpty()) title = "Untitled Script";

            Intent intent = new Intent(this, CameraRecordingActivity.class);
            if (scriptId > 0) {
                intent.putExtra(CameraRecordingActivity.EXTRA_SCRIPT_ID, scriptId);
            } else {
                intent.putExtra(CameraRecordingActivity.EXTRA_DIRECT_TITLE, title);
                intent.putExtra(CameraRecordingActivity.EXTRA_DIRECT_CONTENT, content);
            }
            startActivity(intent);
        });
    }
}
