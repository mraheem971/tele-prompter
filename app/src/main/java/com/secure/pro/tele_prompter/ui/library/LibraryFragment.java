package com.secure.pro.tele_prompter.ui.library;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.secure.pro.tele_prompter.R;
import com.secure.pro.tele_prompter.data.local.entity.ScriptEntity;
import com.secure.pro.tele_prompter.databinding.FragmentLibraryBinding;
import com.secure.pro.tele_prompter.ui.camera.CameraRecordingActivity;
import com.secure.pro.tele_prompter.ui.editor.ScriptEditorActivity;
import com.secure.pro.tele_prompter.ui.prompter.PrompterActivity;
import com.secure.pro.tele_prompter.ui.viewmodel.MainViewModel;

public class LibraryFragment extends Fragment {

    private FragmentLibraryBinding binding;
    private MainViewModel viewModel;
    private ScriptAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLibraryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        setupRecyclerView();
        setupSearch();
        setupCategoryChips();
        setupEmptyStateActions();

        viewModel.getFilteredScripts().observe(getViewLifecycleOwner(), scripts -> {
            adapter.submitList(scripts);
            boolean isEmpty = scripts == null || scripts.isEmpty();
            binding.layoutEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            binding.rvScripts.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        });
    }

    private void setupRecyclerView() {
        adapter = new ScriptAdapter(new ScriptAdapter.OnScriptActionListener() {
            @Override
            public void onPrompt(ScriptEntity script) {
                Intent intent = new Intent(requireContext(), PrompterActivity.class);
                intent.putExtra(PrompterActivity.EXTRA_SCRIPT_ID, script.getId());
                startActivity(intent);
            }

            @Override
            public void onRecord(ScriptEntity script) {
                Intent intent = new Intent(requireContext(), CameraRecordingActivity.class);
                intent.putExtra(CameraRecordingActivity.EXTRA_SCRIPT_ID, script.getId());
                startActivity(intent);
            }

            @Override
            public void onEdit(ScriptEntity script) {
                Intent intent = new Intent(requireContext(), ScriptEditorActivity.class);
                intent.putExtra(ScriptEditorActivity.EXTRA_SCRIPT_ID, script.getId());
                startActivity(intent);
            }

            @Override
            public void onDuplicate(ScriptEntity script) {
                viewModel.duplicateScript(script, newId -> {
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Script duplicated!", Toast.LENGTH_SHORT).show()
                        );
                    }
                });
            }

            @Override
            public void onShare(ScriptEntity script) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, script.getTitle());
                shareIntent.putExtra(Intent.EXTRA_TEXT, script.getTitle() + "\n\n" + script.getContent());
                startActivity(Intent.createChooser(shareIntent, "Share Script via"));
            }

            @Override
            public void onDelete(ScriptEntity script) {
                new MaterialAlertDialogBuilder(requireContext(), R.style.Base_Theme_Teleprompter)
                        .setTitle(R.string.action_delete)
                        .setMessage(getString(R.string.delete_script_confirm, script.getTitle()))
                        .setPositiveButton(R.string.dialog_delete, (dialog, which) -> {
                            viewModel.deleteScript(script);
                            Toast.makeText(requireContext(), "Script deleted", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton(R.string.dialog_cancel, null)
                        .show();
            }
        });

        binding.rvScripts.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvScripts.setAdapter(adapter);
    }

    private void setupSearch() {
        binding.etSearchQuery.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s != null ? s.toString() : "";
                binding.btnClearSearch.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);
                viewModel.setSearchQuery(query);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.btnClearSearch.setOnClickListener(v -> {
            binding.etSearchQuery.setText("");
        });
    }

    private void setupCategoryChips() {
        binding.chipGroupCategories.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chipAll) {
                viewModel.setCategory("All");
            } else if (id == R.id.chipYouTube) {
                viewModel.setCategory("YouTube");
            } else if (id == R.id.chipKeynote) {
                viewModel.setCategory("Keynote");
            } else if (id == R.id.chipPitch) {
                viewModel.setCategory("Pitch");
            } else if (id == R.id.chipReels) {
                viewModel.setCategory("Shorts / Reels");
            } else if (id == R.id.chipInterview) {
                viewModel.setCategory("Interview");
            }
        });
    }

    private void setupEmptyStateActions() {
        binding.btnEmptyCreateScript.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ScriptEditorActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
