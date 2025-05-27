package com.example.doantotnghiep.activity.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doantotnghiep.MyApplication;
import com.example.doantotnghiep.R;
import com.example.doantotnghiep.activity.BaseActivity;
import com.example.doantotnghiep.adapter.admin.AdminRevenueAdapter;
import com.example.doantotnghiep.databinding.ActivityAdminRevenueBinding;
import com.example.doantotnghiep.listener.IOnSingleClickListener;
import com.example.doantotnghiep.model.Order;
import com.example.doantotnghiep.utils.Constant;
import com.example.doantotnghiep.utils.DateTimeUtils;
import com.example.doantotnghiep.utils.GlobalFunction;
import com.example.doantotnghiep.utils.StringUtil;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.List;

public class AdminRevenueActivity extends BaseActivity {
    ActivityAdminRevenueBinding binding;
    private TextView tvDateFrom, tvDateTo, tvTotalValue;
    private RecyclerView rcvOrderHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminRevenueBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initToolbar();
        initUi();
        initListener();
        getListRevenue();
    }

    private void initToolbar() {
        ImageView imgToolbarBack = binding.toolbar.imgToolbarBack;
        imgToolbarBack.setOnClickListener(view -> onBackPressed());
        TextView tvToolbarTitle = binding.toolbar.tvToolbarTitle;
        tvToolbarTitle.setText(getString(R.string.revenue));
    }

    private void initUi() {
        tvDateFrom = binding.tvDateFrom;
        tvDateTo = binding.tvDateTo;
        tvTotalValue = binding.tvTotalValue;
        rcvOrderHistory = binding.rcvOrderHistory;
    }
    private void initListener() {
        tvDateFrom.setOnClickListener(new IOnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                GlobalFunction.showDatePicker(AdminRevenueActivity.this, tvDateFrom.getText().toString(), date -> {
                    tvDateFrom.setText(date);
                    getListRevenue();
                });
            }
        });

        tvDateTo.setOnClickListener(new IOnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                GlobalFunction.showDatePicker(AdminRevenueActivity.this, tvDateTo.getText().toString(), date -> {
                    tvDateTo.setText(date);
                    getListRevenue();
                });
            }
        });
    }

    private void getListRevenue() {
        MyApplication.get(this).getOrderDatabaseReference()
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Order> list = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Order order = dataSnapshot.getValue(Order.class);
                    if (canAddOrder(order)) {
                        list.add(0, order);
                    }
                }
                handleDataHistories(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private boolean canAddOrder(@Nullable Order order) {
        if (order == null) return false;
        if (Order.STATUS_COMPLETE != order.getStatus()) return false;
        String strDateFrom = tvDateFrom.getText().toString();
        String strDateTo = tvDateTo.getText().toString();
        if (StringUtil.isEmpty(strDateFrom) && StringUtil.isEmpty(strDateTo)) {
            return true;
        }
        String strDateOrder = DateTimeUtils.convertTimeStampToDate_2(order.getId());
        long longOrder = Long.parseLong(DateTimeUtils.convertDate2ToTimeStamp(strDateOrder));

        if (StringUtil.isEmpty(strDateFrom) && !StringUtil.isEmpty(strDateTo)) {
            long longDateTo = Long.parseLong(DateTimeUtils.convertDate2ToTimeStamp(strDateTo));
            return longOrder <= longDateTo;
        }
        if (!StringUtil.isEmpty(strDateFrom) && StringUtil.isEmpty(strDateTo)) {
            long longDateFrom = Long.parseLong(DateTimeUtils.convertDate2ToTimeStamp(strDateFrom));
            return longOrder >= longDateFrom;
        }
        long longDateTo = Long.parseLong(DateTimeUtils.convertDate2ToTimeStamp(strDateTo));
        long longDateFrom = Long.parseLong(DateTimeUtils.convertDate2ToTimeStamp(strDateFrom));
        return longOrder >= longDateFrom && longOrder <= longDateTo;
    }

    private void handleDataHistories(List<Order> list) {
        if (list == null) {
            return;
        }
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rcvOrderHistory.setLayoutManager(linearLayoutManager);
        AdminRevenueAdapter adminRevenueAdapter = new AdminRevenueAdapter(list);
        rcvOrderHistory.setAdapter(adminRevenueAdapter);

        // Calculate total
        String strTotalValue = getTotalValues(list) + Constant.CURRENCY;
        tvTotalValue.setText(strTotalValue);
    }

    private int getTotalValues(List<Order> list) {
        if (list == null || list.isEmpty()) {
            return 0;
        }

        int total = 0;
        for (Order order : list) {
            total += order.getTotal();
        }
        return total;
    }
}