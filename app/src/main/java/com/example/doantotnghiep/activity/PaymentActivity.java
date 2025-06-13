package com.example.doantotnghiep.activity;

import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.doantotnghiep.MyApplication;
import com.example.doantotnghiep.R;
import com.example.doantotnghiep.database.ProductDatabase;
import com.example.doantotnghiep.databinding.ActivityPaymentBinding;
import com.example.doantotnghiep.event.DisplayCartEvent;
import com.example.doantotnghiep.event.OrderSuccessEvent;
import com.example.doantotnghiep.helper.NotificationHelper;
import com.example.doantotnghiep.model.Order;
import com.example.doantotnghiep.utils.Constant;
import com.example.doantotnghiep.utils.GlobalFunction;

import org.greenrobot.eventbus.EventBus;

public class PaymentActivity extends BaseActivity {
    ActivityPaymentBinding binding;
    private Order mOrderBooking;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityPaymentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getDataIntent();

        Handler handler = new Handler();
        handler.postDelayed(this::createOrderFirebase, 2000);
    }
    private void getDataIntent() {
        Bundle bundle = getIntent().getExtras();
        if (bundle == null) return;
        mOrderBooking = (Order) bundle.get(Constant.ORDER_OBJECT);
    }
    private void createOrderFirebase() {
        MyApplication.get(this).getOrderDatabaseReference()
                .child(String.valueOf(mOrderBooking.getId()))
                .setValue(mOrderBooking, (error1, ref1) -> {
                    if (error1 == null) {
                        // Tạo notification cho user
                        NotificationHelper.createOrderStatusNotification(
                                this,
                                mOrderBooking.getUserEmail(),
                                mOrderBooking.getId(),
                                -1, // No previous status
                                Order.STATUS_NEW
                        );

                        // Tạo notification cho admin về đơn hàng mới
                        NotificationHelper.createNewOrderNotificationForAdmin(
                                this,
                                mOrderBooking.getId(),
                                mOrderBooking.getUserEmail()
                        );

                        // Clear cart và chuyển trang
                        ProductDatabase.getInstance(this).productDAO().deleteAllProduct();
                        EventBus.getDefault().post(new DisplayCartEvent());
                        EventBus.getDefault().post(new OrderSuccessEvent());

                        Bundle bundle = new Bundle();
                        bundle.putLong(Constant.ORDER_ID, mOrderBooking.getId());
                        GlobalFunction.startActivity(PaymentActivity.this,
                                ReceiptOrderActivity.class, bundle);
                        finish();
                    } else {
                        showToastMessage("Lỗi tạo đơn hàng: " + error1.getMessage());
                    }
                });
    }


}