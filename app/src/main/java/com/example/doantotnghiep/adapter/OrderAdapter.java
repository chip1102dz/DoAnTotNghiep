package com.example.doantotnghiep.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doantotnghiep.R;
import com.example.doantotnghiep.databinding.ItemOrderBinding;
import com.example.doantotnghiep.model.Order;
import com.example.doantotnghiep.model.ProductOrder;
import com.example.doantotnghiep.prefs.DataStoreManager;
import com.example.doantotnghiep.utils.Constant;
import com.example.doantotnghiep.utils.GlideUtils;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OderViewHolder> {
    private Context context;
    private final List<Order> listOrder;
    private final IClickOrderListener iClickOrderListener;

    public interface IClickOrderListener {
        void onClickTrackingOrder(long orderId);
        void onClickReceiptOrder(Order order);
    }

    public OrderAdapter(Context context, List<Order> list, IClickOrderListener listener) {
        this.context = context;
        this.listOrder = list;
        this.iClickOrderListener = listener;
    }

    @NonNull
    @Override
    public OderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOrderBinding binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new OderViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull OderViewHolder holder, int position) {
        Order order = listOrder.get(position);
        if (order == null) return;

        if (DataStoreManager.getUser().isAdmin()) {
            holder.binding.layoutUser.setVisibility(View.VISIBLE);
            holder.binding.tvUser.setText(order.getUserEmail());
        } else {
            holder.binding.layoutUser.setVisibility(View.GONE);
        }
        ProductOrder firstProductOrder = order.getProducts().get(0);
        GlideUtils.loadUrl(firstProductOrder.getImage(), holder.binding.imgProduct);
        holder.binding.tvOrderId.setText(String.valueOf(order.getId()));
        String strTotal = order.getTotal() + Constant.CURRENCY;
        holder.binding.tvTotal.setText(strTotal);
        holder.binding.tvProductsName.setText(order.getListProductsName());
        String strQuantity = "(" + order.getProducts().size() + " " + context.getString(R.string.label_item) + ")";
        holder.binding.tvQuantity.setText(strQuantity);

        if (Order.STATUS_COMPLETE == order.getStatus()) {
            holder.binding.tvSuccess.setVisibility(View.VISIBLE);
            holder.binding.tvAction.setText(context.getString(R.string.label_receipt_order));
            holder.binding.layoutReview.setVisibility(View.VISIBLE);
            holder.binding.tvRate.setText(String.valueOf(order.getRate()));
            holder.binding.tvReview.setText(order.getReview());
            holder.binding.layoutAction.setOnClickListener(v ->
                    iClickOrderListener.onClickReceiptOrder(order));
        } else {
            holder.binding.tvSuccess.setVisibility(View.GONE);
            holder.binding.tvAction.setText(context.getString(R.string.label_tracking_order));
            holder.binding.layoutReview.setVisibility(View.GONE);
            holder.binding.layoutAction.setOnClickListener(v ->
                    iClickOrderListener.onClickTrackingOrder(order.getId()));
        }
    }

    @Override
    public int getItemCount() {
        if (listOrder != null) {
            return listOrder.size();
        }
        return 0;
    }
    public void release() {
        if (context != null) context = null;
    }

    public static class OderViewHolder extends RecyclerView.ViewHolder {

        ItemOrderBinding binding;
        public OderViewHolder(@NonNull ItemOrderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
