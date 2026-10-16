package com.secure.pro.tele_prompter.ui.home;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.secure.pro.tele_prompter.data.model.ScriptTemplate;
import com.secure.pro.tele_prompter.databinding.ItemTemplateBinding;

import java.util.List;

public class TemplateAdapter extends RecyclerView.Adapter<TemplateAdapter.TemplateViewHolder> {

    public interface OnTemplateSelectedListener {
        void onTemplateSelected(ScriptTemplate template);
    }

    private final List<ScriptTemplate> templates;
    private final OnTemplateSelectedListener listener;

    public TemplateAdapter(List<ScriptTemplate> templates, OnTemplateSelectedListener listener) {
        this.templates = templates;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TemplateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTemplateBinding binding = ItemTemplateBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new TemplateViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TemplateViewHolder holder, int position) {
        holder.bind(templates.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return templates.size();
    }

    static class TemplateViewHolder extends RecyclerView.ViewHolder {
        private final ItemTemplateBinding binding;

        public TemplateViewHolder(ItemTemplateBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ScriptTemplate template, OnTemplateSelectedListener listener) {
            binding.tvTemplateTitle.setText(template.getTitle());
            binding.tvTemplateCategory.setText(template.getCategory());
            binding.tvTemplateDesc.setText(template.getDescription());

            binding.btnUseTemplate.setOnClickListener(v -> {
                if (listener != null) listener.onTemplateSelected(template);
            });

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) listener.onTemplateSelected(template);
            });
        }
    }
}
