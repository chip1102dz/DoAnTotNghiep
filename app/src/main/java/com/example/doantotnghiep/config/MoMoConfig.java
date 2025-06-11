package com.example.doantotnghiep.config;

public class MoMoConfig {
    // Test environment credentials
    public static final String PARTNER_CODE = "MOMOBKUN20180529";
    public static final String ACCESS_KEY = "klm05TvNBzhg7h7j";
    public static final String SECRET_KEY = "at67qH6mk8w5Y1nAyMoYKMWACiEi2bsa";

    // API URLs
    public static final String CREATE_ORDER_URL = "https://test-payment.momo.vn/v2/gateway/api/create";
    public static final String QUERY_ORDER_URL = "https://test-payment.momo.vn/v2/gateway/api/query";

    // App info
    public static final String REDIRECT_URL = "doantotnghiep://momo";
    public static final String IPN_URL = "https://your-server.com/momo/callback";

    // QR Request Types
    public static final String REQUEST_TYPE_QR = "captureWallet"; // Cho QR code
    public static final String REQUEST_TYPE_APP = "payWithATM"; // Cho app redirect

    public static final String ORDER_INFO = "Nạp tiền vào ví";

    public static final String PARTNER_NAME = "DoAnTotNghiep";
    public static final String STORE_ID = "MomoTestStore";
    public static final String LANG = "vi";
}