package com.example.doantotnghiep.adapter.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.doantotnghiep.databinding.ItemAdminRevenueBinding;
import com.example.doantotnghiep.model.Order;
import com.example.doantotnghiep.utils.Constant;
import com.example.doantotnghiep.utils.DateTimeUtils;

import java.util.List;

public class AdminRevenueAdapter extends RecyclerView.Adapter<AdminRevenueAdapter.RevenueViewHolder> {

    private final List<Order> mListOrder;

    public AdminRevenueAdapter(List<Order> mListOrder) {
        this.mListOrder = mListOrder;
    }

    @NonNull
    @Override
    public RevenueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminRevenueBinding binding = ItemAdminRevenueBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new RevenueViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RevenueViewHolder holder, int position) {
        Order order = mListOrder.get(position);
        if (order == null) {
            return;
        }
        holder.binding.tvId.setText(String.valueOf(order.getId()));
        holder.binding.tvDate.setText(DateTimeUtils.convertTimeStampToDate_2(order.getId()));

        String strAmount = order.getTotal() + Constant.CURRENCY;
        holder.binding.tvTotalAmount.setText(strAmount);
    }

    @Override
    public int getItemCount() {
        if (mListOrder != null) {
            return mListOrder.size();
        }
        return 0;
    }

    public static class RevenueViewHolder extends RecyclerView.ViewHolder {

        ItemAdminRevenueBinding binding;

        public RevenueViewHolder(@NonNull ItemAdminRevenueBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
