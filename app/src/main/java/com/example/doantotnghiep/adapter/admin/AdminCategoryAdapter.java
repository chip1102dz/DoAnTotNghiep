package com.example.doantotnghiep.adapter.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.doantotnghiep.databinding.ItemAdminCategoryBinding;
import com.example.doantotnghiep.listener.IOnAdminManagerCategoryListener;
import com.example.doantotnghiep.model.Category;

import java.util.List;

public class AdminCategoryAdapter extends RecyclerView.Adapter<AdminCategoryAdapter.AdminCategoryViewHolder> {

    private final List<Category> mListCategory;
    private final IOnAdminManagerCategoryListener mListener;

    public AdminCategoryAdapter(List<Category> list, IOnAdminManagerCategoryListener listener) {
        this.mListCategory = list;
        this.mListener = listener;
    }

    @NonNull
    @Override
    public AdminCategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminCategoryBinding binding = ItemAdminCategoryBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new AdminCategoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminCategoryViewHolder holder, int position) {
        Category category = mListCategory.get(position);
        if (category == null) return;
        holder.binding.tvName.setText(category.getName());
        holder.binding.imgEdit.setOnClickListener(v -> mListener.onClickUpdateCategory(category));
        holder.binding.imgDelete.setOnClickListener(v -> mListener.onClickDeleteCategory(category));
        holder.binding.layoutItem.setOnClickListener(v -> mListener.onClickItemCategory(category));
    }

    @Override
    public int getItemCount() {
        if (mListCategory != null) {
            return mListCategory.size();
        }
        return 0;
    }

    public static class AdminCategoryViewHolder extends RecyclerView.ViewHolder {

        ItemAdminCategoryBinding binding;
        public AdminCategoryViewHolder(@NonNull ItemAdminCategoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
