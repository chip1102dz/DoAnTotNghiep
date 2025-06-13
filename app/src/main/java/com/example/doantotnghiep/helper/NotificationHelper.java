package com.example.doantotnghiep.helper;

import android.content.Context;
import android.util.Log;

import com.example.doantotnghiep.MyApplication;
import com.example.doantotnghiep.model.Notification;
import com.example.doantotnghiep.model.Order;
import com.example.doantotnghiep.prefs.DataStoreManager;
import com.example.doantotnghiep.utils.Constant;

/**
 * Helper class để quản lý notifications trong ứng dụng
 */
public class NotificationHelper {

    private static final String TAG = "NotificationHelper";

    /**
     * Tạo thông báo khi trạng thái đơn hàng thay đổi
     */
    public static void createOrderStatusNotification(Context context, String userEmail, Long orderId, int oldStatus, int newStatus) {
        if (context == null || orderId == null) return;

        try {
            // Tạo notification dựa trên trạng thái mới
            Notification notification = createOrderNotificationByStatus(userEmail, orderId, newStatus);

            if (notification != null) {
                saveNotificationToFirebase(context, notification);
                Log.d(TAG, "Order notification created for order: " + orderId + ", status: " + newStatus);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error creating order notification: " + e.getMessage());
        }
    }

    /**
     * Tạo thông báo khuyến mãi
     */
    public static void createPromotionNotification(Context context, String userEmail, String title, String message, String promoCode) {
        if (context == null) return;

        try {
            Notification notification = Notification.createPromotionNotification(userEmail, title, message, promoCode);
            notification.setId(System.currentTimeMillis());

            saveNotificationToFirebase(context, notification);
            Log.d(TAG, "Promotion notification created: " + title);
        } catch (Exception e) {
            Log.e(TAG, "Error creating promotion notification: " + e.getMessage());
        }
    }

    /**
     * Tạo thông báo hệ thống
     */
    public static void createSystemNotification(Context context, String userEmail, String title, String message) {
        if (context == null) return;

        try {
            Notification notification = Notification.createSystemNotification(userEmail, title, message);
            notification.setId(System.currentTimeMillis());

            saveNotificationToFirebase(context, notification);
            Log.d(TAG, "System notification created: " + title);
        } catch (Exception e) {
            Log.e(TAG, "Error creating system notification: " + e.getMessage());
        }
    }

    /**
     * Tạo thông báo chào mừng cho user mới
     */
    public static void createWelcomeNotification(Context context, String userEmail) {
        String title = "🎉 Chào mừng đến với ứng dụng!";
        String message = "Cảm ơn bạn đã tham gia. Khám phá các món ăn ngon và ưu đãi hấp dẫn ngay thôi!";
        createSystemNotification(context, userEmail, title, message);
    }

    /**
     * Tạo thông báo khi có đơn hàng mới (cho admin)
     */
    public static void createNewOrderNotificationForAdmin(Context context, Long orderId, String customerEmail) {
        String adminEmail = Constant.MAIN_ADMIN;
        String title = "📦 Đơn hàng mới #" + orderId;
        String message = "Có đơn hàng mới từ khách hàng: " + customerEmail;
        createSystemNotification(context, adminEmail, title, message);
    }

    /**
     * Tạo thông báo nhắc nhở đánh giá sau khi hoàn thành đơn hàng
     */
    public static void createReviewReminderNotification(Context context, String userEmail, Long orderId) {
        String title = "⭐ Đánh giá đơn hàng #" + orderId;
        String message = "Hãy chia sẻ trải nghiệm của bạn để giúp chúng tôi cải thiện dịch vụ!";

        Notification notification = new Notification(title, message, Notification.TYPE_REMINDER, userEmail);
        notification.setId(System.currentTimeMillis());
        notification.setOrderId(orderId);
        notification.setActionText("Đánh giá ngay");
        notification.setActionData(String.valueOf(orderId));

        saveNotificationToFirebase(context, notification);
    }

    /**
     * Tạo notification object dựa trên trạng thái đơn hàng
     */
    private static Notification createOrderNotificationByStatus(String userEmail, Long orderId, int status) {
        String title = "📦 Cập nhật đơn hàng #" + orderId;
        String message;
        int notificationStatus;

        switch (status) {
            case Order.STATUS_NEW:
                message = "Đơn hàng của bạn đã được tiếp nhận và đang chờ xác nhận";
                notificationStatus = Notification.ORDER_STATUS_CONFIRMED;
                break;
            case Order.STATUS_DOING:
                message = "Đơn hàng của bạn đang được chuẩn bị với sự tận tâm";
                notificationStatus = Notification.ORDER_STATUS_PREPARING;
                break;
            case Order.STATUS_ARRIVED:
                message = "Đơn hàng của bạn đã sẵn sàng và đang được giao đến";
                notificationStatus = Notification.ORDER_STATUS_SHIPPING;
                break;
            case Order.STATUS_COMPLETE:
                message = "Đơn hàng đã hoàn thành! Cảm ơn bạn đã tin tưởng chúng tôi";
                notificationStatus = Notification.ORDER_STATUS_DELIVERED;
                break;
            default:
                return null;
        }

        Notification notification = new Notification(title, message, userEmail, orderId, notificationStatus);
        notification.setId(System.currentTimeMillis());
        return notification;
    }

    /**
     * Lưu notification vào Firebase
     */
    private static void saveNotificationToFirebase(Context context, Notification notification) {
        try {
            MyApplication.get(context)
                    .getNotificationDatabaseReference(String.valueOf(notification.getId()))
                    .setValue(notification)
                    .addOnSuccessListener(aVoid ->
                            Log.d(TAG, "Notification saved successfully: " + notification.getId()))
                    .addOnFailureListener(e ->
                            Log.e(TAG, "Failed to save notification: " + e.getMessage()));
        } catch (Exception e) {
            Log.e(TAG, "Error saving notification to Firebase: " + e.getMessage());
        }
    }

    /**
     * Đánh dấu notification đã đọc
     */
    public static void markNotificationAsRead(Context context, long notificationId) {
        if (context == null) return;

        try {
            MyApplication.get(context)
                    .getNotificationDatabaseReference(String.valueOf(notificationId))
                    .child("read")
                    .setValue(true)
                    .addOnSuccessListener(aVoid ->
                            Log.d(TAG, "Notification marked as read: " + notificationId))
                    .addOnFailureListener(e ->
                            Log.e(TAG, "Failed to mark notification as read: " + e.getMessage()));
        } catch (Exception e) {
            Log.e(TAG, "Error marking notification as read: " + e.getMessage());
        }
    }

    /**
     * Xóa tất cả notifications của một user
     */
    public static void clearAllUserNotifications(Context context, String userEmail) {
        if (context == null) return;

        try {
            MyApplication.get(context)
                    .getNotificationDatabaseReference()
                    .orderByChild("userEmail")
                    .equalTo(userEmail)
                    .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                        @Override
                        public void onDataChange(@androidx.annotation.NonNull com.google.firebase.database.DataSnapshot snapshot) {
                            for (com.google.firebase.database.DataSnapshot childSnapshot : snapshot.getChildren()) {
                                childSnapshot.getRef().removeValue();
                            }
                            Log.d(TAG, "All notifications cleared for user: " + userEmail);
                        }

                        @Override
                        public void onCancelled(@androidx.annotation.NonNull com.google.firebase.database.DatabaseError error) {
                            Log.e(TAG, "Failed to clear notifications: " + error.getMessage());
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error clearing user notifications: " + e.getMessage());
        }
    }

    /**
     * Gửi notification chung cho tất cả users (admin only)
     */
    public static void sendBroadcastNotification(Context context, String title, String message, int type) {
        // Implementation cho broadcast notification nếu cần
        // Có thể sử dụng Firebase Cloud Messaging (FCM) cho chức năng này
        Log.d(TAG, "Broadcast notification feature not implemented yet");
    }

    /**
     * Kiểm tra xem user có notifications chưa đọc không
     */
    public static void checkUnreadNotifications(Context context, String userEmail, UnreadNotificationCallback callback) {
        if (context == null || callback == null) return;

        try {
            MyApplication.get(context)
                    .getNotificationDatabaseReference()
                    .orderByChild("userEmail")
                    .equalTo(userEmail)
                    .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                        @Override
                        public void onDataChange(@androidx.annotation.NonNull com.google.firebase.database.DataSnapshot snapshot) {
                            int unreadCount = 0;
                            for (com.google.firebase.database.DataSnapshot childSnapshot : snapshot.getChildren()) {
                                Notification notification = childSnapshot.getValue(Notification.class);
                                if (notification != null && !notification.isRead()) {
                                    unreadCount++;
                                }
                            }
                            callback.onUnreadCountReceived(unreadCount);
                        }

                        @Override
                        public void onCancelled(@androidx.annotation.NonNull com.google.firebase.database.DatabaseError error) {
                            callback.onUnreadCountReceived(0);
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error checking unread notifications: " + e.getMessage());
            callback.onUnreadCountReceived(0);
        }
    }

    /**
     * Interface để callback số lượng notification chưa đọc
     */
    public interface UnreadNotificationCallback {
        void onUnreadCountReceived(int count);
    }
}