package com.example.doantotnghiep.activity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.activity.EdgeToEdge;

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
    private boolean isBalancePayment = false;

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
        if (mOrderBooking == null) {
            showToastMessage("Lỗi: Không tìm thấy thông tin đơn hàng");
            finish();
            return;
        }

        // Kiểm tra phương thức thanh toán
        String paymentMethod = mOrderBooking.getPaymentMethod();
        Log.d(TAG, "Payment method: " + paymentMethod);
        Log.d(TAG, "Balance payment method name: " + getString(R.string.title_payment_method_balance));

        if (paymentMethod != null && paymentMethod.equals(getString(R.string.title_payment_method_balance))) {
            // Thanh toán bằng số dư
            isBalancePayment = true;
            processBalancePayment();
        } else {
            // Thanh toán tiền mặt - tạo đơn hàng trực tiếp
            isBalancePayment = false;
            createOrderFirebase();
        }
    }

    private void processBalancePayment() {
        User currentUser = DataStoreManager.getUser();
        if (currentUser == null) {
            Log.e(TAG, "Current user is null!");
            showToastMessage("Lỗi: Không tìm thấy thông tin người dùng");
            finish();
            return;
        }

        double currentBalance = currentUser.getBalance();
        double orderTotal = (double) mOrderBooking.getTotal();

        Log.d(TAG, "=== BALANCE PAYMENT PROCESSING ===");
        Log.d(TAG, "User email: " + currentUser.getEmail());
        Log.d(TAG, "Current balance: " + currentBalance);
        Log.d(TAG, "Order total: " + orderTotal);

        // Kiểm tra số dư lần cuối
        if (currentBalance < orderTotal) {
            double shortage = orderTotal - currentBalance;
            Log.e(TAG, "Insufficient balance! Current: " + currentBalance + ", Required: " + orderTotal + ", Shortage: " + shortage);

            String message = "❌ SỐ DƯ KHÔNG ĐỦ!\n\n" +
                    "💰 Số dư hiện tại: " + currentUser.getFormattedBalance() + "\n" +
                    "💳 Cần thanh toán: " + String.format("%,.0f", orderTotal) + "đ\n" +
                    "❌ Thiếu: " + String.format("%,.0f", shortage) + "đ\n\n" +
                    "Vui lòng nạp thêm tiền hoặc chọn thanh toán tiền mặt.";

            showToastMessage(message);
            finish();
            return;
        }

        // Thực hiện trừ tiền - SỬ DỤNG PHƯƠNG THỨC deductBalance()
        boolean deductSuccess = currentUser.deductBalance(orderTotal);

        if (!deductSuccess) {
            Log.e(TAG, "Failed to deduct balance!");
            showToastMessage("❌ Lỗi khi trừ tiền từ tài khoản");
            finish();
            return;
        }

        double newBalance = currentUser.getBalance();
        Log.d(TAG, "Balance deducted successfully. New balance: " + newBalance);

        // Lưu vào SharedPreferences ngay lập tức
        DataStoreManager.setUser(currentUser);
        Log.d(TAG, "Updated balance saved to DataStore: " + DataStoreManager.getUser().getBalance());

        // Cập nhật vào Firebase
        updateUserBalanceInFirebase(currentUser, () -> {
            Log.d(TAG, "Firebase balance update completed");
            createOrderFirebase();
        });
    }

    private void updateUserBalanceInFirebase(User user, Runnable onComplete) {
        String userKey = String.valueOf(GlobalFunction.encodeEmailUser());

        Map<String, Object> balanceUpdate = new HashMap<>();
        balanceUpdate.put("balance", user.getBalance());
        balanceUpdate.put("lastPaymentTime", System.currentTimeMillis());
        balanceUpdate.put("lastPaymentAmount", (double) mOrderBooking.getTotal());
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

        Log.d(TAG, "Updating Firebase with new balance: " + user.getBalance());

        MyApplication.get(this).getUserDatabaseReference(userKey)
                .updateChildren(balanceUpdate)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ Firebase balance update SUCCESS: " + user.getBalance());
                    if (onComplete != null) {
                        onComplete.run();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Firebase balance update FAILED: " + e.getMessage());
                    // Vẫn tiếp tục tạo đơn hàng vì số dư local đã được cập nhật
                    if (onComplete != null) {
                        onComplete.run();
                    }
                });
    }

    private void createOrderFirebase() {
        Log.d(TAG, "Creating order in Firebase...");

        MyApplication.get(this).getOrderDatabaseReference()
                .child(String.valueOf(mOrderBooking.getId()))
                .setValue(mOrderBooking, (error1, ref1) -> {
                    if (error1 == null) {
                        Log.d(TAG, "✅ Order created successfully");

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

                        // Hiển thị thông báo thành công
                        if (isBalancePayment) {
                            User updatedUser = DataStoreManager.getUser();
                            showToastMessage("✅ THANH TOÁN THÀNH CÔNG!\n\n" +
                                    "💳 Đã trừ: " + String.format("%,.0f", (double) mOrderBooking.getTotal()) + "đ\n" +
                                    "💰 Số dư còn lại: " + updatedUser.getFormattedBalance());
                        }

                        Bundle bundle = new Bundle();
                        bundle.putLong(Constant.ORDER_ID, mOrderBooking.getId());
                        GlobalFunction.startActivity(PaymentActivity.this,
                                ReceiptOrderActivity.class, bundle);
                        finish();
                    } else {
                        Log.e(TAG, "❌ Order creation failed: " + error1.getMessage());
                        showToastMessage("Lỗi tạo đơn hàng: " + error1.getMessage());

                        // Nếu tạo đơn hàng thất bại và đã trừ tiền, cần hoàn lại số dư
                        if (isBalancePayment) {
                            rollbackBalancePayment();
                        }

                        finish();
                    }
                });
    }

    private void rollbackBalancePayment() {
        Log.d(TAG, "Rolling back balance payment...");

        // Hoàn lại số dư nếu tạo đơn hàng thất bại
        User currentUser = DataStoreManager.getUser();
        double rollbackBalance = currentUser.getBalance() + mOrderBooking.getTotal();
        currentUser.setBalance(rollbackBalance);
        DataStoreManager.setUser(currentUser);

        // Cập nhật lại Firebase
        updateUserBalanceInFirebase(currentUser, null);

        Log.d(TAG, "Balance rolled back to: " + rollbackBalance);
        showToastMessage("⚠️ Đã hoàn lại số dư do lỗi tạo đơn hàng\n💰 Số dư: " + currentUser.getFormattedBalance());
    }
}