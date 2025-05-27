package com.example.doantotnghiep.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doantotnghiep.R;
import com.example.doantotnghiep.databinding.ItemVoucherBinding;
import com.example.doantotnghiep.model.Voucher;
import com.example.doantotnghiep.utils.StringUtil;

import java.util.List;

public class VoucherAdapter extends RecyclerView.Adapter<VoucherAdapter.VoucherViewHolder> {
    private Context context;
    private final List<Voucher> listVoucher;
    private final int amount;
    private final IClickVoucherListener iClickVoucherListener;
    public interface IClickVoucherListener {
        void onClickVoucherItem(Voucher voucher);
    }

    public VoucherAdapter(Context context, List<Voucher> list, int amount, IClickVoucherListener listener) {
        this.context = context;
        this.listVoucher = list;
        this.amount = amount;
        this.iClickVoucherListener = listener;
    }
    @NonNull
    @Override
    public VoucherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemVoucherBinding binding = ItemVoucherBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new VoucherViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull VoucherViewHolder holder, int position) {
        Voucher voucher = listVoucher.get(position);
        if (voucher == null) return;
        holder.binding.tvVoucherTitle.setText(voucher.getTitle());
        holder.binding.tvVoucherMinimum.setText(voucher.getMinimumText());
        if (StringUtil.isEmpty(voucher.getCondition(amount))) {
            holder.binding.tvVoucherCondition.setVisibility(View.GONE);
        } else {
            holder.binding.tvVoucherCondition.setVisibility(View.VISIBLE);
            holder.binding.tvVoucherCondition.setText(voucher.getCondition(amount));
        }

        if (voucher.isVoucherEnable(amount)) {
            holder.binding.imgStatus.setVisibility(View.VISIBLE);
            holder.binding.tvVoucherTitle.setTextColor(ContextCompat.getColor(context,
                    R.color.textColorHeading));
            holder.binding.tvVoucherMinimum.setTextColor(ContextCompat.getColor(context,
                    R.color.textColorSecondary));
        } else {
            holder.binding.imgStatus.setVisibility(View.GONE);
            holder.binding.tvVoucherTitle.setTextColor(ContextCompat.getColor(context,
                    R.color.textColorAccent));
            holder.binding.tvVoucherMinimum.setTextColor(ContextCompat.getColor(context,
                    R.color.textColorAccent));
        }
        if (voucher.isSelected()) {
            holder.binding.imgStatus.setImageResource(R.drawable.ic_item_selected);
        } else {
            holder.binding.imgStatus.setImageResource(R.drawable.ic_item_unselect);
        }

        holder.binding.layoutItem.setOnClickListener(view -> {
            if (!voucher.isVoucherEnable(amount)) return;
            iClickVoucherListener.onClickVoucherItem(voucher);
        });
    }

    @Override
    public int getItemCount() {
        if (listVoucher != null) {
            return listVoucher.size();
        }
        return 0;
    }
    public void release() {
        if (context != null) context = null;
    }

    public static class VoucherViewHolder extends RecyclerView.ViewHolder {
        ItemVoucherBinding binding;
        public VoucherViewHolder(@NonNull ItemVoucherBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
