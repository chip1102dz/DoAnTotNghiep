package com.example.doantotnghiep.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doantotnghiep.R;
import com.example.doantotnghiep.databinding.ItemPaymentMethodBinding;
import com.example.doantotnghiep.model.PaymentMethod;
import com.example.doantotnghiep.utils.Constant;

import java.util.List;

public class PaymentMethodAdapter extends RecyclerView.Adapter<PaymentMethodAdapter.PaymentMethodViewHolder>{

    private final List<PaymentMethod> listPaymentMethod;
    private final IClickPaymentMethodListener iClickPaymentMethodListener;

    public interface IClickPaymentMethodListener {
        void onClickPaymentMethodItem(PaymentMethod paymentMethod);
    }

    public PaymentMethodAdapter(List<PaymentMethod> list, IClickPaymentMethodListener listener) {
        this.listPaymentMethod = list;
        this.iClickPaymentMethodListener = listener;
    }

    @NonNull
    @Override
    public PaymentMethodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPaymentMethodBinding binding = ItemPaymentMethodBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new PaymentMethodViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentMethodViewHolder holder, int position) {
        PaymentMethod paymentMethod = listPaymentMethod.get(position);
        if (paymentMethod == null) return;

        switch (paymentMethod.getId()) {
            case Constant.TYPE_GOPAY:
                holder.binding.imgPaymentMethod.setImageResource(R.drawable.ic_money);
                break;
            case Constant.TYPE_BALANCE:
                holder.binding.imgPaymentMethod.setImageResource(R.drawable.ic_account_balance_wallet);
                break;
        }

        holder.binding.tvName.setText(paymentMethod.getName());
        holder.binding.tvDescription.setText(paymentMethod.getDescription());

        if (paymentMethod.isSelected()) {
            holder.binding.imgStatus.setImageResource(R.drawable.ic_item_selected);
        } else {
            holder.binding.imgStatus.setImageResource(R.drawable.ic_item_unselect);
        }

        holder.binding.layoutItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iClickPaymentMethodListener.onClickPaymentMethodItem(paymentMethod);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (listPaymentMethod != null) {
            return listPaymentMethod.size();
        }
        return 0;
    }

    public static class PaymentMethodViewHolder extends RecyclerView.ViewHolder {
        ItemPaymentMethodBinding binding;
        public PaymentMethodViewHolder(@NonNull ItemPaymentMethodBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}