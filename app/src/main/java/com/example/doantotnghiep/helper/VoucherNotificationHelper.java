package com.example.doantotnghiep.helper;

import android.content.Context;
import android.util.Log;

import com.example.doantotnghiep.MyApplication;
import com.example.doantotnghiep.model.Notification;
import com.example.doantotnghiep.model.User;
import com.example.doantotnghiep.model.Voucher;
import com.example.doantotnghiep.prefs.DataStoreManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class VoucherNotificationHelper {

    private static final String TAG = "VoucherNotificationHelper";

    /**
     * Gửi thông báo voucher mới cho tất cả users
     */
    public static void broadcastNewVoucherNotification(Context context, Voucher voucher) {
        if (context == null || voucher == null) return;

        // Lấy danh sách tất cả users
        MyApplication.get(context).getUserDatabaseReference()
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            String userEmail = userSnapshot.child("email").getValue(String.class);
                            if (userEmail != null && !userEmail.contains("@admin.com")) {
                                createVoucherNotificationForUser(context, userEmail, voucher);
                            }
                        }
                        Log.d(TAG, "Sent voucher notification to all users");
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.e(TAG, "Failed to get users for voucher notification: " + error.getMessage());
                    }
                });
    }

    /**
     * Tạo thông báo voucher cho một user cụ thể
     */
    public static void createVoucherNotificationForUser(Context context, String userEmail, Voucher voucher) {
        String title = "🎉 Voucher mới dành cho bạn!";
        String message = String.format(
                "Giảm %d%% cho đơn hàng từ %s. Sử dụng ngay để không bỏ lỡ!",
                voucher.getDiscount(),
                voucher.getMinimumText()
        );

        NotificationHelper.createPromotionNotification(
                context,
                userEmail,
                title,
                message,
                "VOUCHER" + voucher.getId()
        );
    }

    /**
     * Tạo thông báo voucher sắp hết hạn
     */
    public static void createVoucherExpiryNotification(Context context, String userEmail, Voucher voucher) {
        String title = "⏰ Voucher sắp hết hạn!";
        String message = String.format(
                "Voucher giảm %d%% sắp hết hạn. Đừng bỏ lỡ cơ hội tiết kiệm!",
                voucher.getDiscount()
        );

        NotificationHelper.createPromotionNotification(
                context,
                userEmail,
                title,
                message,
                "EXPIRE" + voucher.getId()
        );
    }

    /**
     * Tạo thông báo voucher đặc biệt cho user thân thiết
     */
    public static void createVIPVoucherNotification(Context context, String userEmail, Voucher voucher) {
        String title = "👑 Voucher VIP đặc biệt!";
        String message = String.format(
                "Chúc mừng! Bạn nhận được voucher VIP giảm %d%% dành riêng cho khách hàng thân thiết",
                voucher.getDiscount()
        );

        NotificationHelper.createPromotionNotification(
                context,
                userEmail,
                title,
                message,
                "VIP" + voucher.getId()
        );
    }

    /**
     * Tạo thông báo combo voucher
     */
    public static void createComboVoucherNotification(Context context, String userEmail, List<Voucher> vouchers) {
        String title = "🔥 Combo voucher siêu hấp dẫn!";
        StringBuilder message = new StringBuilder("Nhận ngay combo voucher:\n");

        for (int i = 0; i < Math.min(3, vouchers.size()); i++) {
            Voucher voucher = vouchers.get(i);
            message.append("• Giảm ").append(voucher.getDiscount()).append("%\n");
        }
        message.append("Áp dụng ngay hôm nay!");

        NotificationHelper.createPromotionNotification(
                context,
                userEmail,
                title,
                message.toString(),
                "COMBO" + System.currentTimeMillis()
        );
    }
}