package com.example.doantotnghiep.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doantotnghiep.databinding.ItemCategoryHomeBinding;
import com.example.doantotnghiep.databinding.ItemOrderBinding;
import com.example.doantotnghiep.model.CategoryHome;

import java.util.List;

public class CategoryHomeAdapter extends RecyclerView.Adapter<CategoryHomeAdapter.CategoryHomeViewHolder>{
    private List<CategoryHome> list;

    public CategoryHomeAdapter(List<CategoryHome> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryHomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCategoryHomeBinding binding = ItemCategoryHomeBinding.inflate(LayoutInflater.from(parent.getContext()), parent,false);
        return new CategoryHomeViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryHomeViewHolder holder, int position) {
        CategoryHome categoryHome = list.get(position);
        if(categoryHome == null){
            return;
        }
        holder.binding.imgCategoryHome.setImageResource(categoryHome.getImg());
        holder.binding.tvCategoryNameHome.setText(categoryHome.getName());
    }

    @Override
    public int getItemCount() {
        if(list!=null){
            return list.size();
        }
        return 0;
    }

    public class CategoryHomeViewHolder extends RecyclerView.ViewHolder {
        ItemCategoryHomeBinding binding;
        public CategoryHomeViewHolder(@NonNull ItemCategoryHomeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
