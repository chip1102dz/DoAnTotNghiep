package com.example.doantotnghiep.helper;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.doantotnghiep.MyApplication;
import com.example.doantotnghiep.model.Voucher;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NotificationScheduler {

    private static final String TAG = "NotificationScheduler";
    private static final long PROMOTION_INTERVAL = 24 * 60 * 60 * 1000; // 24 hours
    private static final long REMINDER_INTERVAL = 3 * 24 * 60 * 60 * 1000; // 3 days

    private Context context;
    private Handler handler;
    private List<Runnable> scheduledTasks;

    public NotificationScheduler(Context context) {
        this.context = context;
        this.handler = new Handler(Looper.getMainLooper());
        this.scheduledTasks = new ArrayList<>();
    }

    /**
     * Bắt đầu lên lịch các notification
     */
    public void startScheduling() {
        schedulePromotionNotifications();
        scheduleReminderNotifications();
        scheduleSpecialOfferNotifications();
    }

    /**
     * Lên lịch notification khuyến mãi hàng ngày
     */
    private void schedulePromotionNotifications() {
        Runnable promotionTask = new Runnable() {
            @Override
            public void run() {
                sendDailyPromotionNotification();
                handler.postDelayed(this, PROMOTION_INTERVAL);
            }
        };

        scheduledTasks.add(promotionTask);
        handler.postDelayed(promotionTask, PROMOTION_INTERVAL);
    }

    /**
     * Lên lịch notification nhắc nhở
     */
    private void scheduleReminderNotifications() {
        Runnable reminderTask = new Runnable() {
            @Override
            public void run() {
                sendReminderNotification();
                handler.postDelayed(this, REMINDER_INTERVAL);
            }
        };

        scheduledTasks.add(reminderTask);
        handler.postDelayed(reminderTask, REMINDER_INTERVAL);
    }

    /**
     * Lên lịch notification ưu đãi đặc biệt
     */
    private void scheduleSpecialOfferNotifications() {
        // Random time between 1-7 days
        long randomInterval = (1 + new Random().nextInt(7)) * 24 * 60 * 60 * 1000;

        Runnable specialOfferTask = new Runnable() {
            @Override
            public void run() {
                sendSpecialOfferNotification();
                long nextInterval = (1 + new Random().nextInt(7)) * 24 * 60 * 60 * 1000;
                handler.postDelayed(this, nextInterval);
            }
        };

        scheduledTasks.add(specialOfferTask);
        handler.postDelayed(specialOfferTask, randomInterval);
    }

    private void sendDailyPromotionNotification() {
        // Lấy voucher ngẫu nhiên và gửi notification
        MyApplication.get(context).getVoucherDatabaseReference()
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        List<Voucher> vouchers = new ArrayList<>();
                        for (DataSnapshot voucherSnapshot : snapshot.getChildren()) {
                            Voucher voucher = voucherSnapshot.getValue(Voucher.class);
                            if (voucher != null) {
                                vouchers.add(voucher);
                            }
                        }

                        if (!vouchers.isEmpty()) {
                            Voucher randomVoucher = vouchers.get(new Random().nextInt(vouchers.size()));
                            VoucherNotificationHelper.broadcastNewVoucherNotification(context, randomVoucher);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.e(TAG, "Failed to get vouchers: " + error.getMessage());
                    }
                });
    }

    private void sendReminderNotification() {
        String title = "🍽️ Đói chưa? Về với chúng tôi nhé!";
        String message = "Có nhiều món ngon đang chờ bạn khám phá. Đặt hàng ngay để nhận ưu đãi!";

        // Gửi cho tất cả users (có thể lọc theo users không hoạt động gần đây)
        sendNotificationToAllUsers(title, message);
    }

    private void sendSpecialOfferNotification() {
        String[] titles = {
                "⚡ Flash Sale - Giảm 50%!",
                "🎊 Ưu đãi cuối tuần!",
                "🌟 Combo siêu tiết kiệm!",
                "🔥 Happy Hour - Giảm giá đặc biệt!"
        };

        String[] messages = {
                "Flash Sale chỉ trong 2 tiếng! Giảm 50% tất cả món ăn",
                "Cuối tuần vui vẻ với ưu đãi mua 1 tặng 1",
                "Combo 3 món chỉ từ 99k. Gọi ngay kẻo lỡ!",
                "Happy Hour từ 14h-16h: Giảm 30% tất cả đồ uống"
        };

        Random random = new Random();
        int index = random.nextInt(titles.length);

        sendNotificationToAllUsers(titles[index], messages[index]);
    }

    private void sendNotificationToAllUsers(String title, String message) {
        MyApplication.get(context).getUserDatabaseReference()
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            String userEmail = userSnapshot.child("email").getValue(String.class);
                            if (userEmail != null && !userEmail.contains("@admin.com")) {
                                NotificationHelper.createSystemNotification(context, userEmail, title, message);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.e(TAG, "Failed to send notification to all users: " + error.getMessage());
                    }
                });
    }

    /**
     * Dừng tất cả scheduled tasks
     */
    public void stopScheduling() {
        for (Runnable task : scheduledTasks) {
            handler.removeCallbacks(task);
        }
        scheduledTasks.clear();
    }
}