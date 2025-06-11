package com.example.doantotnghiep.helper;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.apache.commons.codec.binary.Hex;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import java.io.IOException;

import com.example.doantotnghiep.config.MoMoConfig;

public class MoMoHelper {

    private static final String TAG = "MoMoHelper";

    public interface MoMoListener {
        void onCreateOrderSuccess(String payUrl, String orderId);
        void onCreateQRSuccess(String qrCodeData, String orderId); // Thêm callback cho QR
        void onCreateOrderError(String error);
        void onPaymentResult(boolean success, String orderId, String message);
    }

    // Method tạo QR Payment
    public static void createQRPayment(Context context, double amount, String description,
                                       String userEmail, MoMoListener listener) {

        try {
            String orderId = "ORDER_" + System.currentTimeMillis();
            String requestId = "REQ_" + System.currentTimeMillis();

            // Tạo rawSignature cho QR
            String rawSignature = "accessKey=" + MoMoConfig.ACCESS_KEY +
                    "&amount=" + (long) amount +
                    "&extraData=" +
                    "&ipnUrl=" + MoMoConfig.IPN_URL +
                    "&orderId=" + orderId +
                    "&orderInfo=" + description +
                    "&partnerCode=" + MoMoConfig.PARTNER_CODE +
                    "&redirectUrl=" + MoMoConfig.REDIRECT_URL +
                    "&requestId=" + requestId +
                    "&requestType=" + MoMoConfig.REQUEST_TYPE_QR; // Sử dụng captureWallet

            String signature = signHmacSHA256(rawSignature, MoMoConfig.SECRET_KEY);

            // Tạo JSON request body
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("partnerCode", MoMoConfig.PARTNER_CODE);
            jsonBody.put("partnerName", "DoAnTotNghiep");
            jsonBody.put("storeId", "MomoTestStore");
            jsonBody.put("requestId", requestId);
            jsonBody.put("amount", (long) amount);
            jsonBody.put("orderId", orderId);
            jsonBody.put("orderInfo", description);
            jsonBody.put("redirectUrl", MoMoConfig.REDIRECT_URL);
            jsonBody.put("ipnUrl", MoMoConfig.IPN_URL);
            jsonBody.put("lang", "vi");
            jsonBody.put("extraData", "");
            jsonBody.put("requestType", MoMoConfig.REQUEST_TYPE_QR); // QR request type
            jsonBody.put("signature", signature);

            // Gửi request
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();

            RequestBody body = RequestBody.create(
                    jsonBody.toString(),
                    MediaType.get("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(MoMoConfig.CREATE_ORDER_URL)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "MoMo QR request failed: " + e.getMessage());
                    listener.onCreateOrderError("Lỗi kết nối: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        Log.d(TAG, "MoMo QR response: " + responseBody);

                        try {
                            JSONObject jsonResponse = new JSONObject(responseBody);
                            int resultCode = jsonResponse.getInt("resultCode");

                            if (resultCode == 0) {
                                String qrCodeUrl = jsonResponse.getString("qrCodeUrl");
                                listener.onCreateQRSuccess(qrCodeUrl, orderId);
                            } else {
                                String message = jsonResponse.optString("message", "Lỗi không xác định");
                                listener.onCreateOrderError("Lỗi tạo QR: " + message);
                            }
                        } catch (Exception parseException) {
                            Log.e(TAG, "Parse error: " + parseException.getMessage());
                            listener.onCreateOrderError("Lỗi xử lý phản hồi: " + parseException.getMessage());
                        }
                    } else {
                        listener.onCreateOrderError("Lỗi server: " + response.code());
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error creating MoMo QR: " + e.getMessage());
            listener.onCreateOrderError("Lỗi tạo QR: " + e.getMessage());
        }
    }

    // Method tạo App Payment (giữ nguyên từ code cũ)
    public static void createAppPayment(Context context, double amount, String description,
                                        String userEmail, MoMoListener listener) {
        // ... code tạo app payment như cũ, chỉ thay REQUEST_TYPE_APP
    }

    // Generate QR Code Bitmap từ URL
    public static Bitmap generateQRCode(String qrData, int width, int height) {
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            return barcodeEncoder.encodeBitmap(qrData, BarcodeFormat.QR_CODE, width, height);
        } catch (WriterException e) {
            Log.e(TAG, "Error generating QR code: " + e.getMessage());
            return null;
        }
    }

    // Query payment status (giữ nguyên)
    public static void queryPaymentStatus(String orderId, String requestId, MoMoListener listener) {
        // ... code query như cũ
    }

    private static String signHmacSHA256(String data, String key) throws NoSuchAlgorithmException, InvalidKeyException {
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(secretKey);
        byte[] hashBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return new String(Hex.encodeHex(hashBytes));
    }
}