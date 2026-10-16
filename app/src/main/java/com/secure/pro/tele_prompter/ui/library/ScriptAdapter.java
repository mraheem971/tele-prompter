package com.secure.pro.tele_prompter.ui.library;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.secure.pro.tele_prompter.R;
import com.secure.pro.tele_prompter.data.local.entity.ScriptEntity;
import com.secure.pro.tele_prompter.databinding.ItemScriptBinding;
import com.secure.pro.tele_prompter.utils.TextMetricsHelper;

public class ScriptAdapter extends ListAdapter<ScriptEntity, ScriptAdapter.ScriptViewHolder> {

    public interface OnScriptActionListener {
        void onPrompt(ScriptEntity script);
        void onRecord(ScriptEntity script);
        void onEdit(ScriptEntity script);
        void onDuplicate(ScriptEntity script);
        void onShare(ScriptEntity script);
        void onDelete(ScriptEntity script);
    }

    private final OnScriptActionListener listener;

    public ScriptAdapter(OnScriptActionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<ScriptEntity> DIFF_CALLBACK = new DiffUtil.ItemCallback<ScriptEntity>() {
        @Override
        public boolean areItemsTheSame(@NonNull ScriptEntity oldItem, @NonNull ScriptEntity newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull ScriptEntity oldItem, @NonNull ScriptEntity newItem) {
            return oldItem.getUpdatedAt() == newItem.getUpdatedAt() &&
                    oldItem.getTitle().equals(newItem.getTitle()) &&
                    oldItem.getContent().equals(newItem.getContent());
        }
    };

    @NonNull
    @Override
    public ScriptViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemScriptBinding binding = ItemScriptBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ScriptViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ScriptViewHolder holder, int position) {
        ScriptEntity script = getItem(position);
        holder.bind(script, listener);
    }

    static class ScriptViewHolder extends RecyclerView.ViewHolder {
        private final ItemScriptBinding binding;

        public ScriptViewHolder(ItemScriptBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ScriptEntity script, OnScriptActionListener listener) {
            binding.tvScriptTitle.setText(script.getTitle());
            binding.tvCategory.setText(script.getCategory() != null ? script.getCategory() : "General");

            String snippet = script.getContent() != null ? script.getContent().replace("\n", " ").trim() : "";
            binding.tvScriptSnippet.setText(snippet);

            int words = script.getWordCount() > 0 ? script.getWordCount() : TextMetricsHelper.calculateWordCount(script.getContent());
            int durationSec = script.getEstReadingTimeSeconds() > 0 ? script.getEstReadingTimeSeconds() : TextMetricsHelper.calculateReadingTimeSeconds(words);
            binding.tvWordAndDuration.setText(String.format(
                    binding.getRoot().getContext().getString(R.string.editor_word_stats),
                    words,
                    TextMetricsHelper.formatDurationFriendly(durationSec)
            ));

            binding.btnPrompt.setOnClickListener(v -> {
                if (listener != null) listener.onPrompt(script);
            });

            binding.btnRecord.setOnClickListener(v -> {
                if (listener != null) listener.onRecord(script);
            });

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) listener.onEdit(script);
            });

            binding.btnMoreOptions.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(v.getContext(), v);
                popup.getMenu().add(0, 1, 0, R.string.action_edit);
                popup.getMenu().add(0, 2, 1, R.string.action_duplicate);
                popup.getMenu().add(0, 3, 2, R.string.action_share);
                popup.getMenu().add(0, 4, 3, R.string.action_delete);

                popup.setOnMenuItemClickListener(item -> {
                    if (listener == null) return false;
                    int id = item.getItemId();
                    if (id == 1) {
                        listener.onEdit(script);
                        return true;
                    } else if (id == 2) {
                        listener.onDuplicate(script);
                        return true;
                    } else if (id == 3) {
                        listener.onShare(script);
                        return true;
                    } else if (id == 4) {
                        listener.onDelete(script);
                        return true;
                    }
                    return false;
                });
                popup.show();
            });
        }
    }
}
