package com.example.doantotnghiep;

import android.app.Application;
import android.content.Context;

import com.example.doantotnghiep.prefs.DataStoreManager;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MyApplication extends Application {

    private static final String FIREBASE_URL = "https://doantotnghieppro-d2186-default-rtdb.firebaseio.com";
    private FirebaseDatabase mFirebaseDatabase;

    public static MyApplication get(Context context) {
        return (MyApplication) context.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        mFirebaseDatabase = FirebaseDatabase.getInstance(FIREBASE_URL);
        DataStoreManager.init(getApplicationContext());
    }

    // Existing methods...
    public DatabaseReference getAdminDatabaseReference() {
        return mFirebaseDatabase.getReference("admin");
    }

    public DatabaseReference getVoucherDatabaseReference() {
        return mFirebaseDatabase.getReference("voucher");
    }

    public DatabaseReference getAddressDatabaseReference() {
        return mFirebaseDatabase.getReference("address");
    }

    public DatabaseReference getCategoryDatabaseReference() {
        return mFirebaseDatabase.getReference("category");
    }

    public DatabaseReference getProductDatabaseReference() {
        return mFirebaseDatabase.getReference("product");
    }

    public DatabaseReference getProductDetailDatabaseReference(long productId) {
        return mFirebaseDatabase.getReference("product/" + productId);
    }

    public DatabaseReference getFeedbackDatabaseReference() {
        return mFirebaseDatabase.getReference("/feedback");
    }

    public DatabaseReference getOrderDatabaseReference() {
        return mFirebaseDatabase.getReference("order");
    }

    public DatabaseReference getRatingProductDatabaseReference(String productId) {
        return mFirebaseDatabase.getReference("/product/" + productId + "/rating");
    }

    public DatabaseReference getOrderDetailDatabaseReference(long orderId) {
        return mFirebaseDatabase.getReference("order/" + orderId);
    }

    public DatabaseReference getStoreLocationDatabaseReference() {
        return mFirebaseDatabase.getReference("store_location");
    }

    public DatabaseReference getUserDatabaseReference() {
        return mFirebaseDatabase.getReference("users");
    }

    public DatabaseReference getUserDatabaseReference(String userKey) {
        return mFirebaseDatabase.getReference("users/" + userKey);
    }

    // New methods for Notifications
    public DatabaseReference getNotificationDatabaseReference() {
        return mFirebaseDatabase.getReference("notifications");
    }

    public DatabaseReference getNotificationDatabaseReference(String notificationId) {
        return mFirebaseDatabase.getReference("notifications/" + notificationId);
    }

    public com.google.firebase.database.Query getUserNotificationsDatabaseReference(String userEmail) {
        return mFirebaseDatabase.getReference("notifications").orderByChild("userEmail").equalTo(userEmail);
    }

    // Helper method to get the main database reference
    public FirebaseDatabase getDatabase() {
        return mFirebaseDatabase;
    }

    // Method to create notification when order status changes
    public void createOrderNotification(String userEmail, Long orderId, int orderStatus) {
        String title = "Cập nhật đơn hàng #" + orderId;
        String message = getOrderStatusMessage(orderStatus);

        com.example.doantotnghiep.model.Notification notification =
                com.example.doantotnghiep.model.Notification.createOrderNotification(userEmail, orderId, orderStatus);
        notification.setId(System.currentTimeMillis());

        getNotificationDatabaseReference(String.valueOf(notification.getId()))
                .setValue(notification);
    }

    // Method to create promotion notification
    public void createPromotionNotification(String userEmail, String title, String message, String promoCode) {
        com.example.doantotnghiep.model.Notification notification =
                com.example.doantotnghiep.model.Notification.createPromotionNotification(userEmail, title, message, promoCode);
        notification.setId(System.currentTimeMillis());

        getNotificationDatabaseReference(String.valueOf(notification.getId()))
                .setValue(notification);
    }

    // Method to create system notification
    public void createSystemNotification(String userEmail, String title, String message) {
        com.example.doantotnghiep.model.Notification notification =
                com.example.doantotnghiep.model.Notification.createSystemNotification(userEmail, title, message);
        notification.setId(System.currentTimeMillis());

        getNotificationDatabaseReference(String.valueOf(notification.getId()))
                .setValue(notification);
    }

    private String getOrderStatusMessage(int orderStatus) {
        switch (orderStatus) {
            case com.example.doantotnghiep.model.Notification.ORDER_STATUS_CONFIRMED:
                return "đã được xác nhận và đang được chuẩn bị";
            case com.example.doantotnghiep.model.Notification.ORDER_STATUS_PREPARING:
                return "đang được chuẩn bị với sự tận tâm";
            case com.example.doantotnghiep.model.Notification.ORDER_STATUS_SHIPPING:
                return "đang trên đường giao đến bạn";
            case com.example.doantotnghiep.model.Notification.ORDER_STATUS_DELIVERED:
                return "đã giao thành công. Cảm ơn bạn!";
            case com.example.doantotnghiep.model.Notification.ORDER_STATUS_CANCELLED:
                return "đã bị hủy. Liên hệ hỗ trợ nếu cần";
            default:
                return "có cập nhật mới";
        }
    }
}