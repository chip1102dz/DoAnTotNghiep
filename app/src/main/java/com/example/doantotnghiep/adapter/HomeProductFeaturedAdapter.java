package com.example.doantotnghiep.adapter;

import android.graphics.Paint;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doantotnghiep.databinding.ItemHomeProductFeaturedBinding;
import com.example.doantotnghiep.listener.IClickProductListener;
import com.example.doantotnghiep.model.Product;
import com.example.doantotnghiep.utils.GlideUtils;

import java.util.List;

public class HomeProductFeaturedAdapter extends RecyclerView.Adapter<HomeProductFeaturedAdapter.HomeProductFeaturedViewHolder>{
    private final List<Product> listProduct;
    private final IClickProductListener iClickProductListener;

    public HomeProductFeaturedAdapter(List<Product> listProduct, IClickProductListener iClickProductListener) {
        this.listProduct = listProduct;
        this.iClickProductListener = iClickProductListener;
    }

    @NonNull
    @Override
    public HomeProductFeaturedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull HomeProductFeaturedViewHolder holder, int position) {
        Product product = listProduct.get(position);
        if (product == null) return;

        GlideUtils.loadUrl(product.getImage(), holder.binding.imgProductFeature);
        holder.binding.tvProductFeature.setText(product.getName());
        holder.binding.layoutProductFeature.setOnClickListener(new View.OnClickListener() {
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

    public static class HomeProductFeaturedViewHolder extends RecyclerView.ViewHolder {
        ItemHomeProductFeaturedBinding binding;
        public HomeProductFeaturedViewHolder(@NonNull ItemHomeProductFeaturedBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
