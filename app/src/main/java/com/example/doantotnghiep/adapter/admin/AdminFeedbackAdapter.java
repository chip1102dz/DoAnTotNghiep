package com.example.doantotnghiep.adapter.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.doantotnghiep.databinding.ItemAdminFeedbackBinding;
import com.example.doantotnghiep.model.Feedback;

import java.util.List;

public class AdminFeedbackAdapter extends RecyclerView.Adapter<AdminFeedbackAdapter.AdminFeedbackViewHolder> {

    private final List<Feedback> mListFeedback;

    public AdminFeedbackAdapter(List<Feedback> list) {
        this.mListFeedback = list;
    }

    @NonNull
    @Override
    public AdminFeedbackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminFeedbackBinding binding = ItemAdminFeedbackBinding.inflate(LayoutInflater.from(parent.getContext()),parent, false);
        return new AdminFeedbackViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminFeedbackViewHolder holder, int position) {
        Feedback feedback = mListFeedback.get(position);
        if (feedback == null) return;
        holder.binding.tvEmail.setText(feedback.getEmail());
        holder.binding.tvFeedback.setText(feedback.getComment());
    }

    @Override
    public int getItemCount() {
        if (mListFeedback != null) {
            return mListFeedback.size();
        }
        return 0;
    }

    public static class AdminFeedbackViewHolder extends RecyclerView.ViewHolder {
        ItemAdminFeedbackBinding binding;
        public AdminFeedbackViewHolder(@NonNull ItemAdminFeedbackBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
