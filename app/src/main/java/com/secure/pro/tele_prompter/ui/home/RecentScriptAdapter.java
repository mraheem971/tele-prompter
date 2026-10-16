package com.secure.pro.tele_prompter.ui.home;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.secure.pro.tele_prompter.R;
import com.secure.pro.tele_prompter.data.local.entity.ScriptEntity;
import com.secure.pro.tele_prompter.databinding.ItemRecentScriptBinding;
import com.secure.pro.tele_prompter.utils.TextMetricsHelper;

import java.util.ArrayList;
import java.util.List;

public class RecentScriptAdapter extends RecyclerView.Adapter<RecentScriptAdapter.RecentViewHolder> {

    public interface OnRecentActionListener {
        void onPrompt(ScriptEntity script);
        void onRecord(ScriptEntity script);
        void onClick(ScriptEntity script);
    }

    private List<ScriptEntity> items = new ArrayList<>();
    private final OnRecentActionListener listener;

    public RecentScriptAdapter(OnRecentActionListener listener) {
        this.listener = listener;
    }

    public void submitList(List<ScriptEntity> list) {
        this.items = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRecentScriptBinding binding = ItemRecentScriptBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new RecentViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecentViewHolder holder, int position) {
        holder.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class RecentViewHolder extends RecyclerView.ViewHolder {
        private final ItemRecentScriptBinding binding;

        public RecentViewHolder(ItemRecentScriptBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ScriptEntity script, OnRecentActionListener listener) {
            binding.tvRecentTitle.setText(script.getTitle());
            binding.tvRecentCategory.setText(script.getCategory() != null ? script.getCategory() : "General");

            int words = script.getWordCount() > 0 ? script.getWordCount() : TextMetricsHelper.calculateWordCount(script.getContent());
            int durationSec = script.getEstReadingTimeSeconds() > 0 ? script.getEstReadingTimeSeconds() : TextMetricsHelper.calculateReadingTimeSeconds(words);
            binding.tvRecentStats.setText(String.format(
                    binding.getRoot().getContext().getString(R.string.editor_word_stats),
                    words,
                    TextMetricsHelper.formatDurationFriendly(durationSec)
            ));

            binding.btnRecentPrompt.setOnClickListener(v -> {
                if (listener != null) listener.onPrompt(script);
            });

            binding.btnRecentRecord.setOnClickListener(v -> {
                if (listener != null) listener.onRecord(script);
            });

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) listener.onClick(script);
            });
        }
    }
}
