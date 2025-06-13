package com.example.doantotnghiep.activity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

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
import com.example.doantotnghiep.model.User;
import com.example.doantotnghiep.prefs.DataStoreManager;
import com.example.doantotnghiep.utils.Constant;
import com.example.doantotnghiep.utils.GlobalFunction;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.Map;

public class PaymentActivity extends BaseActivity {

    private static final String TAG = "PaymentActivity";

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
        handler.postDelayed(this::processPayment, 2000);
    }

    private void getDataIntent() {
        Bundle bundle = getIntent().getExtras();
        if (bundle == null) return;
        mOrderBooking = (Order) bundle.get(Constant.ORDER_OBJECT);
    }

    private void processPayment() {
        // Kiểm tra phương thức thanh toán
        if (mOrderBooking.getPaymentMethod().equals(getString(R.string.title_payment_method_balance))) {
            // Thanh toán bằng số dư
            processBalancePayment();
        } else {
            // Thanh toán tiền mặt - tạo đơn hàng trực tiếp
            createOrderFirebase();
        }
    }

    private void processBalancePayment() {
        User currentUser = DataStoreManager.getUser();

        // Kiểm tra số dư một lần nữa
        if (currentUser.getBalance() < mOrderBooking.getTotal()) {
            showToastMessage("Số dư không đủ để thanh toán!");
            finish();
            return;
        }

        // Trừ tiền từ số dư
        boolean success = currentUser.deductBalance(mOrderBooking.getTotal());
        if (success) {
            // Cập nhật user trong local
            DataStoreManager.setUser(currentUser);

            // Cập nhật số dư trong Firebase
            updateUserBalanceInFirebase(currentUser);

            // Tạo đơn hàng
            createOrderFirebase();
        } else {
            showToastMessage("Không thể thực hiện thanh toán!");
            finish();
        }
    }

    private void updateUserBalanceInFirebase(User user) {
        String userKey = String.valueOf(GlobalFunction.encodeEmailUser());

        Map<String, Object> balanceUpdate = new HashMap<>();
        balanceUpdate.put("balance", user.getBalance());
        balanceUpdate.put("lastPaymentTime", System.currentTimeMillis());
        balanceUpdate.put("email", user.getEmail());

        // Giữ nguyên các thông tin khác của user
        if (user.getFullName() != null) {
            balanceUpdate.put("fullName", user.getFullName());
        }
        if (user.getPhoneNumber() != null) {
            balanceUpdate.put("phoneNumber", user.getPhoneNumber());
        }
        if (user.getAddress() != null) {
            balanceUpdate.put("address", user.getAddress());
        }
        if (user.getProfileImageUrl() != null) {
            balanceUpdate.put("profileImageUrl", user.getProfileImageUrl());
        }
        if (user.getDateOfBirth() != null) {
            balanceUpdate.put("dateOfBirth", user.getDateOfBirth());
        }
        if (user.getGender() != null) {
            balanceUpdate.put("gender", user.getGender());
        }

        MyApplication.get(this).getUserDatabaseReference(userKey)
                .updateChildren(balanceUpdate)
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "User balance updated successfully: " + user.getBalance()))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to update user balance: " + e.getMessage()));
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
                        finish();
                    }
                });
    }
}