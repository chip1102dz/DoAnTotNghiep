package com.example.doantotnghiep.adapter.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doantotnghiep.databinding.ItemAdminVoucherBinding;
import com.example.doantotnghiep.listener.IOnAdminManagerVoucherListener;
import com.example.doantotnghiep.model.Voucher;

import java.util.List;

public class AdminVoucherAdapter extends RecyclerView.Adapter<AdminVoucherAdapter.AdminVoucherViewHolder> {

    private final List<Voucher> mListVoucher;
    private final IOnAdminManagerVoucherListener mListener;

    public AdminVoucherAdapter(List<Voucher> list, IOnAdminManagerVoucherListener listener) {
        this.mListVoucher = list;
        this.mListener = listener;
    }

    @NonNull
    @Override
    public AdminVoucherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminVoucherBinding binding = ItemAdminVoucherBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new AdminVoucherViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminVoucherViewHolder holder, int position) {
        Voucher voucher = mListVoucher.get(position);
        if (voucher == null) return;
        holder.binding.tvTitle.setText(voucher.getTitle());
        holder.binding.tvMinimum.setText(voucher.getMinimumText());
        holder.binding.imgEdit.setOnClickListener(v -> mListener.onClickUpdateVoucher(voucher));
        holder.binding.imgDelete.setOnClickListener(v -> mListener.onClickDeleteVoucher(voucher));
    }

    @Override
    public int getItemCount() {
        if (mListVoucher != null) {
            return mListVoucher.size();
        }
        return 0;
    }

    public static class AdminVoucherViewHolder extends RecyclerView.ViewHolder {

        ItemAdminVoucherBinding binding;

        public AdminVoucherViewHolder(@NonNull ItemAdminVoucherBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
