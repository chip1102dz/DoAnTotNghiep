package com.example.doantotnghiep.helper;

import android.util.Log;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class VietQRHelper {

    private static final String TAG = "VietQRHelper";

    // VietQR API Base URL
    private static final String VIETQR_BASE_URL = "https://img.vietqr.io/image";

    // Thông tin ngân hàng - BẠN CẦN THAY ĐỔI THEO THÔNG TIN THẬT
    public static final String BANK_CODE = "970415"; // VietinBank
    public static final String ACCOUNT_NUMBER = "104874849902"; // SỐ TÀI KHOẢN CỦA BẠN
    public static final String ACCOUNT_NAME = "TRIEU QUOC THINH"; // TÊN CHỦ TÀI KHOẢN

    /**
     * Tạo URL VietQR cho thanh toán
     * @param amount Số tiền (VNĐ)
     * @param description Nội dung chuyển khoản
     * @param userEmail Email người dùng (để tạo mã giao dịch)
     * @return URL của QR code
     */
    public static String generateVietQRUrl(double amount, String description, String userEmail) {
        try {
            // Tạo mã giao dịch duy nhất
            String transactionId = generateTransactionId(userEmail);

            // Tạo nội dung chuyển khoản
            String transferContent = String.format("%s - Ma GD: %s", description, transactionId);

            // Encode nội dung để đảm bảo URL safe
            String encodedContent = URLEncoder.encode(transferContent, StandardCharsets.UTF_8.toString());
            String encodedAccountName = URLEncoder.encode(ACCOUNT_NAME, StandardCharsets.UTF_8.toString());

            // Tạo VietQR URL
            String qrUrl = String.format(
                    "%s/%s-%s-compact2.jpg?amount=%d&addInfo=%s&accountName=%s",
                    VIETQR_BASE_URL,
                    BANK_CODE,
                    ACCOUNT_NUMBER,
                    (int) amount,
                    encodedContent,
                    encodedAccountName
            );

            Log.d(TAG, "Generated VietQR URL: " + qrUrl);
            return qrUrl;

        } catch (Exception e) {
            Log.e(TAG, "Error generating VietQR URL: " + e.getMessage());
            return getDefaultQRUrl(amount, description);
        }
    }

    /**
     * Tạo mã giao dịch duy nhất
     */
    private static String generateTransactionId(String userEmail) {
        String emailPrefix = userEmail.split("@")[0].replaceAll("[^a-zA-Z0-9]", "");
        long timestamp = System.currentTimeMillis();
        return emailPrefix.toUpperCase() + timestamp % 1000000; // 6 số cuối của timestamp
    }

    /**
     * URL VietQR mặc định (không có thông tin bổ sung)
     */
    private static String getDefaultQRUrl(double amount, String description) {
        return String.format(
                "%s/%s-%s-compact2.jpg?amount=%d",
                VIETQR_BASE_URL,
                BANK_CODE,
                ACCOUNT_NUMBER,
                (int) amount
        );
    }

    /**
     * Tạo URL VietQR với template khác
     */
    public static String generateVietQRUrlWithTemplate(double amount, String description, String userEmail, String template) {
        try {
            String transactionId = generateTransactionId(userEmail);
            String transferContent = String.format("%s - Ma GD: %s", description, transactionId);
            String encodedContent = URLEncoder.encode(transferContent, StandardCharsets.UTF_8.toString());

            // Templates: compact, compact2, qr_only, print
            return String.format(
                    "%s/%s-%s-%s.jpg?amount=%d&addInfo=%s&accountName=%s",
                    VIETQR_BASE_URL,
                    BANK_CODE,
                    ACCOUNT_NUMBER,
                    template,
                    (int) amount,
                    encodedContent,
                    URLEncoder.encode(ACCOUNT_NAME, StandardCharsets.UTF_8.toString())
            );

        } catch (Exception e) {
            Log.e(TAG, "Error generating VietQR URL with template: " + e.getMessage());
            return getDefaultQRUrl(amount, description);
        }
    }

    /**
     * Kiểm tra thông tin ngân hàng có hợp lệ không
     */
    public static boolean isValidBankInfo() {
        boolean isValid = !BANK_CODE.equals("970415") || !ACCOUNT_NUMBER.equals("1234567890");
        if (!isValid) {
            Log.w(TAG, "VietQR: Đang sử dụng thông tin ngân hàng mẫu! Vui lòng cập nhật thông tin thật.");
        }
        return isValid;
    }

    /**
     * Danh sách một số mã ngân hàng phổ biến
     */
    public static class BankCodes {
        public static final String VIETINBANK = "970415";
        public static final String VIETCOMBANK = "970436";
        public static final String BIDV = "970418";
        public static final String AGRIBANK = "970405";
        public static final String TECHCOMBANK = "970407";
        public static final String MB_BANK = "970422";
        public static final String SACOMBANK = "970403";
        public static final String ACB = "970416";
        public static final String VPBANK = "970432";
        public static final String TPBANK = "970423";
    }

    /**
     * Lấy tên ngân hàng từ mã
     */
    public static String getBankName(String bankCode) {
        switch (bankCode) {
            case "970415": return "VietinBank";
            case "970436": return "Vietcombank";
            case "970418": return "BIDV";
            case "970405": return "Agribank";
            case "970407": return "Techcombank";
            case "970422": return "MB Bank";
            case "970403": return "Sacombank";
            case "970416": return "ACB";
            case "970432": return "VPBank";
            case "970423": return "TPBank";
            default: return "Ngân hàng";
        }
    }
}