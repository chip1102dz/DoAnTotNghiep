package com.example.doantotnghiep.adapter.admin;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doantotnghiep.databinding.ItemAdminProductBinding;
import com.example.doantotnghiep.listener.IOnAdminManagerProductListener;
import com.example.doantotnghiep.model.Product;
import com.example.doantotnghiep.utils.Constant;
import com.example.doantotnghiep.utils.GlideUtils;

import java.util.List;

public class AdminProductAdapter extends RecyclerView.Adapter<AdminProductAdapter.AdminProductViewHolder> {

    private final List<Product> listProduct;
    private final IOnAdminManagerProductListener mListener;

    public AdminProductAdapter(List<Product> list, IOnAdminManagerProductListener listener) {
        this.listProduct = list;
        this.mListener = listener;
    }

    @NonNull
    @Override
    public AdminProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminProductBinding binding = ItemAdminProductBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new AdminProductViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminProductViewHolder holder, int position) {
        Product product = listProduct.get(position);
        if (product == null) return;

        GlideUtils.loadUrl(product.getImage(), holder.binding.imgProduct);
        holder.binding.tvName.setText(product.getName());

        if (product.getSale() <= 0) {
            holder.binding.tvPrice.setVisibility(View.GONE);
            String strPrice = product.getPrice() + Constant.CURRENCY;
            holder.binding.tvPriceSale.setText(strPrice);
        } else {
            holder.binding.tvPrice.setVisibility(View.VISIBLE);

            String strOldPrice = product.getPrice() + Constant.CURRENCY;
            holder.binding.tvPrice.setText(strOldPrice);
            holder.binding.tvPrice.setPaintFlags(holder.binding.tvPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

            String strRealPrice = product.getRealPrice() + Constant.CURRENCY;
            holder.binding.tvPriceSale.setText(strRealPrice);
        }
        if (product.getCategory_id() > 0) {
            holder.binding.layoutCategory.setVisibility(View.VISIBLE);
            holder.binding.tvCategory.setText(product.getCategory_name());
        } else {
            holder.binding.layoutCategory.setVisibility(View.GONE);
        }
        if (product.isFeatured()) {
            holder.binding.tvFeatured.setText("Có");
        } else {
            holder.binding.tvFeatured.setText("Không");
        }

        holder.binding.imgEdit.setOnClickListener(view -> mListener.onClickUpdateProduct(product));
        holder.binding.imgDelete.setOnClickListener(view -> mListener.onClickDeleteProduct(product));
    }

    @Override
    public int getItemCount() {
        if (listProduct != null) {
            return listProduct.size();
        }
        return 0;
    }

    public static class AdminProductViewHolder extends RecyclerView.ViewHolder {

        ItemAdminProductBinding binding;

        public AdminProductViewHolder(@NonNull ItemAdminProductBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
