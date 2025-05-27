package com.example.doantotnghiep.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doantotnghiep.MyApplication;
import com.example.doantotnghiep.R;
import com.example.doantotnghiep.adapter.ProductOrderAdapter;
import com.example.doantotnghiep.databinding.ActivityReceiptOrderBinding;
import com.example.doantotnghiep.model.Order;
import com.example.doantotnghiep.utils.Constant;
import com.example.doantotnghiep.utils.DateTimeUtils;
import com.example.doantotnghiep.utils.GlobalFunction;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class ReceiptOrderActivity extends BaseActivity {
    ActivityReceiptOrderBinding binding;
    private TextView tvIdTransaction, tvDateTime;
    private RecyclerView rcvProducts;
    private TextView tvPrice, tvVoucher, tvTotal, tvPaymentMethod;
    private TextView tvName, tvPhone, tvAddress;
    private TextView tvTrackingOrder;

    private long orderId;
    private Order mOrder;
    private ValueEventListener mValueEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReceiptOrderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getDataIntent();
        initToolbar();
        initUi();
        initListener();
        getOrderDetailFromFirebase();
    }

    private void getDataIntent() {
        Bundle bundle = getIntent().getExtras();
        if (bundle == null) return;
        orderId = bundle.getLong(Constant.ORDER_ID);
    }

    private void initToolbar() {
        ImageView imgToolbarBack = binding.toolbar.imgToolbarBack;
        TextView tvToolbarTitle = binding.toolbar.tvToolbarTitle;
        imgToolbarBack.setOnClickListener(view -> finish());
        tvToolbarTitle.setText(getString(R.string.label_receipt_order));
    }

    private void initUi() {
        tvIdTransaction = binding.tvIdTransaction;
        tvDateTime = binding.tvDateTime;
        rcvProducts = binding.rcvProducts;
        tvPrice = binding.tvPrice;
        tvVoucher = binding.tvVoucher;
        tvTotal = binding.tvTotal;
        tvPaymentMethod = binding.tvPaymentMethod;
        tvTrackingOrder = binding.tvTrackingOrder;
        tvName = binding.tvName;
        tvPhone = binding.tvPhone;
        tvAddress = binding.tvAddress;
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rcvProducts.setLayoutManager(linearLayoutManager);
    }
    private void initListener() {
        tvTrackingOrder.setOnClickListener(view -> {
            if (mOrder == null) return;
            Bundle bundle = new Bundle();
            bundle.putLong(Constant.ORDER_ID, mOrder.getId());
            GlobalFunction.startActivity(ReceiptOrderActivity.this,
                    TrackingOrderActivity.class, bundle);
            finish();
        });
    }

    private void getOrderDetailFromFirebase() {
        showProgressDialog(true);
        mValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                showProgressDialog(false);
                mOrder = snapshot.getValue(Order.class);
                if (mOrder == null) return;

                initData();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showProgressDialog(false);
                showToastMessage(getString(R.string.msg_get_date_error));
            }
        };
        MyApplication.get(this).getOrderDetailDatabaseReference(orderId)
                .addValueEventListener(mValueEventListener);
    }

    private void initData() {
        tvIdTransaction.setText(String.valueOf(mOrder.getId()));
        tvDateTime.setText(DateTimeUtils.convertTimeStampToDate(Long.parseLong(mOrder.getDateTime())));
        String strPrice = mOrder.getPrice() + Constant.CURRENCY;
        tvPrice.setText(strPrice);
        String strVoucher = "-" + mOrder.getVoucher() + Constant.CURRENCY;
        tvVoucher.setText(strVoucher);
        String strTotal = mOrder.getTotal() + Constant.CURRENCY;
        tvTotal.setText(strTotal);
        tvPaymentMethod.setText(mOrder.getPaymentMethod());
        tvName.setText(mOrder.getAddress().getName());
        tvPhone.setText(mOrder.getAddress().getPhone());
        tvAddress.setText(mOrder.getAddress().getAddress());
        ProductOrderAdapter adapter = new ProductOrderAdapter(mOrder.getProducts());
        rcvProducts.setAdapter(adapter);
        if (Order.STATUS_COMPLETE == mOrder.getStatus()) {
            tvTrackingOrder.setVisibility(View.GONE);
        } else {
            tvTrackingOrder.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mValueEventListener != null) {
            MyApplication.get(this).getOrderDetailDatabaseReference(orderId)
                    .removeEventListener(mValueEventListener);
        }
    }
}