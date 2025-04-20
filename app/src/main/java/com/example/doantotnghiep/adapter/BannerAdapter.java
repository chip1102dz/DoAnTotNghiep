package com.example.doantotnghiep.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.example.doantotnghiep.databinding.ItemBannerBinding;
import com.example.doantotnghiep.listener.IClickProductListener;
import com.example.doantotnghiep.model.Product;
import com.example.doantotnghiep.model.Test.Banner;
import com.example.doantotnghiep.utils.GlideUtils;

import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {

    private final List<Product> mListProduct;

    private final IClickProductListener iClickProductListener;

    public BannerAdapter(List<Product> mListProduct, IClickProductListener iClickProductListener) {
        this.mListProduct = mListProduct;
        this.iClickProductListener = iClickProductListener;
    }


    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBannerBinding binding = ItemBannerBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new BannerViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        Product product = mListProduct.get(position);
        if(product == null){
            return;
        }
        GlideUtils.loadUrlBanner(product.getBanner(), holder.binding.imgBanner);
        holder.binding.imgBanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iClickProductListener.onClickProductItem(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        if(mListProduct!=null){
            return mListProduct.size();
        }
        return 0;
    }

    public static class BannerViewHolder extends RecyclerView.ViewHolder {
        ItemBannerBinding binding;
        public BannerViewHolder(@NonNull ItemBannerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
