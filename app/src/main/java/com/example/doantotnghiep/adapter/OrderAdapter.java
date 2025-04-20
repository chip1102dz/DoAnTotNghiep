package com.example.doantotnghiep.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doantotnghiep.databinding.ItemOrderBinding;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OderViewHolder> {


    @NonNull
    @Override
    public OderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOrderBinding binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new OderViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull OderViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public static class OderViewHolder extends RecyclerView.ViewHolder {

        ItemOrderBinding binding;
        public OderViewHolder(@NonNull ItemOrderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
