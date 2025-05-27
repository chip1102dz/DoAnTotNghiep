package com.example.doantotnghiep.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.doantotnghiep.MyApplication;
import com.example.doantotnghiep.R;
import com.example.doantotnghiep.activity.ReceiptOrderActivity;
import com.example.doantotnghiep.activity.TrackingOrderActivity;
import com.example.doantotnghiep.adapter.OrderAdapter;

import com.example.doantotnghiep.databinding.FragmentOderBinding;
import com.example.doantotnghiep.model.Order;
import com.example.doantotnghiep.model.TabOrder;
import com.example.doantotnghiep.prefs.DataStoreManager;
import com.example.doantotnghiep.utils.Constant;
import com.example.doantotnghiep.utils.GlobalFunction;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.List;


public class OrderFragment extends Fragment {

    FragmentOderBinding binding;
    private int orderTabType;
    private List<Order> listOrder;
    private OrderAdapter orderAdapter;
    private ValueEventListener mOrderAllValueEventListener;
    private ValueEventListener mOrderValueEventListener;
    public static OrderFragment newInstance(int type) {
        OrderFragment orderFragment = new OrderFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(Constant.ORDER_TAB_TYPE, type);
        orderFragment.setArguments(bundle);
        return orderFragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentOderBinding.inflate(inflater, container, false);
        getDataArguments();
        initUi();
        if (DataStoreManager.getUser().isAdmin()) {
            getListOrderAllUsersFromFirebase();
        } else {
            getListOrderFromFirebase();
        }
        return binding.getRoot();
    }
    private void getDataArguments() {
        Bundle bundle = getArguments();
        if (bundle == null) return;
        orderTabType = bundle.getInt(Constant.ORDER_TAB_TYPE);
    }

    private void initUi() {
        listOrder = new ArrayList<>();
        RecyclerView rcvOrder = binding.rcvOrder;
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        rcvOrder.setLayoutManager(linearLayoutManager);
        orderAdapter = new OrderAdapter(getActivity(), listOrder, new OrderAdapter.IClickOrderListener() {
            @Override
            public void onClickTrackingOrder(long orderId) {
                Bundle bundle = new Bundle();
                bundle.putLong(Constant.ORDER_ID, orderId);
                GlobalFunction.startActivity(getActivity(), TrackingOrderActivity.class, bundle);
            }

            @Override
            public void onClickReceiptOrder(Order order) {
                Bundle bundle = new Bundle();
                bundle.putLong(Constant.ORDER_ID, order.getId());
                GlobalFunction.startActivity(getActivity(), ReceiptOrderActivity.class, bundle);
            }
        });
        rcvOrder.setAdapter(orderAdapter);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getListOrderAllUsersFromFirebase() {
        if (getActivity() == null) return;
        mOrderAllValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (listOrder != null) {
                    listOrder.clear();
                } else {
                    listOrder = new ArrayList<>();
                }
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Order order = dataSnapshot.getValue(Order.class);
                    if (order != null) {
                        if (TabOrder.TAB_ORDER_PROCESS == orderTabType) {
                            if (Order.STATUS_COMPLETE != order.getStatus()) {
                                listOrder.add(0, order);
                            }
                        } else if (TabOrder.TAB_ORDER_DONE == orderTabType) {
                            if (Order.STATUS_COMPLETE == order.getStatus()) {
                                listOrder.add(0, order);
                            }
                        }
                    }
                }
                if (orderAdapter != null) orderAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        MyApplication.get(getActivity()).getOrderDatabaseReference()
                .addValueEventListener(mOrderAllValueEventListener);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getListOrderFromFirebase() {
        if (getActivity() == null) return;
        mOrderValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (listOrder != null) {
                    listOrder.clear();
                } else {
                    listOrder = new ArrayList<>();
                }
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Order order = dataSnapshot.getValue(Order.class);
                    if (order != null) {
                        if (TabOrder.TAB_ORDER_PROCESS == orderTabType) {
                            if (Order.STATUS_COMPLETE != order.getStatus()) {
                                listOrder.add(0, order);
                            }
                        } else if (TabOrder.TAB_ORDER_DONE == orderTabType) {
                            if (Order.STATUS_COMPLETE == order.getStatus()) {
                                listOrder.add(0, order);
                            }
                        }
                    }
                }
                if (orderAdapter != null) orderAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        MyApplication.get(getActivity()).getOrderDatabaseReference()
                .orderByChild("userEmail").equalTo(DataStoreManager.getUser().getEmail())
                .addValueEventListener(mOrderValueEventListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (orderAdapter != null) orderAdapter.release();
        if (getActivity() != null && mOrderAllValueEventListener != null) {
            MyApplication.get(getActivity()).getOrderDatabaseReference()
                    .removeEventListener(mOrderAllValueEventListener);
        }
        if (getActivity() != null && mOrderValueEventListener != null) {
            MyApplication.get(getActivity()).getOrderDatabaseReference()
                    .removeEventListener(mOrderValueEventListener);
        }
    }
}