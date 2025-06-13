package com.example.doantotnghiep.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doantotnghiep.MyApplication;
import com.example.doantotnghiep.R;
import com.example.doantotnghiep.adapter.ProductOrderAdapter;
import com.example.doantotnghiep.databinding.ActivityTrackingOrderBinding;
import com.example.doantotnghiep.helper.NotificationHelper;
import com.example.doantotnghiep.model.Order;
import com.example.doantotnghiep.model.RatingReview;
import com.example.doantotnghiep.prefs.DataStoreManager;
import com.example.doantotnghiep.utils.Constant;
import com.example.doantotnghiep.utils.GlobalFunction;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class TrackingOrderActivity extends BaseActivity {
    ActivityTrackingOrderBinding binding;
    private RecyclerView rcvProducts;
    private LinearLayout layoutReceiptOrder;
    private View dividerStep1, dividerStep2;
    private ImageView imgStep1, imgStep2, imgStep3;
    private TextView tvTakeOrder, tvTakeOrderMessage;

    private long orderId;
    private Order mOrder;
    private boolean isOrderArrived;
    private ValueEventListener mValueEventListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTrackingOrderBinding.inflate(getLayoutInflater());
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
        ImageView imgToolbarBack = findViewById(R.id.img_toolbar_back);
        TextView tvToolbarTitle = findViewById(R.id.tv_toolbar_title);
        imgToolbarBack.setOnClickListener(view -> finish());
        tvToolbarTitle.setText(getString(R.string.label_tracking_order));
    }

    private void initUi() {
        layoutReceiptOrder = binding.layoutReceiptOrder;
        dividerStep1 = binding.dividerStep1;
        dividerStep2 = binding.dividerStep2;
        imgStep1 = binding.imgStep1;
        imgStep2 = binding.imgStep2;
        imgStep3 = binding.imgStep3;
        tvTakeOrder = binding.tvTakeOrder;
        tvTakeOrderMessage = binding.tvTakeOrderMessage;
        LinearLayout layoutBottom = binding.layoutBottom;
        if (DataStoreManager.getUser().isAdmin()) {
            layoutBottom.setVisibility(View.GONE);
        } else {
            layoutBottom.setVisibility(View.VISIBLE);
        }
        rcvProducts = binding.rcvProducts;
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rcvProducts.setLayoutManager(linearLayoutManager);
    }

    private void initListener() {
        layoutReceiptOrder.setOnClickListener(view -> {
            if (mOrder == null) return;
            Bundle bundle = new Bundle();
            bundle.putLong(Constant.ORDER_ID, mOrder.getId());
            GlobalFunction.startActivity(TrackingOrderActivity.this,
                    ReceiptOrderActivity.class, bundle);
            finish();
        });

        if (DataStoreManager.getUser().isAdmin()) {
            imgStep1.setOnClickListener(view -> updateStatusOrder(Order.STATUS_NEW));
            imgStep2.setOnClickListener(view -> updateStatusOrder(Order.STATUS_DOING));
            imgStep3.setOnClickListener(view -> updateStatusOrder(Order.STATUS_ARRIVED));
        } else {
            imgStep1.setOnClickListener(null);
            imgStep2.setOnClickListener(null);
            imgStep3.setOnClickListener(null);
        }
        tvTakeOrder.setOnClickListener(view -> {
            if (isOrderArrived) {
                updateStatusOrder(Order.STATUS_COMPLETE);
            }
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
        ProductOrderAdapter adapter = new ProductOrderAdapter(mOrder.getProducts());
        rcvProducts.setAdapter(adapter);

        switch (mOrder.getStatus()) {
            case Order.STATUS_NEW:
                imgStep1.setImageResource(R.drawable.ic_step_enable);
                dividerStep1.setBackgroundColor(ContextCompat.getColor(this, R.color.green));
                imgStep2.setImageResource(R.drawable.ic_step_disable);
                dividerStep2.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent));
                imgStep3.setImageResource(R.drawable.ic_step_disable);

                isOrderArrived = false;
                tvTakeOrder.setBackgroundResource(R.drawable.bg_button_disable_corner_10);
                tvTakeOrderMessage.setVisibility(View.GONE);
                break;

            case Order.STATUS_DOING:
                imgStep1.setImageResource(R.drawable.ic_step_enable);
                dividerStep1.setBackgroundColor(ContextCompat.getColor(this, R.color.green));
                imgStep2.setImageResource(R.drawable.ic_step_enable);
                dividerStep2.setBackgroundColor(ContextCompat.getColor(this, R.color.green));
                imgStep3.setImageResource(R.drawable.ic_step_disable);

                isOrderArrived = false;
                tvTakeOrder.setBackgroundResource(R.drawable.bg_button_disable_corner_10);
                tvTakeOrderMessage.setVisibility(View.GONE);
                break;

            case Order.STATUS_ARRIVED:
                imgStep1.setImageResource(R.drawable.ic_step_enable);
                dividerStep1.setBackgroundColor(ContextCompat.getColor(this, R.color.green));
                imgStep2.setImageResource(R.drawable.ic_step_enable);
                dividerStep2.setBackgroundColor(ContextCompat.getColor(this, R.color.green));
                imgStep3.setImageResource(R.drawable.ic_step_enable);

                isOrderArrived = true;
                tvTakeOrder.setBackgroundResource(R.drawable.bg_button_enable_corner_10);
                tvTakeOrderMessage.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void updateStatusOrder(int newStatus) {
        if (mOrder == null) return;

        int oldStatus = mOrder.getStatus();

        Map<String, Object> map = new HashMap<>();
        map.put("status", newStatus);

        MyApplication.get(this).getOrderDatabaseReference()
                .child(String.valueOf(mOrder.getId()))
                .updateChildren(map, (error, ref) -> {
                    if (error == null) {
                        // Tạo notification cho user khi admin cập nhật trạng thái
                        NotificationHelper.createOrderStatusNotification(
                                this,
                                mOrder.getUserEmail(),
                                mOrder.getId(),
                                oldStatus,
                                newStatus
                        );

                        // Tạo notification qua MyApplication (backup)
                        MyApplication.get(this).createOrderNotification(
                                mOrder.getUserEmail(),
                                mOrder.getId(),
                                newStatus
                        );

                        if (Order.STATUS_COMPLETE == newStatus) {
                            // Tạo notification nhắc nhở đánh giá
                            NotificationHelper.createReviewReminderNotification(
                                    this,
                                    mOrder.getUserEmail(),
                                    mOrder.getId()
                            );

                            Bundle bundle = new Bundle();
                            RatingReview ratingReview = new RatingReview(
                                    RatingReview.TYPE_RATING_REVIEW_ORDER,
                                    String.valueOf(mOrder.getId())
                            );
                            bundle.putSerializable(Constant.RATING_REVIEW_OBJECT, ratingReview);
                            GlobalFunction.startActivity(TrackingOrderActivity.this,
                                    RatingReviewActivity.class, bundle);
                            finish();
                        }
                    } else {
                        showToastMessage("Lỗi cập nhật trạng thái: " + error.getMessage());
                    }
                });
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