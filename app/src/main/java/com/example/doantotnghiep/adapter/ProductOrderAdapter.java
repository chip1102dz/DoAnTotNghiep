package com.example.doantotnghiep.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doantotnghiep.databinding.ItemProductOrderBinding;
import com.example.doantotnghiep.model.ProductOrder;
import com.example.doantotnghiep.utils.Constant;
import com.example.doantotnghiep.utils.GlideUtils;

import java.util.List;

public class ProductOrderAdapter extends RecyclerView.Adapter<ProductOrderAdapter.ProductOrderViewHolder> {

    private final List<ProductOrder> listProductOrder;

    public ProductOrderAdapter(List<ProductOrder> list) {
        this.listProductOrder = list;
    }

    @NonNull
    @Override
    public ProductOrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProductOrderBinding binding = ItemProductOrderBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ProductOrderViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductOrderViewHolder holder, int position) {
        ProductOrder productOrder = listProductOrder.get(position);
        if (productOrder == null) return;

        GlideUtils.loadUrl(productOrder.getImage(), holder.binding.imgProduct);
        holder.binding.tvName.setText(productOrder.getName());
        String strPrice = productOrder.getPrice() + Constant.CURRENCY;
        holder.binding.tvPrice.setText(strPrice);
        holder.binding.tvDescription.setText(productOrder.getDescription());
        String strCount = "x" + productOrder.getCount();
        holder.binding.tvCount.setText(strCount);
    }

    @Override
    public int getItemCount() {
        if (listProductOrder != null) {
            return listProductOrder.size();
        }
        return 0;
    }

    public static class ProductOrderViewHolder extends RecyclerView.ViewHolder {

        ItemProductOrderBinding binding;

        public ProductOrderViewHolder(@NonNull ItemProductOrderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
