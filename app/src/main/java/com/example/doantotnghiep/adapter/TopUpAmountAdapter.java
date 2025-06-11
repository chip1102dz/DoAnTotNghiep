package com.example.doantotnghiep.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
    private boolean isEnabled = true; // Thêm field này

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

        // Update selection state và enabled state
        updateItemState(holder, topUpAmount);

        // Set click listener - chỉ hoạt động khi enabled
        holder.binding.layoutAmount.setOnClickListener(v -> {
            if (!isEnabled) return; // Không cho click khi disabled

            // Clear all other selections
            for (TopUpAmount amount : listTopUpAmounts) {
                amount.setSelected(false);
            }

            // Select current item
            topUpAmount.setSelected(true);
            notifyDataSetChanged();

            if (iClickTopUpAmountListener != null) {
                iClickTopUpAmountListener.onClickTopUpAmount(topUpAmount);
            }
        });
    }

    private void updateItemState(TopUpAmountViewHolder holder, TopUpAmount topUpAmount) {
        if (!isEnabled) {
            // Disabled state
            holder.binding.layoutAmount.setBackgroundResource(R.drawable.bg_unselected_amount);
            holder.binding.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.textColorAccent));
            holder.binding.layoutAmount.setAlpha(0.5f);
        } else if (topUpAmount.isSelected()) {
            // Selected state
            holder.binding.layoutAmount.setBackgroundResource(R.drawable.bg_selected_amount);
            holder.binding.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
            holder.binding.layoutAmount.setAlpha(1.0f);
        } else {
            // Normal state
            holder.binding.layoutAmount.setBackgroundResource(R.drawable.bg_unselected_amount);
            holder.binding.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.textColorHeading));
            holder.binding.layoutAmount.setAlpha(1.0f);
        }
    }

    @Override
    public int getItemCount() {
        return listTopUpAmounts != null ? listTopUpAmounts.size() : 0;
    }

    // Thêm method setEnabled
    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
        notifyDataSetChanged(); // Refresh tất cả items
    }

    public boolean isEnabled() {
        return isEnabled;
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
