package com.example.doantotnghiep.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.doantotnghiep.databinding.ItemHomeProductRatingBinding;
import com.example.doantotnghiep.listener.IClickProductListener;
import com.example.doantotnghiep.model.Product;
import com.example.doantotnghiep.utils.GlideUtils;

import java.util.List;

public class HomeProductRatingAdapter extends RecyclerView.Adapter<HomeProductRatingAdapter.HomeProductRatingViewHolder>{
    private final List<Product> listProduct;
    private final IClickProductListener iClickProductListener;

    public HomeProductRatingAdapter(List<Product> listProduct, IClickProductListener iClickProductListener) {
        this.listProduct = listProduct;
        this.iClickProductListener = iClickProductListener;
    }

    @NonNull
    @Override
    public HomeProductRatingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHomeProductRatingBinding binding = ItemHomeProductRatingBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new HomeProductRatingViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull HomeProductRatingViewHolder holder, int position) {
        Product product = listProduct.get(position);
        if (product == null) return;

        GlideUtils.loadUrl(product.getImage(), holder.binding.imgProductRating);
        holder.binding.tvProductRating.setText(product.getName());
        holder.binding.layoutProductRating.setOnClickListener(new View.OnClickListener() {
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

    public static class HomeProductRatingViewHolder extends RecyclerView.ViewHolder {
        ItemHomeProductRatingBinding binding;
        public HomeProductRatingViewHolder(@NonNull ItemHomeProductRatingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
