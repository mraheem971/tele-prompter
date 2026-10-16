package com.secure.pro.tele_prompter.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.secure.pro.tele_prompter.MainActivity;
import com.secure.pro.tele_prompter.data.local.entity.ScriptEntity;
import com.secure.pro.tele_prompter.data.model.ScriptTemplate;
import com.secure.pro.tele_prompter.databinding.FragmentHomeBinding;
import com.secure.pro.tele_prompter.ui.camera.CameraRecordingActivity;
import com.secure.pro.tele_prompter.ui.editor.ScriptEditorActivity;
import com.secure.pro.tele_prompter.ui.prompter.PrompterActivity;
import com.secure.pro.tele_prompter.ui.viewmodel.MainViewModel;
import com.secure.pro.tele_prompter.utils.TextMetricsHelper;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private MainViewModel viewModel;
    private RecentScriptAdapter recentAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        setupRecentRecyclerView();
        setupTemplatesRecyclerView();
        setupMetricsObservers();
        setupQuickActionCards();
    }

    private void setupRecentRecyclerView() {
        recentAdapter = new RecentScriptAdapter(new RecentScriptAdapter.OnRecentActionListener() {
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
            public void onClick(ScriptEntity script) {
                Intent intent = new Intent(requireContext(), ScriptEditorActivity.class);
                intent.putExtra(ScriptEditorActivity.EXTRA_SCRIPT_ID, script.getId());
                startActivity(intent);
            }
        });

        binding.rvRecentScripts.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvRecentScripts.setAdapter(recentAdapter);

        viewModel.getRecentScripts().observe(getViewLifecycleOwner(), list -> {
            recentAdapter.submitList(list);
        });

        binding.tvViewAllScripts.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToTab(1); // Navigate to Library
            }
        });
    }

    private void setupTemplatesRecyclerView() {
        TemplateAdapter templateAdapter = new TemplateAdapter(ScriptTemplate.getDefaultTemplates(), template -> {
            Intent intent = new Intent(requireContext(), ScriptEditorActivity.class);
            intent.putExtra(ScriptEditorActivity.EXTRA_TEMPLATE_TITLE, template.getTitle());
            intent.putExtra(ScriptEditorActivity.EXTRA_TEMPLATE_CONTENT, template.getContent());
            intent.putExtra(ScriptEditorActivity.EXTRA_TEMPLATE_CATEGORY, template.getCategory());
            startActivity(intent);
        });

        binding.rvTemplates.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvTemplates.setAdapter(templateAdapter);
    }

    private void setupMetricsObservers() {
        viewModel.getTotalScriptsCount().observe(getViewLifecycleOwner(), count -> {
            binding.tvStatScriptsCount.setText(String.valueOf(count != null ? count : 0));
        });

        viewModel.getTotalWordsCount().observe(getViewLifecycleOwner(), words -> {
            binding.tvStatWordsCount.setText(String.valueOf(words != null ? words : 0));
        });

        viewModel.getTotalSpeakingTimeSeconds().observe(getViewLifecycleOwner(), seconds -> {
            binding.tvStatEstDuration.setText(TextMetricsHelper.formatDurationFriendly(seconds != null ? seconds : 0));
        });
    }

    private void setupQuickActionCards() {
        binding.cardQuickPrompt.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), PrompterActivity.class);
            startActivity(intent);
        });

        binding.cardQuickRecord.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), CameraRecordingActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
