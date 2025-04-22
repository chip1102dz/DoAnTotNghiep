package com.example.doantotnghiep.adapter;


import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doantotnghiep.databinding.ItemProductBinding;
import com.example.doantotnghiep.listener.IClickProductListener;
import com.example.doantotnghiep.model.Product;
import com.example.doantotnghiep.utils.Constant;
import com.example.doantotnghiep.utils.GlideUtils;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder>{
    private final List<Product> listProduct;
    private final IClickProductListener iClickProductListener;

    public ProductAdapter(List<Product> list, IClickProductListener listener) {
        this.listProduct = list;
        this.iClickProductListener = listener;
    }
    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProductBinding binding = ItemProductBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ProductViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = listProduct.get(position);
        if (product == null) return;

        GlideUtils.loadUrl(product.getImage(), holder.binding.imgProduct);
        holder.binding.tvName.setText(product.getName());
        holder.binding.tvDescription.setText(product.getDescription());
        holder.binding.tvRate.setText(String.valueOf(product.getRate()));

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

        holder.binding.layoutItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iClickProductListener.onClickProductItem(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (listProduct != null) {
            return listProduct.size();
        }
        return 0;
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ItemProductBinding binding;
        public ProductViewHolder(@NonNull ItemProductBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
