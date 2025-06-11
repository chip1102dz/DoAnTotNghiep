package com.example.doantotnghiep.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class PaymentResultActivity extends BaseActivity {

    private static final String TAG = "PaymentResultActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        Uri data = intent.getData();

        if (data != null) {
            String scheme = data.getScheme();
            String host = data.getHost();

            Log.d(TAG, "Received callback: " + data.toString());

            if ("doantotnghiep".equals(scheme) && "momo".equals(host)) {
                // Xử lý callback từ MoMo
                String resultCode = data.getQueryParameter("resultCode");
                String orderId = data.getQueryParameter("orderId");

                if ("0".equals(resultCode)) {
                    // Thanh toán thành công
                    Log.d(TAG, "Payment success: " + orderId);
                } else {
                    // Thanh toán thất bại
                    Log.d(TAG, "Payment failed: " + orderId);
                }
            }
        }

        // Quay lại TopUpActivity
        finish();
    }
}