package com.example.doantotnghiep.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doantotnghiep.R;
import com.example.doantotnghiep.databinding.ItemAddressBinding;
import com.example.doantotnghiep.model.Address;

import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder>{
    private final List<Address> listAddress;
    private final IClickAddressListener iClickAddressListener;

    public interface IClickAddressListener {
        void onClickAddressItem(Address address);
    }

    public AddressAdapter(List<Address> list, IClickAddressListener listener) {
        this.listAddress = list;
        this.iClickAddressListener = listener;
    }
    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAddressBinding binding = ItemAddressBinding.inflate(LayoutInflater.from(parent.getContext()), parent,false);
        return new AddressViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        Address address = listAddress.get(position);
        if (address == null) return;
        holder.binding.tvName.setText(address.getName());
        holder.binding.tvPhone.setText(address.getPhone());
        holder.binding.tvAddress.setText(address.getAddress());
        if (address.isSelected()) {
            holder.binding.imgStatus.setImageResource(R.drawable.ic_item_selected);
        } else {
            holder.binding.imgStatus.setImageResource(R.drawable.ic_item_unselect);
        }

        holder.binding.layoutItem.setOnClickListener(view -> iClickAddressListener.onClickAddressItem(address));
    }

    @Override
    public int getItemCount() {
        if (listAddress != null) {
            return listAddress.size();
        }
        return 0;
    }

    public static class AddressViewHolder extends RecyclerView.ViewHolder {
        ItemAddressBinding binding;
        public AddressViewHolder(@NonNull ItemAddressBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
