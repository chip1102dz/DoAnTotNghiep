package com.example.doantotnghiep.adapter.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.doantotnghiep.databinding.ItemAdminRoleBinding;
import com.example.doantotnghiep.model.Admin;

import java.util.List;

public class AdminRoleAdapter extends RecyclerView.Adapter<AdminRoleAdapter.AdminRoleViewHolder> {

    private final List<Admin> mListAdmin;

    public AdminRoleAdapter(List<Admin> mListAdmin) {
        this.mListAdmin = mListAdmin;
    }

    @NonNull
    @Override
    public AdminRoleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminRoleBinding binding = ItemAdminRoleBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new AdminRoleViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminRoleViewHolder holder, int position) {
        Admin admin = mListAdmin.get(position);
        if (admin == null) return;
        holder.binding.tvEmail.setText(admin.getEmail());
    }

    @Override
    public int getItemCount() {
        if (mListAdmin != null) {
            return mListAdmin.size();
        }
        return 0;
    }

    public static class AdminRoleViewHolder extends RecyclerView.ViewHolder {

        ItemAdminRoleBinding binding;

        public AdminRoleViewHolder(@NonNull ItemAdminRoleBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
