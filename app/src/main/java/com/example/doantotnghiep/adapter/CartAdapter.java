package com.example.doantotnghiep.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doantotnghiep.databinding.ItemCartBinding;
import com.example.doantotnghiep.model.Product;
import com.example.doantotnghiep.utils.Constant;
import com.example.doantotnghiep.utils.GlideUtils;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private final List<Product> listProduct;
    private final IClickCartListener iClickCartListener;

    public interface IClickCartListener {
        void onClickDeleteItem(Product product, int position);
        void onClickUpdateItem(Product product, int position);
        void onClickEditItem(Product product);
    }

    public CartAdapter(List<Product> list, IClickCartListener listener) {
        this.listProduct = list;
        this.iClickCartListener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCartBinding binding = ItemCartBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CartViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        Product product = listProduct.get(position);
        if (product == null) return;

        GlideUtils.loadUrl(product.getImage(), holder.binding.imgProduct);
        holder.binding.tvName.setText(product.getName());
        String strPrice = product.getPriceOneProduct() + Constant.CURRENCY;
        holder.binding.tvPrice.setText(strPrice);
        holder.binding.tvDescription.setText(product.getDescription());
        String strQuantity = "x" + product.getCount();
        holder.binding.tvQuantity.setText(strQuantity);
        holder.binding.tvCount.setText(String.valueOf(product.getCount()));

        holder.binding.tvSub.setOnClickListener(v -> {
            String strCount = holder.binding.tvCount.getText().toString();
            int count = Integer.parseInt(strCount);
            if (count <= 1) {
                return;
            }
            int newCount = count - 1;
            holder.binding.tvCount.setText(String.valueOf(newCount));

            int totalPrice = product.getPriceOneProduct() * newCount;
            product.setCount(newCount);
            product.setTotalPrice(totalPrice);

            iClickCartListener.onClickUpdateItem(product, holder.getAdapterPosition());
        });

        holder.binding.tvAdd.setOnClickListener(v -> {
            int newCount = Integer.parseInt(holder.binding.tvCount.getText().toString()) + 1;
            holder.binding.tvCount.setText(String.valueOf(newCount));

            int totalPrice = product.getPriceOneProduct() * newCount;
            product.setCount(newCount);
            product.setTotalPrice(totalPrice);

            iClickCartListener.onClickUpdateItem(product, holder.getAdapterPosition());
        });

        holder.binding.imgEdit.setOnClickListener(v -> iClickCartListener.onClickEditItem(product));
        holder.binding.imgDelete.setOnClickListener(v
                -> iClickCartListener.onClickDeleteItem(product, holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        if (listProduct != null) {
            return listProduct.size();
        }
        return 0;
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        ItemCartBinding binding;
        public CartViewHolder(@NonNull ItemCartBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
