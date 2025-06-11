package com.example.doantotnghiep.config;

public class MoMoConfig {
    // Test environment credentials (Thông tin test từ MoMo)
    public static final String PARTNER_CODE = "MOMOBKUN20180529";
    public static final String ACCESS_KEY = "klm05TvNBzhg7h7j";
    public static final String SECRET_KEY = "at67qH6mk8w5Y1nAyMoYKMWACiEi2bsa";

    // API URLs
    public static final String CREATE_ORDER_URL = "https://test-payment.momo.vn/v2/gateway/api/create";
    public static final String QUERY_ORDER_URL = "https://test-payment.momo.vn/v2/gateway/api/query";

    // App info
    public static final String REDIRECT_URL = "doantotnghiep://momo";
    public static final String IPN_URL = "https://webhook.site/your-webhook-url"; // Thay bằng webhook thật

    // Request Types - Sửa lại cho đúng
    public static final String REQUEST_TYPE_QR = "captureWallet";      // Cho QR code
    public static final String REQUEST_TYPE_APP = "payWithMethod";     // Cho app redirect

    // Other configs
    public static final String ORDER_INFO = "Nạp tiền vào ví";
    public static final String PARTNER_NAME = "DoAnTotNghiep";
    public static final String STORE_ID = "MomoTestStore";
    public static final String LANG = "vi";

    // Thêm các config mới
    public static final String AUTO_CAPTURE = "true";

    // Timeout configs
    public static final int CONNECTION_TIMEOUT = 30; // seconds
    public static final int READ_TIMEOUT = 30; // seconds
}