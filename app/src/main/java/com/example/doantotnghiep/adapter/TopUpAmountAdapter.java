// TopUpAmountAdapter
package com.example.doantotnghiep.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doantotnghiep.R;
import com.example.doantotnghiep.databinding.ItemTopUpAmountBinding;
import com.example.doantotnghiep.model.TopUpAmount;

import java.util.List;

public class TopUpAmountAdapter extends RecyclerView.Adapter<TopUpAmountAdapter.TopUpAmountViewHolder> {

    private final List<TopUpAmount> listTopUpAmounts;
    private final IClickTopUpAmountListener iClickTopUpAmountListener;
    private Context context;

    public interface IClickTopUpAmountListener {
        void onClickTopUpAmount(TopUpAmount topUpAmount);
    }

    public TopUpAmountAdapter(List<TopUpAmount> list, IClickTopUpAmountListener listener) {
        this.listTopUpAmounts = list;
        this.iClickTopUpAmountListener = listener;
    }

    @NonNull
    @Override
    public TopUpAmountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        ItemTopUpAmountBinding binding = ItemTopUpAmountBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new TopUpAmountViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TopUpAmountViewHolder holder, int position) {
        TopUpAmount topUpAmount = listTopUpAmounts.get(position);
        if (topUpAmount == null) return;

        holder.binding.tvAmount.setText(topUpAmount.getDisplayText());

        // Update selection state
        if (topUpAmount.isSelected()) {
            holder.binding.layoutAmount.setBackgroundResource(R.drawable.bg_selected_amount);
            holder.binding.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
        } else {
            holder.binding.layoutAmount.setBackgroundResource(R.drawable.bg_unselected_amount);
            holder.binding.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.textColorHeading));
        }

        holder.binding.layoutAmount.setOnClickListener(v -> {
            // Clear all other selections
            for (TopUpAmount amount : listTopUpAmounts) {
                amount.setSelected(false);
            }

            // Select current item
            topUpAmount.setSelected(true);
            notifyDataSetChanged();

            iClickTopUpAmountListener.onClickTopUpAmount(topUpAmount);
        });
    }

    @Override
    public int getItemCount() {
        return listTopUpAmounts != null ? listTopUpAmounts.size() : 0;
    }

    public void release() {
        if (context != null) context = null;
    }

    public static class TopUpAmountViewHolder extends RecyclerView.ViewHolder {
        ItemTopUpAmountBinding binding;

        public TopUpAmountViewHolder(@NonNull ItemTopUpAmountBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}