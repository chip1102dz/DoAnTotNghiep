package com.example.doantotnghiep.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doantotnghiep.R;
import com.example.doantotnghiep.databinding.ItemFilterBinding;
import com.example.doantotnghiep.listener.IClickFilterListener;
import com.example.doantotnghiep.model.Filter;

import java.util.List;

public class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.FilterViewHolder>{

    private Context context;
    private final List<Filter> listFilter;
    private final IClickFilterListener iClickFilterListener;

    public FilterAdapter(Context context, List<Filter> listFilter, IClickFilterListener iClickFilterListener) {
        this.context = context;
        this.listFilter = listFilter;
        this.iClickFilterListener = iClickFilterListener;
    }


    @NonNull
    @Override
    public FilterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFilterBinding binding = ItemFilterBinding.inflate(LayoutInflater.from(parent.getContext()), parent,false);
        return new FilterAdapter.FilterViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FilterViewHolder holder, int position) {
        Filter filter = listFilter.get(position);
        if (filter == null) return;
        holder.binding.tvTitleFilter.setText(filter.getName());
        if (filter.isSelected()) {
            holder.binding.layoutFilter.setBackgroundResource(R.drawable.bg_button_enable_corner_10);
            int color = ContextCompat.getColor(context, R.color.white);
            holder.binding.tvTitleFilter.setTextColor(color);
        } else {
            holder.binding.layoutFilter.setBackgroundResource(R.drawable.bg_button_disable_corner_10);
            int color = ContextCompat.getColor(context, R.color.textColorHeading);
            holder.binding.tvTitleFilter.setTextColor(color);

        }

        holder.binding.layoutFilter.setOnClickListener(view
                -> iClickFilterListener.onClickFilterItem(filter));

    }

    @Override
    public int getItemCount() {
        if(listFilter!=null){
            return listFilter.size();
        }
        return 0;
    }
    public void release() {
        if (context != null) context = null;
    }

    public class FilterViewHolder extends RecyclerView.ViewHolder {
        ItemFilterBinding binding;
        public FilterViewHolder(@NonNull ItemFilterBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
