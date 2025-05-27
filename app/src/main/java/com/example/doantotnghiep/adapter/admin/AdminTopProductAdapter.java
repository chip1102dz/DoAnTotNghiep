package com.example.doantotnghiep.adapter.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.doantotnghiep.databinding.ItemAdminTopProductBinding;
import com.example.doantotnghiep.model.ProductOrder;
import com.example.doantotnghiep.utils.Constant;

import java.util.List;

public class AdminTopProductAdapter extends RecyclerView.Adapter<AdminTopProductAdapter.AdminTopProductViewHolder> {

    private final List<ProductOrder> mListProducts;

    public AdminTopProductAdapter(List<ProductOrder> mListProducts) {
        this.mListProducts = mListProducts;
    }

    @NonNull
    @Override
    public AdminTopProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminTopProductBinding binding = ItemAdminTopProductBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new AdminTopProductViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminTopProductViewHolder holder, int position) {
        ProductOrder product = mListProducts.get(position);
        if (product == null) return;
        holder.binding.tvStt.setText(String.valueOf(position + 1));
        holder.binding.tvProductName.setText(product.getName());
        holder.binding.tvQuantity.setText(String.valueOf(product.getCount()));
        String strTotalPrice = product.getPrice() * product.getCount() + Constant.CURRENCY;
        holder.binding.tvTotalPrice.setText(strTotalPrice);
    }

    @Override
    public int getItemCount() {
        if (mListProducts != null) {
            return mListProducts.size();
        }
        return 0;
    }

    public static class AdminTopProductViewHolder extends RecyclerView.ViewHolder {

        ItemAdminTopProductBinding binding;

        public AdminTopProductViewHolder(@NonNull ItemAdminTopProductBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
