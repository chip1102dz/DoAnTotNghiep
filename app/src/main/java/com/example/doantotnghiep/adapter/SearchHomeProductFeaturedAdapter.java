package com.example.doantotnghiep.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doantotnghiep.databinding.ItemCategoryHomeBinding;
import com.example.doantotnghiep.databinding.ItemSearchHomeProductFeaturedBinding;
import com.example.doantotnghiep.model.CategoryHome;
import com.example.doantotnghiep.model.Test.Category1;

import java.util.List;

public class SearchHomeProductFeaturedAdapter extends RecyclerView.Adapter<SearchHomeProductFeaturedAdapter.SearchHomeProductFeatureViewHolder>{

    List<Category1> list;

    public SearchHomeProductFeaturedAdapter(List<Category1> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SearchHomeProductFeatureViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSearchHomeProductFeaturedBinding binding = ItemSearchHomeProductFeaturedBinding.inflate(LayoutInflater.from(parent.getContext()), parent,false);
        return new SearchHomeProductFeaturedAdapter.SearchHomeProductFeatureViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchHomeProductFeatureViewHolder holder, int position) {
        Category1 category1 = list.get(position);
        if(category1 == null){
            return;
        }
        holder.binding.HomeProductFeaturedTitle.setText(category1.getTitle());
        holder.binding.HomeProductFeaturedSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    @Override
    public int getItemCount() {
        if(list!=null){
            return list.size();
        }
        return 0;
    }

    public class SearchHomeProductFeatureViewHolder extends RecyclerView.ViewHolder {
        ItemSearchHomeProductFeaturedBinding binding;
        public SearchHomeProductFeatureViewHolder(@NonNull ItemSearchHomeProductFeaturedBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
