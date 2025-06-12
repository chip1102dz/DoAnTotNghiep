package com.example.doantotnghiep.model;

import java.io.Serializable;

public class Notification implements Serializable {

    // Notification Types
    public static final int TYPE_ORDER = 1;
    public static final int TYPE_PROMOTION = 2;
    public static final int TYPE_SYSTEM = 3;
    public static final int TYPE_REMINDER = 4;

    // Order Status Types
    public static final int ORDER_STATUS_CONFIRMED = 1;
    public static final int ORDER_STATUS_PREPARING = 2;
    public static final int ORDER_STATUS_SHIPPING = 3;
    public static final int ORDER_STATUS_DELIVERED = 4;
    public static final int ORDER_STATUS_CANCELLED = 5;

    private long id;
    private String title;
    private String message;
    private int type;
    private long timestamp;
    private boolean isRead;
    private String userEmail;

    // For order notifications
    private Long orderId;
    private int orderStatus;

    // For promotion notifications
    private String promoCode;
    private String promoImageUrl;

    // For action button
    private String actionText;
    private String actionData;

    public Notification() {
        this.timestamp = System.currentTimeMillis();
        this.isRead = false;
    }

    public Notification(String title, String message, int type, String userEmail) {
        this();
        this.title = title;
        this.message = message;
        this.type = type;
        this.userEmail = userEmail;
    }

    // Order notification constructor
    public Notification(String title, String message, String userEmail, Long orderId, int orderStatus) {
        this(title, message, TYPE_ORDER, userEmail);
        this.orderId = orderId;
        this.orderStatus = orderStatus;
        this.actionText = "Xem đơn hàng";
        this.actionData = String.valueOf(orderId);
    }

    // Promotion notification constructor
    public Notification(String title, String message, String userEmail, String promoCode, String promoImageUrl) {
        this(title, message, TYPE_PROMOTION, userEmail);
        this.promoCode = promoCode;
        this.promoImageUrl = promoImageUrl;
        this.actionText = "Xem ưu đãi";
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public int getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(int orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getPromoCode() {
        return promoCode;
    }

    public void setPromoCode(String promoCode) {
        this.promoCode = promoCode;
    }

    public String getPromoImageUrl() {
        return promoImageUrl;
    }

    public void setPromoImageUrl(String promoImageUrl) {
        this.promoImageUrl = promoImageUrl;
    }

    public String getActionText() {
        return actionText;
    }

    public void setActionText(String actionText) {
        this.actionText = actionText;
    }

    public String getActionData() {
        return actionData;
    }

    public void setActionData(String actionData) {
        this.actionData = actionData;
    }

    // Helper methods
    public String getTypeString() {
        switch (type) {
            case TYPE_ORDER:
                return "Đơn hàng";
            case TYPE_PROMOTION:
                return "Khuyến mãi";
            case TYPE_SYSTEM:
                return "Hệ thống";
            case TYPE_REMINDER:
                return "Nhắc nhở";
            default:
                return "Thông báo";
        }
    }

    public int getIconResource() {
        switch (type) {
            case TYPE_ORDER:
                return com.example.doantotnghiep.R.drawable.ic_shopping_bag;
            case TYPE_PROMOTION:
                return com.example.doantotnghiep.R.drawable.ic_local_offer;
            case TYPE_SYSTEM:
                return com.example.doantotnghiep.R.drawable.ic_settings;
            case TYPE_REMINDER:
                return com.example.doantotnghiep.R.drawable.ic_schedule;
            default:
                return com.example.doantotnghiep.R.drawable.ic_notifications;
        }
    }

    public String getOrderStatusText() {
        switch (orderStatus) {
            case ORDER_STATUS_CONFIRMED:
                return "đã được xác nhận";
            case ORDER_STATUS_PREPARING:
                return "đang được chuẩn bị";
            case ORDER_STATUS_SHIPPING:
                return "đang giao hàng";
            case ORDER_STATUS_DELIVERED:
                return "đã giao thành công";
            case ORDER_STATUS_CANCELLED:
                return "đã bị hủy";
            default:
                return "có cập nhật mới";
        }
    }

    // Static factory methods for common notifications
    public static Notification createOrderNotification(String userEmail, Long orderId, int orderStatus) {
        String title = "Cập nhật đơn hàng #" + orderId;
        String message = "Đơn hàng của bạn " + getOrderStatusMessage(orderStatus);
        return new Notification(title, message, userEmail, orderId, orderStatus);
    }

    public static Notification createPromotionNotification(String userEmail, String promoTitle, String promoMessage, String promoCode) {
        return new Notification(promoTitle, promoMessage, userEmail, promoCode, null);
    }

    public static Notification createSystemNotification(String userEmail, String title, String message) {
        Notification notification = new Notification(title, message, TYPE_SYSTEM, userEmail);
        return notification;
    }

    private static String getOrderStatusMessage(int orderStatus) {
        switch (orderStatus) {
            case ORDER_STATUS_CONFIRMED:
                return "đã được xác nhận và đang được chuẩn bị";
            case ORDER_STATUS_PREPARING:
                return "đang được chuẩn bị với sự tận tâm";
            case ORDER_STATUS_SHIPPING:
                return "đang trên đường giao đến bạn";
            case ORDER_STATUS_DELIVERED:
                return "đã giao thành công. Cảm ơn bạn!";
            case ORDER_STATUS_CANCELLED:
                return "đã bị hủy. Liên hệ hỗ trợ nếu cần";
            default:
                return "có cập nhật mới";
        }
    }
}