package com.example.doantotnghiep.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doantotnghiep.R;
import com.example.doantotnghiep.databinding.ItemFilterBinding;
import com.example.doantotnghiep.fragment.HomeFragment;
import com.example.doantotnghiep.listener.IClickFilterListener;
import com.example.doantotnghiep.listener.IClickSearchListener;
import com.example.doantotnghiep.model.Category;
import com.example.doantotnghiep.model.Filter;

import java.util.List;

public class SearchFeatureAdapter extends RecyclerView.Adapter<SearchFeatureAdapter.SearchFeatureViewHolder>{
    private Context context;
    private final List<Category> mData;


    public SearchFeatureAdapter(List<Category> mData, Context context) {
        this.mData = mData;
        this.context = context;
    }

    @NonNull
    @Override
    public SearchFeatureViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFilterBinding binding = ItemFilterBinding.inflate(LayoutInflater.from(parent.getContext()), parent,false);
        return new SearchFeatureViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchFeatureViewHolder holder, int position) {
        Category category = mData.get(position);
        if (category == null) return;
        holder.binding.tvTitleFilter.setText(category.getName());
        if (category.isSelected()) {
            holder.binding.layoutFilter.setBackgroundResource(R.drawable.bg_button_enable_corner_10);
            int color = ContextCompat.getColor(context, R.color.white);
            holder.binding.tvTitleFilter.setTextColor(color);
        } else {
            holder.binding.layoutFilter.setBackgroundResource(R.drawable.bg_button_disable_corner_10);
            int color = ContextCompat.getColor(context, R.color.textColorHeading);
            holder.binding.tvTitleFilter.setTextColor(color);
        }


    }

    @Override
    public int getItemCount() {
        if(mData!=null){
            return mData.size();
        }
        return 0;
    }
    public void release() {
        if (context != null) context = null;
    }

    public static class SearchFeatureViewHolder extends RecyclerView.ViewHolder {
        ItemFilterBinding binding;
        public SearchFeatureViewHolder(@NonNull ItemFilterBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
